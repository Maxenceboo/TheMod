package fr.themod.event;

import fr.themod.block.WornGrassBlock;
import fr.themod.registry.ModBlocks;
import fr.themod.world.GrassWearSavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class GrassWearHandler {
    private static final int WALK_WEAR_INTERVAL_TICKS = 4;
    private static final int REGROW_SCAN_INTERVAL_TICKS = 80;
    private static final int REGROW_SCAN_RADIUS = 5;
    private static final int BASE_REGROW_CHANCE = 10;
    private static final int RECENT_USE_REGROW_PENALTY = 18;
    private static final int RECENT_USE_WINDOW_TICKS = 24000;
    private static final int WEAR_PRESSURE_THRESHOLD = 7;
    private static final int MAX_TRACKED_PRESSURE = 4096;
    private static final int MAX_TRACKED_ENTITY_STEPS = 4096;
    private static final int WORN_GRASS_VARIANTS = 4;

    private final Map<UUID, BlockPos> lastWalkedBlocks = new HashMap<>();

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity livingEntity)
                || entity.level().isClientSide()
                || !entity.onGround()
                || entity.isPassenger()
                || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (livingEntity instanceof Player player && player.isSpectator()) {
            return;
        }

        if (entity.tickCount % WALK_WEAR_INTERVAL_TICKS == 0) {
            wearBlockUnderEntity(level, livingEntity);
        }

        if (livingEntity instanceof Player player && entity.tickCount % REGROW_SCAN_INTERVAL_TICKS == 0) {
            slowlyRegrowNearbyGrass(level, player.blockPosition());
        }
    }

    @SubscribeEvent
    public void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getLevel().isClientSide()
                || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK
                || !event.getItemStack().is(ItemTags.SHOVELS)
                || event.getFace() == Direction.DOWN
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        BlockState flattenedState = shovelFlattenedState(level, state);

        if (flattenedState == null || flattenedState == state) {
            return;
        }

        level.setBlock(pos, flattenedState, Block.UPDATE_ALL);
        grassData(level).resetPressure(pos, level.getGameTime());
        playWearFeedback(level, pos, flattenedState, SoundEvents.SHOVEL_FLATTEN, 0.9F);
        damageTool(event.getItemStack(), level, event.getPlayer(), event.getHand() == InteractionHand.MAIN_HAND);
        event.cancelWithResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public void onBonemeal(BonemealEvent event) {
        if (!event.getState().is(ModBlocks.WORN_GRASS.get())) {
            return;
        }

        event.setSuccessful(true);

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState regrownState = previousRegrowthState(event.getState());

        if (regrownState != null) {
            level.setBlock(pos, regrownState, Block.UPDATE_ALL);

            if (regrownState.is(Blocks.GRASS_BLOCK)) {
                grassData(level).removePressure(pos);
            } else {
                grassData(level).resetPressure(pos, level.getGameTime());
            }

            level.playSound(null, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 0.7F, 1.1F);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState()),
                    pos.getX() + 0.5D,
                    pos.getY() + 1.05D,
                    pos.getZ() + 0.5D,
                    8,
                    0.35D,
                    0.04D,
                    0.35D,
                    0.02D
            );
        }
    }

    private void wearBlockUnderEntity(ServerLevel level, LivingEntity entity) {
        BlockPos pos = entity.getBlockPosBelowThatAffectsMyMovement();
        UUID entityId = entity.getUUID();
        BlockPos previousPos = lastWalkedBlocks.get(entityId);

        if (pos.equals(previousPos)) {
            return;
        }

        lastWalkedBlocks.put(entityId, pos.immutable());

        int pressure = footPressure(entity);
        applyFootPressure(level, pos, pressure, true);
        spreadFootPressure(level, pos, pressure);
        trimTrackedEntitySteps();
    }

    private int footPressure(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.isSteppingCarefully()) {
                return 1;
            }

            int pressure = player.isSprinting() ? 4 : 2;

            if (player.fallDistance > 1.5F) {
                pressure += Math.min(4, (int) player.fallDistance);
            }

            return pressure;
        }

        float footprint = entity.getBbWidth() * entity.getBbHeight();
        int pressure = footprint >= 4.0F ? 4 : footprint >= 2.0F ? 3 : footprint >= 1.0F ? 2 : 1;

        if (entity.isSprinting()) {
            pressure++;
        }

        return pressure;
    }

    // Spread a small amount of pressure around the exact footstep so paths widen naturally.
    private void spreadFootPressure(ServerLevel level, BlockPos pos, int amount) {
        int sidePressure = Math.max(1, amount / 2);
        int sideSteps = amount >= 4 ? 2 : 1;

        for (int i = 0; i < sideSteps; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom());
            BlockPos sidePos = pos.relative(direction);

            if (level.getRandom().nextInt(amount >= 3 ? 2 : 3) == 0) {
                applyFootPressure(level, sidePos, sidePressure, false);
            }

            if (amount >= 3 && level.getRandom().nextInt(4) == 0) {
                Direction secondDirection = Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom());
                applyFootPressure(level, sidePos.relative(secondDirection), 1, false);
            }
        }
    }

    private void applyFootPressure(ServerLevel level, BlockPos pos, int amount, boolean emitFeedback) {
        BlockState state = level.getBlockState(pos);

        if (!canWear(state)) {
            return;
        }

        GrassWearSavedData data = grassData(level);
        int pressure = data.addPressure(pos, amount, level.getGameTime());

        if (pressure >= WEAR_PRESSURE_THRESHOLD) {
            data.resetPressure(pos, level.getGameTime());
            BlockState wornState = nextWornState(level, state);

            if (wornState != null && wornState != state) {
                level.setBlock(pos, wornState, Block.UPDATE_ALL);

                if (emitFeedback) {
                    playWearFeedback(level, pos, wornState, SoundEvents.GRASS_HIT, 0.45F);
                }

                if (reachedFinalWearStep(state, wornState)) {
                    startWearOnAdjacentGrass(level, pos);
                }
            }
        }

        data.trim(MAX_TRACKED_PRESSURE, 512);
    }

    private void slowlyRegrowNearbyGrass(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.randomBetweenClosed(
                level.getRandom(),
                12,
                center.getX() - REGROW_SCAN_RADIUS,
                center.getY() - 1,
                center.getZ() - REGROW_SCAN_RADIUS,
                center.getX() + REGROW_SCAN_RADIUS,
                center.getY() + 1,
                center.getZ() + REGROW_SCAN_RADIUS
        )) {
            BlockState state = level.getBlockState(pos);

            if (!state.is(ModBlocks.WORN_GRASS.get()) || !canNaturallyRegrow(level, pos, state)) {
                continue;
            }

            BlockState regrownState = previousRegrowthState(state);

            if (regrownState != null) {
                level.setBlock(pos, regrownState, Block.UPDATE_ALL);

                if (regrownState.is(Blocks.GRASS_BLOCK)) {
                    grassData(level).removePressure(pos);
                }
            }
        }
    }

    private boolean canNaturallyRegrow(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return false;
        }

        GrassWearSavedData data = grassData(level);
        int wear = state.getValue(WornGrassBlock.WEAR);
        int chance = BASE_REGROW_CHANCE + wear * 4 + data.pressureAt(pos);

        if (data.wasTouchedRecently(pos, level.getGameTime(), RECENT_USE_WINDOW_TICKS)) {
            chance += RECENT_USE_REGROW_PENALTY;
        }

        if (level.isRainingAt(pos.above()) || hasNearbyWater(level, pos)) {
            chance = Math.max(4, chance - 4);
        }

        return level.getRandom().nextInt(chance) == 0;
    }

    private boolean hasNearbyWater(Level level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (level.getFluidState(nearbyPos).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    private static boolean canWear(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(ModBlocks.WORN_GRASS.get());
    }

    private static BlockState nextWornState(ServerLevel level, BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK)) {
            return initialWornState(level, 1);
        }

        if (state.is(ModBlocks.WORN_GRASS.get())) {
            int wear = state.getValue(WornGrassBlock.WEAR);
            return state.setValue(WornGrassBlock.WEAR, Math.min(5, wear + 1));
        }

        return null;
    }

    private static BlockState shovelFlattenedState(ServerLevel level, BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK)) {
            return initialWornState(level, 5);
        }

        if (state.is(ModBlocks.WORN_GRASS.get())) {
            int wear = state.getValue(WornGrassBlock.WEAR);
            return state.setValue(WornGrassBlock.WEAR, Math.min(5, wear + 2));
        }

        return null;
    }

    // Keep every freshly damaged block visually stable by choosing a variant only once.
    private static BlockState initialWornState(ServerLevel level, int wear) {
        return ModBlocks.WORN_GRASS.get().defaultBlockState()
                .setValue(WornGrassBlock.WEAR, wear)
                .setValue(WornGrassBlock.VARIANT, level.getRandom().nextInt(WORN_GRASS_VARIANTS));
    }

    private static boolean reachedFinalWearStep(BlockState previousState, BlockState newState) {
        return newState.is(ModBlocks.WORN_GRASS.get())
                && newState.getValue(WornGrassBlock.WEAR) == 5
                && (!previousState.is(ModBlocks.WORN_GRASS.get())
                || previousState.getValue(WornGrassBlock.WEAR) < 5);
    }

    private void startWearOnAdjacentGrass(ServerLevel level, BlockPos pos) {
        List<BlockPos> candidates = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);

            if (level.getBlockState(neighborPos).is(Blocks.GRASS_BLOCK)) {
                candidates.add(neighborPos.immutable());
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        BlockPos chosenPos = candidates.get(level.getRandom().nextInt(candidates.size()));
        level.setBlock(chosenPos, initialWornState(level, 1), Block.UPDATE_ALL);
        grassData(level).removePressure(chosenPos);
        playWearFeedback(level, chosenPos, level.getBlockState(chosenPos), SoundEvents.GRASS_HIT, 0.35F);
    }

    private static BlockState previousRegrowthState(BlockState state) {
        if (state.is(ModBlocks.WORN_GRASS.get())) {
            int wear = state.getValue(WornGrassBlock.WEAR);

            if (wear <= 1) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }

            return state.setValue(WornGrassBlock.WEAR, wear - 1);
        }

        return null;
    }

    private void playWearFeedback(ServerLevel level, BlockPos pos, BlockState state, SoundEvent sound, float volume) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, 0.85F + level.getRandom().nextFloat() * 0.25F);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5D,
                pos.getY() + 1.02D,
                pos.getZ() + 0.5D,
                5,
                0.25D,
                0.03D,
                0.25D,
                0.015D
        );
    }

    private void damageTool(ItemStack itemStack, ServerLevel level, Player player, boolean mainHand) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.isCreative()) {
            return;
        }

        EquipmentSlot slot = mainHand ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        itemStack.hurtAndBreak(1, level, serverPlayer, item -> serverPlayer.onEquippedItemBroken(item, slot));
    }

    // Prevent long-running worlds from keeping old entity UUIDs forever.
    private void trimTrackedEntitySteps() {
        if (lastWalkedBlocks.size() <= MAX_TRACKED_ENTITY_STEPS) {
            return;
        }

        int removed = 0;
        var iterator = lastWalkedBlocks.keySet().iterator();

        while (iterator.hasNext() && removed < 512) {
            iterator.next();
            iterator.remove();
            removed++;
        }
    }

    private GrassWearSavedData grassData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GrassWearSavedData.TYPE);
    }
}
