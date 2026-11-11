package net.modcrafters.minecraft.barnyardromance.rivalry.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;

// What other features ask of courtship, fights, and the path searches that keep rivals in separate pens apart.
public interface RivalryApi {
    // Whether she chose him: only the male she chose may mate with her.
    boolean chosen(Animal female, Animal male);

    // The breeding rest after mating: none for a male, whose Vigour is his only limit; rest for anyone else.
    int restAfterMating(Animal animal, int rest);

    // Whether the animal's last hurt was a rival's in a fight.
    boolean killedByRival(Animal animal);

    // Whether the animal can walk to the block, within range: a fence or a wall in between says no.
    boolean canWalkTo(Animal from, BlockPos to, int range);

    // The same, but a search that never got anywhere says UNKNOWN rather than NO.
    Walk walkTo(Animal from, BlockPos to, int range);

    // For a moment that can't wait for a better one: only a path that is found and falls short rules the walk out.
    boolean walkBlocked(Animal from, Animal to, int range);
}
