package fr.themod.registry;

import fr.themod.TheMod;
import fr.themod.screen.PatternReplacerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TheMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PatternReplacerMenu>> PATTERN_REPLACER =
            MENUS.register(
                    "pattern_replacer",
                    () -> new MenuType<>(PatternReplacerMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}