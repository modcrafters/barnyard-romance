package net.modcrafters.minecraft.barnyardromance.rivalry;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

// Who gets a female in love. The males who could have her are walked closest first, two at a time: in a species that
// fights, each pair rolls, and a pair that fights is out of it while the walk goes on to the next two. The first male
// left alone, or the closer of a pair that doesn't fight, mates with her. So with three, two fight and the third gets
// her; with four, two pairs may fight and nobody does, for now.
public final class CourtshipRules {
    private CourtshipRules() {
    }

    public record Pair<T>(T a, T b) {
    }

    // rolled is true when a pair rolled for a fight, which happens once per love.
    public record Outcome<T>(@Nullable T mate, List<Pair<T>> fights, boolean rolled) {
    }

    // reachable is a path search, so it's only asked about each male as the walk gets to him, and never past the mate.
    public static <T> Outcome<T> decide(List<T> closestFirst, Predicate<T> reachable, boolean speciesFights, DoubleSupplier roll, double fightChance) {
        Iterator<T> males = closestFirst.stream().filter(reachable).iterator();
        List<Pair<T>> fights = new ArrayList<>();
        boolean rolled = false;
        while (males.hasNext()) {
            T first = males.next();
            if (!speciesFights || !males.hasNext()) {
                return new Outcome<>(first, fights, rolled);
            }
            T second = males.next();
            rolled = true;
            if (roll.getAsDouble() >= fightChance) {
                return new Outcome<>(first, fights, true);
            }
            fights.add(new Pair<>(first, second));
        }
        return new Outcome<>(null, fights, rolled);
    }
}
