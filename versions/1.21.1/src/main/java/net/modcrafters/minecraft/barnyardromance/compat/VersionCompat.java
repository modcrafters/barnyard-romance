package net.modcrafters.minecraft.barnyardromance.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
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

    // No shared eating sound here: each animal plays its own inside its vanilla feeding code, so the AnimalEating
    // invoker has no target and isn't applied (extra_mixins). Loading it would be an error, so nothing touches it.
    public static void playEatingSound(Animal animal) {
    }
}
