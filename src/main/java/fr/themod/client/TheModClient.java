package fr.themod.client;

import fr.themod.TheMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TheMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TheMod.MODID, value = Dist.CLIENT)
public class TheModClient {
    public TheModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        TheMod.LOGGER.info("TheMod client setup");
        TheMod.LOGGER.info("Minecraft user: {}", Minecraft.getInstance().getUser().getName());
    }
}
