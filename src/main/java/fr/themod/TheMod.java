package fr.themod;

import com.mojang.logging.LogUtils;
import fr.themod.config.CommonConfig;
import fr.themod.registry.ModBlocks;
import fr.themod.registry.ModCreativeTabs;
import fr.themod.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(TheMod.MODID)
public class TheMod {
    public static final String MODID = "themod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("TheMod common setup");

        if (CommonConfig.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("Dirt block: {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", CommonConfig.MAGIC_NUMBER_INTRODUCTION.get(), CommonConfig.MAGIC_NUMBER.getAsInt());
        CommonConfig.ITEM_STRINGS.get().forEach(item -> LOGGER.info("Configured item: {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TheMod server starting");
    }
}
