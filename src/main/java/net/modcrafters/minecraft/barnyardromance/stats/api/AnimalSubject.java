package net.modcrafters.minecraft.barnyardromance.stats.api;

import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.herd.api.Familiar;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// What the stat sheet is filled from: an animal loaded right now, or the herd book's record of one. StatsApi.facts
// works out every row the same way from either, from what only the source can say.
public interface AnimalSubject {
    String species();

    // For a record of an animal far from home, as they'd be after the meals it has missed.
    AnimalTraits traits();

    AgeStage stage();

    boolean baby();

    boolean alive();

    // A wolf, cat or one of the horse family, which only breed when tamed.
    boolean needsTaming();

    boolean tamed();

    // The game time the animal is shown at: now, or when it died.
    long shownAt();

    float health();

    float maxHealth();

    @Nullable Pregnancy pregnancy();

    Optional<MilkStatus> milk();

    ShearState shear();

    Familiar familiar();

    // The most familiar it can become.
    double cap();

    // Its species lays eggs, so a grown one has an egg row.
    boolean laysEggs();

    boolean layingHen();

    // Its laying as last seen; only one from today counts.
    Laying laying();

    // What a hen lays tomorrow, as far as the herd book can tell; null for a loaded animal, whose card doesn't say.
    default @Nullable EggOutlook eggsTomorrow() {
        return null;
    }

    // How long ago the herd book last saw it; -1 for a loaded animal.
    default long seenTicksAgo() {
        return -1L;
    }

    // -1 while alive.
    default long diedDay() {
        return -1L;
    }

    default boolean killedByRival() {
        return false;
    }
}
