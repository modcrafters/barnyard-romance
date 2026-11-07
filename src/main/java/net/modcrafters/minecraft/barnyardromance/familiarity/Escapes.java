package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

// A neglected animal running off. For now it only remembers that it ran away; leaving the pen comes later.
public final class Escapes {
    private Escapes() {
    }

    // catchingUp is true when the escape was worked out for days spent away, with no one there to see it go.
    public static void runAway(Animal animal, ServerLevel level, boolean catchingUp) {
        Services.DATA.get(animal).ifPresent(traits -> Services.DATA.set(animal, traits.withUpkeep(traits.upkeep().runAway())));
    }
}
