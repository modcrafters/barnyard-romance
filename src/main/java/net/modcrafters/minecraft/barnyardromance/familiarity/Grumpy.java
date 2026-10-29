package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

// How a grumpy pet shows it is refusing: an angry cloud over its head and a line on the player's action bar.
public final class Grumpy {
    public static final String WONT_RIDE = "message.barnyard_romance.grumpy.wont_ride";
    public static final String WONT_SIT = "message.barnyard_romance.grumpy.wont_sit";

    private Grumpy() {
    }

    public static void refuse(Animal animal, Player player, String key) {
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight(), animal.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        VersionCompat.sendOverlay(player, Component.translatable(key, animal.getName()));
    }
}
