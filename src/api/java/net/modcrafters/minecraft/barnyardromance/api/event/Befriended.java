package net.modcrafters.minecraft.barnyardromance.api.event;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

// A player's feeding brought an animal to the familiar mark for the first time. Its familiarity is already saved; to
// stop it, cancel the FamiliarityChange instead.
public record Befriended(Animal animal, Player player) {
}
