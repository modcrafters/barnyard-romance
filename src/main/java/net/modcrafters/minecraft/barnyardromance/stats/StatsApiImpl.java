package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.Upkeep;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatLine;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;

import java.util.List;

final class StatsApiImpl implements StatsApi {
    @Override
    public List<StatLine> lines(AnimalFacts facts) {
        return StatSheet.lines(facts);
    }

    @Override
    public boolean fedToday(AnimalTraits traits, long clock) {
        return LiveFacts.fedToday(traits, clock);
    }

    @Override
    public String lastOffspringName(AnimalTraits traits) {
        return LiveFacts.offspring(traits);
    }

    @Override
    public Text breedingText(boolean needsTaming, boolean tamed, double familiarity, boolean young, boolean wornOut, long loveUntil,
                             long cooldownUntil, long now) {
        return StatText.breeding(needsTaming, tamed, familiarity, young, wornOut, loveUntil, cooldownUntil, now);
    }

    @Override
    public Text fedByText(Upkeep upkeep, long today) {
        return StatText.feeder(upkeep, today);
    }

    @Override
    public Text milkText(MilkStatus status) {
        return StatText.milkText(status);
    }

    @Override
    public Text fatherText(String sizeLetter, String speciesId) {
        return StatText.father(sizeLetter, speciesId);
    }

    @Override
    public Text lastBirthText(String offspringName, long lastBirthTick, long now) {
        return StatText.lastBirth(offspringName, lastBirthTick, now);
    }

    @Override
    public Text roosterText(boolean young, double vigour, boolean hungry, double perEgg) {
        return StatText.roosterText(young, vigour, hungry, perEgg);
    }

    @Override
    public Text eggsText(int laid, int planned, boolean hungryYesterday) {
        return StatText.eggsText(laid, planned, hungryYesterday);
    }

    @Override
    public Text eggRowLabel(boolean female, String speciesId) {
        return StatText.eggRowLabel(female, speciesId);
    }

    @Override
    public Text labelled(Text label, Text value) {
        return StatText.labelled(label, value);
    }
}
