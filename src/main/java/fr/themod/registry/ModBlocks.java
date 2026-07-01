package fr.themod.registry;

import fr.themod.TheMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import fr.themod.block.PatternReplacerBlock;
import fr.themod.block.ReplacementMarkerBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheMod.MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
            "example_block",
            properties -> properties.mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<Block> REPLACEMENT_MARKER = BLOCKS.registerBlock(
            "replacement_marker",
            ReplacementMarkerBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_PURPLE).strength(0.4f)
    );

    public static final DeferredBlock<Block> PATTERN_REPLACER = BLOCKS.registerBlock(
            "pattern_replacer",
            PatternReplacerBlock::new,
            properties -> properties.mapColor(MapColor.STONE).strength(3.0f)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
