package fr.themod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class GrassySandBlock extends FallingBlock {
    public static final MapCodec<GrassySandBlock> CODEC = simpleCodec(GrassySandBlock::new);

    public GrassySandBlock(BlockBehaviour.Properties properties) {
        super(properties
                .strength(0.5F)
                .sound(SoundType.SAND)
                .instrument(NoteBlockInstrument.SNARE)
                .destroyTime(0.5F)
        );
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0xCDD828;
    }
}
