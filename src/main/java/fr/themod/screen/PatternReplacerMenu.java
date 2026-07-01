package fr.themod.screen;

import fr.themod.registry.ModBlocks;
import fr.themod.registry.ModMenuTypes;
import fr.themod.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import fr.themod.block.entity.PatternReplacerBlockEntity;

public class PatternReplacerMenu extends AbstractContainerMenu {
    private static final int CONTAINER_SLOT_COUNT = 10;

    private final Container container;
    private final ContainerLevelAccess access;

    public PatternReplacerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    public PatternReplacerMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerLevelAccess access
    ) {
        super(ModMenuTypes.PATTERN_REPLACER.get(), containerId);
        checkContainerSize(container, CONTAINER_SLOT_COUNT);

        this.container = container;
        this.access = access;

        container.startOpen(playerInventory.player);

        // Slots 0-8 : palette de blocs.
        for (int slot = 0; slot < 9; slot++) {
            int x = 8 + slot * 18;
            this.addSlot(new Slot(container, slot, x, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof BlockItem
                            && stack.getItem() != ModItems.REPLACEMENT_MARKER_ITEM.get()
                            && stack.getItem() != ModItems.PATTERN_REPLACER_ITEM.get();
                }
            });
        }

        // Slot 9 : carburant.
        this.addSlot(new Slot(container, 9, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.COAL || stack.getItem() == Items.CHARCOAL;
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < CONTAINER_SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, CONTAINER_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() == Items.COAL || stack.getItem() == Items.CHARCOAL) {
                if (!this.moveItemStackTo(stack, 9, 10, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof BlockItem
                    && stack.getItem() != ModItems.REPLACEMENT_MARKER_ITEM.get()
                    && stack.getItem() != ModItems.PATTERN_REPLACER_ITEM.get()) {
                if (!this.moveItemStackTo(stack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.PATTERN_REPLACER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);

        if (this.container instanceof PatternReplacerBlockEntity blockEntity) {
            blockEntity.onMenuClosed(player);
        }
    }
}