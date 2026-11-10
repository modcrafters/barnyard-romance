package net.modcrafters.minecraft.barnyardromance.compat;

import net.modcrafters.minecraft.barnyardromance.mixin.AnimalEating;
import net.modcrafters.minecraft.barnyardromance.mixin.AnimalLoveCauseAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

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

    // The player a tamed animal belongs to, online or not.
    public static @Nullable UUID ownerId(Animal animal) {
        if (!(animal instanceof OwnableEntity owned)) {
            return null;
        }
        EntityReference<LivingEntity> owner = owned.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    // The player who put an animal in love, online or not, wherever they are.
    public static @Nullable UUID loveCauseId(Animal animal) {
        EntityReference<ServerPlayer> cause = ((AnimalLoveCauseAccessor) animal).barnyardromance$loveCause();
        return cause == null ? null : cause.getUUID();
    }

    // The animal's own eating sound, the one vanilla plays when it feeds the animal (most animals have none).
    // The food only matters on 1.21.1, where some animals pick their sound by it.
    public static void playEatingSound(Animal animal, ItemStack food) {
        if (animal instanceof AnimalEating eating) {
            eating.barnyardromance$playEatingSound();
        }
    }
}
