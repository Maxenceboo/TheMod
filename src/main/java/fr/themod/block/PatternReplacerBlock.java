package fr.themod.block;

import com.mojang.serialization.MapCodec;
import fr.themod.block.entity.PatternReplacerBlockEntity;
import fr.themod.registry.ModBlockEntities;
import fr.themod.screen.PatternReplacerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PatternReplacerBlock extends BaseEntityBlock {
    public static final MapCodec<PatternReplacerBlock> CODEC = simpleCodec(PatternReplacerBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public PatternReplacerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PatternReplacerBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntities.PATTERN_REPLACER.get(),
                PatternReplacerBlockEntity::tick
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                activate(level, pos, player);
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        if (!level.isClientSide()) {
            openMenu(level, pos, player);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                activate(level, pos, player);
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        if (!level.isClientSide()) {
            openMenu(level, pos, player);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private static void openMenu(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof PatternReplacerBlockEntity blockEntity)) {
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, menuPlayer) -> new PatternReplacerMenu(
                        containerId,
                        playerInventory,
                        blockEntity,
                        ContainerLevelAccess.create(level, pos)
                ),
                Component.translatable("block.themod.pattern_replacer")
        ));
    }

    private static void activate(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof PatternReplacerBlockEntity blockEntity) {
            blockEntity.activate(player);
        }
    }
}
