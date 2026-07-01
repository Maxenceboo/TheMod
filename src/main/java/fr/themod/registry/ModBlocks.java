package fr.themod.registry;

import fr.themod.block.GrassySandBlock;
import fr.themod.TheMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheMod.MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
            "example_block",
            properties -> properties.mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<GrassySandBlock> GRASSY_SAND_BLOCK = BLOCKS.registerBlock(
            "grassy_sand_block",
            GrassySandBlock::new
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
