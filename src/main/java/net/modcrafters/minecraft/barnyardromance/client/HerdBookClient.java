package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.herd.HerdBookPayloads;
import net.minecraft.client.Minecraft;

// Receives the herd book from the server: opens it, or refreshes the one already open (see BookArrival).
public final class HerdBookClient {
    private HerdBookClient() {
    }

    public static void show(HerdBookPayloads.ShowBook book) {
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=26.1 {
        HerdBookScreen open = minecraft.gui.screen() instanceof HerdBookScreen screen ? screen : null;
        //?} else
        //HerdBookScreen open = minecraft.screen instanceof HerdBookScreen screen ? screen : null;
        switch (BookArrival.of(open != null, book.open())) {
            case REFRESH -> {
                open.update(book);
                book.focus().ifPresent(open::showAnimal);
            }
            case OPEN -> {
                HerdBookScreen opened = new HerdBookScreen(book);
                //? if >=26.1 {
                minecraft.setScreenAndShow(opened);
                //?} else
                //minecraft.setScreen(opened);
                // After the screen is set up, so its buttons exist when it turns to the page.
                book.focus().ifPresent(opened::showAnimal);
            }
            case DROP -> {
            }
        }
    }
}
