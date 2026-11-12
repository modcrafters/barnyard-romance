package net.modcrafters.minecraft.barnyardromance.stats.api;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;

import java.util.List;

// What other features ask the stat sheet: its lines for an animal's facts, and the facts and words the herd book
// fills them from for an animal it keeps a record of.
public interface StatsApi {
    // The sheet's lines for the facts, in order; lines that don't apply are left out.
    List<StatLine> lines(AnimalFacts facts);

    // Fed today by hand, or by a feeding block. clock is the overworld clock time.
    boolean fedToday(AnimalTraits traits, long clock);

    // The name of what the animal last bore, or empty.
    String lastOffspringName(AnimalTraits traits);

    // The breeding row's value: ready, in love, resting, worn out, too young, not tamed or not familiar enough.
    Text breedingText(boolean needsTaming, boolean tamed, double familiarity, boolean young, boolean wornOut, long loveUntil,
                      long cooldownUntil, long now);

    // The name of the feeding block that fed it on this GameDay, or empty.
    Text fedByText(Upkeep upkeep, long today);

    // The milk row's value.
    Text milkText(MilkStatus status);

    // The father of a pregnancy, by size letter and species.
    Text fatherText(String sizeLetter, String speciesId);

    // What the animal last bore and how long ago, or empty.
    Text lastBirthText(String offspringName, long lastBirthTick, long now);

    // A male bird's egg row value: whether he makes eggs fertile today.
    Text roosterText(boolean young, double vigour, boolean hungry, double perEgg);

    // A hen's egg row value: today's eggs; empty when she has none planned.
    Text eggsText(int laid, int planned, boolean hungryYesterday);

    // The label of the egg row: eggs for a hen, rooster or male for a male.
    Text eggRowLabel(boolean female, String speciesId);

    // A row's label and its value, as one line.
    Text labelled(Text label, Text value);
}
