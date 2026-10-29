package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.minecraft.client.Minecraft;

// Receives the herd book from the server: opens it, or refreshes the one already open.
public final class HerdBookClient {
    private HerdBookClient() {
    }

    public static void show(HerdBookPayloads.ShowBook book) {
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=26.1 {
        if (minecraft.gui.screen() instanceof HerdBookScreen open) {
        //?} else
        //if (minecraft.screen instanceof HerdBookScreen open) {
            open.update(book);
            return;
        }
        //? if >=26.1 {
        minecraft.setScreenAndShow(new HerdBookScreen(book));
        //?} else
        //minecraft.setScreen(new HerdBookScreen(book));
    }
}
