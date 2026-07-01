package fr.themod.registry;

import fr.themod.TheMod;
import fr.themod.item.InfusedGemItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheMod.MODID);

    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            "example_block",
            ModBlocks.EXAMPLE_BLOCK
    );

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem(
            "example_item",
            properties -> properties.food(new FoodProperties.Builder()
                    .alwaysEdible()
                    .nutrition(1)
                    .saturationModifier(2f)
                    .build())
    );

    public static final DeferredItem<Item> INFUSED_GEM = ITEMS.registerItem(
            "infused_gem",
            properties -> new InfusedGemItem(properties.stacksTo(1))
    );

    public static final DeferredItem<BlockItem> REPLACEMENT_MARKER_ITEM = ITEMS.registerSimpleBlockItem(
            "replacement_marker",
            ModBlocks.REPLACEMENT_MARKER
    );

    public static final DeferredItem<BlockItem> PATTERN_REPLACER_ITEM = ITEMS.registerSimpleBlockItem(
            "pattern_replacer",
            ModBlocks.PATTERN_REPLACER
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
