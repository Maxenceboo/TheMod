package fr.themod.block.entity;

import fr.themod.registry.ModBlockEntities;
import fr.themod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import fr.themod.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class PatternReplacerBlockEntity extends BlockEntity implements Container {
    public static final int PALETTE_START = 0;
    public static final int PALETTE_END = 8;
    public static final int FUEL_SLOT = 9;
    public static final int SLOT_COUNT = 10;
    private static final int MAX_MARKERS = 2048;

    private int outlineTicks;
    private UUID outlineViewer;
    private int placeDelay;
    private boolean replacing;

    private final Queue<BlockPos> pendingMarkers = new ArrayDeque<>();
    private final List<BlockPos> selectedMarkers = new ArrayList<>();
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public PatternReplacerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PATTERN_REPLACER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PatternReplacerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        if (blockEntity.outlineTicks > 0) {
            blockEntity.spawnSelectionParticles(level);
            blockEntity.outlineTicks--;
        }

        if (blockEntity.replacing) {
            blockEntity.tickReplacement(level);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(this.getMaxStackSize(stack));
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= PALETTE_START && slot <= PALETTE_END) {
            return stack.getItem() instanceof BlockItem
                    && stack.getItem() != ModItems.REPLACEMENT_MARKER_ITEM.get()
                    && stack.getItem() != ModItems.PATTERN_REPLACER_ITEM.get();
        }

        if (slot == FUEL_SLOT) {
            return stack.getItem() == Items.COAL || stack.getItem() == Items.CHARCOAL;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void onMenuClosed(Player player) {
        Level level = this.getLevel();

        if (level == null || level.isClientSide()) {
            return;
        }

        this.selectedMarkers.clear();
        this.selectedMarkers.addAll(this.detectConnectedMarkers(level));
        this.pendingMarkers.clear();
        this.pendingMarkers.addAll(this.selectedMarkers);
        this.outlineTicks = 20 * 5;
        this.outlineViewer = player.getUUID();
        this.placeDelay = 0;
        this.replacing = !this.pendingMarkers.isEmpty();
        this.setChanged();

        player.sendSystemMessage(Component.literal(
                "Markers detectes: " + this.selectedMarkers.size()
        ));
    }

    private List<BlockPos> detectConnectedMarkers(Level level) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        // On commence par les 6 blocs directement colles au Pattern Replacer.
        for (Direction direction : Direction.values()) {
            BlockPos nextPos = this.worldPosition.relative(direction);

            if (isReplacementMarker(level, nextPos)) {
                queue.add(nextPos);
                visited.add(nextPos);
            }
        }

        // Puis on suit tous les markers connectes entre eux, sans diagonales.
        while (!queue.isEmpty() && visited.size() < MAX_MARKERS) {
            BlockPos currentPos = queue.remove();

            for (Direction direction : Direction.values()) {
                BlockPos nextPos = currentPos.relative(direction);

                if (!visited.contains(nextPos) && isReplacementMarker(level, nextPos)) {
                    queue.add(nextPos);
                    visited.add(nextPos);
                }
            }
        }

        return new ArrayList<>(visited);
    }

    private static boolean isReplacementMarker(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.REPLACEMENT_MARKER.get());
    }

    private void tickReplacement(Level level) {
        if (this.placeDelay > 0) {
            this.placeDelay--;
            return;
        }

        if (!this.hasFuel()) {
            this.replacing = false;
            this.setChanged();
            return;
        }

        int paletteSlot = this.findRandomPaletteSlot(level);

        if (paletteSlot == -1) {
            this.replacing = false;
            this.setChanged();
            return;
        }

        while (!this.pendingMarkers.isEmpty()) {
            BlockPos markerPos = this.pendingMarkers.remove();

            if (!isReplacementMarker(level, markerPos)) {
                continue;
            }

            ItemStack paletteStack = this.items.get(paletteSlot);
            BlockItem blockItem = (BlockItem) paletteStack.getItem();
            BlockState placedState = blockItem.getBlock().defaultBlockState();

            level.setBlock(markerPos, placedState, Block.UPDATE_ALL);
            paletteStack.shrink(1);
            this.consumeFuelOnce();
            this.placeDelay = calculatePlaceDelay(level, markerPos, placedState);
            this.setChanged();
            return;
        }

        this.replacing = false;
        this.setChanged();
    }

    private boolean hasFuel() {
        ItemStack fuelStack = this.items.get(FUEL_SLOT);
        return fuelStack.getItem() == Items.COAL || fuelStack.getItem() == Items.CHARCOAL;
    }

    private void consumeFuelOnce() {
        this.items.get(FUEL_SLOT).shrink(1);
    }

    private int findRandomPaletteSlot(Level level) {
        List<Integer> availableSlots = new ArrayList<>();

        for (int slot = PALETTE_START; slot <= PALETTE_END; slot++) {
            ItemStack stack = this.items.get(slot);

            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                availableSlots.add(slot);
            }
        }

        if (availableSlots.isEmpty()) {
            return -1;
        }

        return availableSlots.get(level.getRandom().nextInt(availableSlots.size()));
    }

    private static int calculatePlaceDelay(Level level, BlockPos pos, BlockState placedState) {
        float destroySpeed = placedState.getDestroySpeed(level, pos);

        if (destroySpeed < 0) {
            return 20 * 30;
        }

        return Math.max(4, (int) Math.ceil(destroySpeed * 20.0f));
    }

    private void spawnSelectionParticles(Level level) {
        if (!(level instanceof ServerLevel serverLevel)
                || this.selectedMarkers.isEmpty()
                || this.outlineViewer == null) {
            return;
        }

        ServerPlayer viewer = serverLevel.getServer().getPlayerList().getPlayer(this.outlineViewer);

        if (viewer == null) {
            return;
        }

        Set<BlockPos> markerSet = new HashSet<>(this.selectedMarkers);

        for (BlockPos markerPos : this.selectedMarkers) {
            drawMarkerOutline(viewer, markerPos, markerSet);
        }
    }

    private static void drawMarkerOutline(ServerPlayer viewer, BlockPos pos, Set<BlockPos> markerSet) {
        for (Direction face : Direction.values()) {
            if (!markerSet.contains(pos.relative(face))) {
                drawExposedFaceEdges(viewer, pos, face, markerSet);
            }
        }
    }

    private static void drawExposedFaceEdges(
            ServerPlayer viewer,
            BlockPos pos,
            Direction face,
            Set<BlockPos> markerSet
    ) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        switch (face) {
            case UP -> {
                double yy = y + 1;
                if (!markerSet.contains(pos.relative(Direction.NORTH))) drawLine(viewer, x, yy, z, x + 1, yy, z);
                if (!markerSet.contains(pos.relative(Direction.SOUTH))) drawLine(viewer, x, yy, z + 1, x + 1, yy, z + 1);
                if (!markerSet.contains(pos.relative(Direction.WEST))) drawLine(viewer, x, yy, z, x, yy, z + 1);
                if (!markerSet.contains(pos.relative(Direction.EAST))) drawLine(viewer, x + 1, yy, z, x + 1, yy, z + 1);
            }
            case DOWN -> {
                double yy = y;
                if (!markerSet.contains(pos.relative(Direction.NORTH))) drawLine(viewer, x, yy, z, x + 1, yy, z);
                if (!markerSet.contains(pos.relative(Direction.SOUTH))) drawLine(viewer, x, yy, z + 1, x + 1, yy, z + 1);
                if (!markerSet.contains(pos.relative(Direction.WEST))) drawLine(viewer, x, yy, z, x, yy, z + 1);
                if (!markerSet.contains(pos.relative(Direction.EAST))) drawLine(viewer, x + 1, yy, z, x + 1, yy, z + 1);
            }
            case NORTH -> {
                double zz = z;
                if (!markerSet.contains(pos.relative(Direction.UP))) drawLine(viewer, x, y + 1, zz, x + 1, y + 1, zz);
                if (!markerSet.contains(pos.relative(Direction.DOWN))) drawLine(viewer, x, y, zz, x + 1, y, zz);
                if (!markerSet.contains(pos.relative(Direction.WEST))) drawLine(viewer, x, y, zz, x, y + 1, zz);
                if (!markerSet.contains(pos.relative(Direction.EAST))) drawLine(viewer, x + 1, y, zz, x + 1, y + 1, zz);
            }
            case SOUTH -> {
                double zz = z + 1;
                if (!markerSet.contains(pos.relative(Direction.UP))) drawLine(viewer, x, y + 1, zz, x + 1, y + 1, zz);
                if (!markerSet.contains(pos.relative(Direction.DOWN))) drawLine(viewer, x, y, zz, x + 1, y, zz);
                if (!markerSet.contains(pos.relative(Direction.WEST))) drawLine(viewer, x, y, zz, x, y + 1, zz);
                if (!markerSet.contains(pos.relative(Direction.EAST))) drawLine(viewer, x + 1, y, zz, x + 1, y + 1, zz);
            }
            case WEST -> {
                double xx = x;
                if (!markerSet.contains(pos.relative(Direction.UP))) drawLine(viewer, xx, y + 1, z, xx, y + 1, z + 1);
                if (!markerSet.contains(pos.relative(Direction.DOWN))) drawLine(viewer, xx, y, z, xx, y, z + 1);
                if (!markerSet.contains(pos.relative(Direction.NORTH))) drawLine(viewer, xx, y, z, xx, y + 1, z);
                if (!markerSet.contains(pos.relative(Direction.SOUTH))) drawLine(viewer, xx, y, z + 1, xx, y + 1, z + 1);
            }
            case EAST -> {
                double xx = x + 1;
                if (!markerSet.contains(pos.relative(Direction.UP))) drawLine(viewer, xx, y + 1, z, xx, y + 1, z + 1);
                if (!markerSet.contains(pos.relative(Direction.DOWN))) drawLine(viewer, xx, y, z, xx, y, z + 1);
                if (!markerSet.contains(pos.relative(Direction.NORTH))) drawLine(viewer, xx, y, z, xx, y + 1, z);
                if (!markerSet.contains(pos.relative(Direction.SOUTH))) drawLine(viewer, xx, y, z + 1, xx, y + 1, z + 1);
            }
        }
    }

    private static void drawLine(
            ServerPlayer viewer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        double step = 0.25;
        double distance = Math.sqrt(
                Math.pow(x2 - x1, 2)
                        + Math.pow(y2 - y1, 2)
                        + Math.pow(z2 - z1, 2)
        );

        int steps = Math.max(1, (int) Math.ceil(distance / step));

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double x = x1 + (x2 - x1) * progress;
            double y = y1 + (y2 - y1) * progress;
            double z = z1 + (z2 - z1) * progress;

            viewer.level().sendParticles(
                    viewer,
                    ParticleTypes.END_ROD,
                    true,
                    false,
                    x,
                    y,
                    z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
}
