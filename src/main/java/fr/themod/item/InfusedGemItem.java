package fr.themod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class InfusedGemItem extends Item {
    public InfusedGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        if (!potionContents.hasEffects()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            potionContents.applyToLivingEntity(player, 1.0f);
            player.getCooldowns().addCooldown(stack, 20 * 5);
        }

        return InteractionResult.SUCCESS;
    }
}
