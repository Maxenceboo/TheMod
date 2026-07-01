package fr.themod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

public class WornGrassBlock extends Block {
    public static final MapCodec<WornGrassBlock> CODEC = simpleCodec(WornGrassBlock::new);
    public static final IntegerProperty WEAR = IntegerProperty.create("wear", 1, 5);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 3);

    public WornGrassBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.DIRT)
                .strength(0.6F)
                .sound(SoundType.GRASS)
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WEAR, 1)
                .setValue(VARIANT, 0)
        );
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WEAR, VARIANT);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return new ItemStack(Blocks.GRASS_BLOCK);
    }
}
