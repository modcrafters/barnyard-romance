package net.modcrafters.minecraft.barnyardromance.core.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

// Something that happened to an animal, announced by the feature it happened in (see Events). Not sealed, so tests can
// post events of their own.
public interface AnimalEvent {
    // A mother fell pregnant; breeder is the player who put the pair in love, if anyone did.
    record Conceived(Animal mother, @Nullable UUID breeder) implements AnimalEvent {
    }

    // Young born to a mother, at the end of a pregnancy or straight from vanilla breeding.
    record Born(Animal mother, List<Animal> young, @Nullable UUID breeder, Birth birth) implements AnimalEvent {
    }

    enum Birth {
        // At the end of a pregnancy.
        LIVE,
        // From vanilla breeding while pregnancy is off: conceived and born at once.
        VANILLA
    }

    // A chick out of an egg. parents is null for an egg that forgot them (a wild egg) or never kept them.
    record Hatched(ServerLevel level, Animal chick, @Nullable Lineage parents, Hatch hatch) implements AnimalEvent {
    }

    enum Hatch {
        // A fertilized egg in a nesting box.
        NEST,
        // An egg that was thrown.
        THROWN,
        // A wild egg in a nesting box.
        WILD,
        // One of Naturalist's own egg blocks.
        NATURALIST_EGG
    }

    // A hand meal made a wild animal familiar.
    record Befriended(Animal animal, Player player) implements AnimalEvent {
    }

    // A familiar animal was milked.
    record Milked(Animal animal, Player player) implements AnimalEvent {
    }

    // A familiar animal was sheared; shearer is the player, if a player did it.
    record Sheared(Animal animal, @Nullable UUID shearer) implements AnimalEvent {
    }

    // A player wrote the animal into a herd book.
    record AddedToHerdBook(Animal animal, Player player) implements AnimalEvent {
    }

    record Died(Animal animal, DamageSource source) implements AnimalEvent {
    }

    // The animal ate at a feeding block.
    record FeederMeal(Animal animal) implements AnimalEvent {
    }

    // Two animals started a fight.
    record FightStarted(Animal a, Animal b) implements AnimalEvent {
    }

    // The animal's familiarity was settled from before to after (it may not have fallen at all).
    record FamiliarityFell(Animal animal, double before, double after) implements AnimalEvent {
    }
}
