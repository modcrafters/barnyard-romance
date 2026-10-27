package net.modcrafters.minecraft.barnyardromance.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

// Minecraft calls that are used in many places and look different on older versions. versions/1.21.1 has
// the same class for 1.21.1, so callers stay free of version comments.
public final class VersionCompat {
    // Succeeds on the server, which swings the player's arm for everyone.
    public static final InteractionResult SUCCESS_SERVER = InteractionResult.SUCCESS_SERVER;

    private VersionCompat() {
    }

    // The action bar text above the hotbar.
    public static void sendOverlay(Player player, Component message) {
        player.sendOverlayMessage(message);
    }
}
