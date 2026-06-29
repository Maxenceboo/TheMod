package fr.themod.registry;

import com.mojang.serialization.MapCodec;
import fr.themod.TheMod;
import fr.themod.recipe.InfusedGemRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TheMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusedGemRecipe>> INFUSED_GEM =
            RECIPE_SERIALIZERS.register(
                    "infused_gem",
                    () -> new RecipeSerializer<>(
                            MapCodec.unit(InfusedGemRecipe::new),
                            StreamCodec.unit(new InfusedGemRecipe()).cast()
                    )
            );

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
