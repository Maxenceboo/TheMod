package fr.maxence.maxencemod.client;

import fr.maxence.maxencemod.MaxenceMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MaxenceMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MaxenceMod.MODID, value = Dist.CLIENT)
public class MaxenceModClient {
    public MaxenceModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MaxenceMod.LOGGER.info("Maxence Mod client setup");
        MaxenceMod.LOGGER.info("Minecraft user: {}", Minecraft.getInstance().getUser().getName());
    }
}
