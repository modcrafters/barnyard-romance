package net.modcrafters.minecraft.barnyardromance.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

// The 1.21.1 copy of src/.../compat/VersionCompat.
public final class VersionCompat {
    // SUCCESS returned on the server swings the player's arm for everyone, like SUCCESS_SERVER later.
    public static final InteractionResult SUCCESS_SERVER = InteractionResult.SUCCESS;

    private VersionCompat() {
    }

    public static void sendOverlay(Player player, Component message) {
        player.displayClientMessage(message, true);
    }
}
