package net.modcrafters.minecraft.barnyardromance.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// The 1.21.1 copy of src/.../compat/VersionCompat.
public final class VersionCompat {
    // SUCCESS returned on the server swings the player's arm for everyone, like SUCCESS_SERVER later.
    public static final InteractionResult SUCCESS_SERVER = InteractionResult.SUCCESS;

    private VersionCompat() {
    }

    public static void sendOverlay(Player player, Component message) {
        player.displayClientMessage(message, true);
    }

    // There's no shared eating sound here (the AnimalEating invoker has no target, so it isn't applied and must not
    // be loaded). Each animal plays its own inside its vanilla feeding code instead, so this plays that sound.
    public static void playEatingSound(Animal animal, ItemStack food) {
        SoundEvent sound = eatingSound(animal, food);
        if (sound != null) {
            animal.playSound(sound, 1.0F, 1.0F);
        }
    }

    // The animals that make an eating sound on 26.3 and have one here; pigs only got theirs later.
    public static @Nullable SoundEvent eatingSound(Animal animal, ItemStack food) {
        if (animal instanceof Cat) {
            return SoundEvents.CAT_EAT;
        }
        if (animal instanceof Frog) {
            return SoundEvents.FROG_EAT;
        }
        if (animal instanceof Goat || animal instanceof Fox || animal instanceof Sniffer || animal instanceof Armadillo) {
            return animal.getEatingSound(food);
        }
        return null;
    }
}
