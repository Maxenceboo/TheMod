package fr.themod.client.screen;

import fr.themod.screen.PatternReplacerMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PatternReplacerScreen extends AbstractContainerScreen<PatternReplacerMenu> {
    public PatternReplacerScreen(PatternReplacerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }
}