package net.modcrafters.minecraft.barnyardromance.rivalry.api;

import net.minecraft.world.entity.animal.Animal;

// What other features ask of courtship and fights.
public interface RivalryApi {
    // Whether she chose him: only the male she chose may mate with her.
    boolean chosen(Animal female, Animal male);

    // The breeding rest after mating: none for a male, whose Vigour is his only limit; rest for anyone else.
    int restAfterMating(Animal animal, int rest);

    // Whether the animal's last hurt was a rival's in a fight.
    boolean killedByRival(Animal animal);
}
