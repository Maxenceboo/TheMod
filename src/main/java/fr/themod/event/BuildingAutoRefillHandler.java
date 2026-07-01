package fr.themod.event;

import fr.themod.config.CommonConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class BuildingAutoRefillHandler {
    private final Map<UUID, PendingRefill> pendingRefills = new HashMap<>();

    @SubscribeEvent
    public void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (!CommonConfig.AUTO_REFILL_BUILDING_STACKS.getAsBoolean()
                || event.getLevel().isClientSide()
                || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();

        // Prepare le rechargement uniquement quand le joueur pose le dernier bloc du stack.
        if (player == null || !(stack.getItem() instanceof BlockItem) || stack.getCount() != 1) {
            return;
        }

        // Le bloc est consomme apres cet event, donc le rechargement se fait au tick suivant.
        pendingRefills.put(player.getUUID(), new PendingRefill(event.getHand(), stack.copyWithCount(1), 2));
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        PendingRefill refill = pendingRefills.get(playerId);

        if (refill == null) {
            return;
        }

        ItemStack heldStack = player.getItemInHand(refill.hand());

        // Si la main est vide apres le placement, on y deplace le prochain stack identique.
        if (heldStack.isEmpty()) {
            refillHand(player, refill);
            pendingRefills.remove(playerId);
            return;
        }

        // Supprime les demandes trop anciennes pour eviter un rechargement accidentel plus tard.
        if (refill.remainingTicks() <= 1) {
            pendingRefills.remove(playerId);
        } else {
            pendingRefills.put(playerId, refill.withRemainingTicks(refill.remainingTicks() - 1));
        }
    }

    private static void refillHand(Player player, PendingRefill refill) {
        Inventory inventory = player.getInventory();
        int handSlot = getHandSlot(inventory, refill.hand());
        int sourceSlot = findMatchingStack(inventory, refill.template(), handSlot);

        if (sourceSlot == Inventory.NOT_FOUND_INDEX) {
            return;
        }

        ItemStack replacement = inventory.getItem(sourceSlot).copy();
        inventory.setItem(handSlot, replacement);
        inventory.setItem(sourceSlot, ItemStack.EMPTY);
        inventory.setChanged();

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(inventory.createInventoryUpdatePacket(handSlot));
            serverPlayer.connection.send(new ClientboundSetPlayerInventoryPacket(sourceSlot, ItemStack.EMPTY));
        }
    }

    private static int getHandSlot(Inventory inventory, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : Inventory.SLOT_OFFHAND;
    }

    private static int findMatchingStack(Inventory inventory, ItemStack template, int ignoredSlot) {
        // Compare aussi les composants pour ne pas melanger des variantes ou des blocs moddes.
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (slot == ignoredSlot) {
                continue;
            }

            ItemStack candidate = inventory.getItem(slot);

            if (!candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, template)) {
                return slot;
            }
        }

        return Inventory.NOT_FOUND_INDEX;
    }

    private record PendingRefill(InteractionHand hand, ItemStack template, int remainingTicks) {
        PendingRefill withRemainingTicks(int value) {
            return new PendingRefill(this.hand, this.template, value);
        }
    }
}
