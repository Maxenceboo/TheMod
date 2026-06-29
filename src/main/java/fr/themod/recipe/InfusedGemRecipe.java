package fr.themod.recipe;

import fr.themod.registry.ModItems;
import fr.themod.registry.ModRecipeSerializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class InfusedGemRecipe extends CustomRecipe {
    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findPotion(input) != null
                && countItem(input, Items.AMETHYST_SHARD) == 1
                && countItem(input, Items.REDSTONE) == 1
                && input.ingredientCount() == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack potion = findPotion(input);

        if (potion == null) {
            return ItemStack.EMPTY;
        }

        PotionContents potionContents = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        ItemStack result = new ItemStack(ModItems.INFUSED_GEM.get());
        result.set(DataComponents.POTION_CONTENTS, potionContents);
        result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.INFUSED_GEM.get();
    }

    private static ItemStack findPotion(CraftingInput input) {
        ItemStack foundPotion = null;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);

            if (stack.getItem() == Items.POTION) {
                PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

                if (!potionContents.hasEffects() || foundPotion != null) {
                    return null;
                }

                foundPotion = stack;
            }
        }

        return foundPotion;
    }

    private static int countItem(CraftingInput input, net.minecraft.world.item.Item item) {
        int count = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            if (input.getItem(slot).getItem() == item) {
                count++;
            }
        }

        return count;
    }
}
