package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.eggs.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.eggs.RoosterNear;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.text.English;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.stats.StatKind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatSheetTest {
    private static final long DAY = 24_000L;

    private static AnimalFacts.Builder cow() {
        return AnimalFacts.builder("L", AgeStage.ADULT).ageTicks(18 * DAY).fedToday(true).ticksSinceFed(0L).pregnant(true).pregnancy(0.62)
                .dueTicks(12_000L).familiarity(0.8).fertility(0.9);
    }

    private static AnimalFacts.Builder bull() {
        return AnimalFacts.builder("L", AgeStage.ADULT).ageTicks(18 * DAY).fedToday(true).ticksSinceFed(0L).familiarity(0.8)
                .growthTicks(24_000L).weight(0.5).health(10.0F).maxHealth(10.0F).vigour(0.7).breeding(StatText.READY);
    }

    private static EggOutlook outlook() {
        return EggOutlook.tomorrow(SizeClass.M, true, false, RoosterNear.NONE, true, EggSettings.CHICKEN, "minecraft:chicken");
    }

    private static List<String> english(AnimalFacts.Builder facts) {
        return StatSheet.lines(facts.build()).stream().map(line -> English.of(line.text())).toList();
    }

    private static List<StatKind> kinds(AnimalFacts.Builder facts) {
        return StatSheet.lines(facts.build()).stream().map(StatLine::kind).toList();
    }

    @Test
    void aPregnantFedLoadedCow() {
        assertEquals(List.of("Size L · Adult · 18d old", "Familiar 80%", "Fed today", "Fertile 90%", "Pregnant 62% · 10m 0s"), english(cow()));
    }

    @Test
    void eachLineSaysWhatItIsAndHowFullItsBarIs() {
        List<StatLine> lines = StatSheet.lines(cow().build());

        assertEquals(List.of(SIZE, FAMILIARITY, FED, FERTILITY, PREGNANT), kinds(cow()));
        assertEquals(0.8, lines.get(1).fill());
        assertEquals(0.9, lines.get(3).fill());
        assertEquals(0.62, lines.get(4).fill());
    }

    @Test
    void notesAndDetailsHaveTheirOwnKinds() {
        AnimalFacts.Builder calf = AnimalFacts.builder("S", AgeStage.BABY).ageTicks(500L).milk(Text.plain("Milk 1/1")).familiarity(0.2)
                .seenTicksAgo(3 * DAY).familiar(Familiar.NOT_YET);

        assertEquals(List.of(SIZE, FAMILIARITY, DETAIL, NOT_FED, MILK, NOTE), kinds(calf));
        assertEquals(NOTE, kinds(cow().diedDay(42L)).get(1));
    }

    @Test
    void aSheepSaysWhetherItCanBeShorn() {
        AnimalFacts.Builder ewe = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).familiarity(0.8).fertility(0.9);

        assertEquals(List.of("Size M · Adult", "Familiar 80%", "Fed today", "Fertile 90%", "Ready to shear"), english(ewe.shear(ShearState.READY)));
        assertEquals("Sheared today", english(ewe.shear(ShearState.SHEARED_TODAY)).get(4));
        assertEquals("Wool growing back", english(ewe.shear(ShearState.REGROWING)).get(4));
        assertEquals(SHEAR, kinds(ewe.shear(ShearState.READY)).get(4));
        assertEquals(4, english(ewe.shear(ShearState.NONE)).size());
    }

    @Test
    void aHenSaysWhatSheLaysTomorrowAndWhy() {
        AnimalFacts.Builder hen = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).familiarity(0.2).fertility(0.9)
                .eggs(outlook());

        assertEquals(List.of("Size M · Adult", "Familiar 20%", "Fed today", "Fertile 90%", "Plain eggs tomorrow (1)", "No rooster nearby",
                "Not familiar: one egg fewer"), english(hen));
        assertEquals(List.of(SIZE, FAMILIARITY, FED, FERTILITY, EGGS, DETAIL, DETAIL), kinds(hen));
    }

    @Test
    void anUnloadedCowSaysWhenItWasSeen() {
        assertEquals("Seen 2d ago", english(cow().seenTicksAgo(2 * DAY)).getLast());
    }

    @Test
    void frozenAnimalsSayWhy() {
        assertEquals("Not familiar", english(cow().familiar(Familiar.NOT_YET)).get(2));
        assertEquals("No longer familiar", english(cow().familiar(Familiar.NO_LONGER)).get(2));
    }

    @Test
    void theDeadKeepTheirValuesButNoLiveStatus() {
        assertEquals(List.of("Size L · Adult · 18d old", "Died on day 42", "Familiar 80%", "Fertile 90%"), english(cow().diedDay(42L)));
    }

    @Test
    void aMaleKilledByARivalSaysSo() {
        assertEquals(List.of("Size L · Adult · 18d old", "Died on day 42", "Killed by a rival", "Familiar 80%", "Fertile 90%"),
                english(cow().diedDay(42L).killedByRival(true)));
    }

    @Test
    void aRunawayIsNoted() {
        AnimalFacts.Builder cow = AnimalFacts.builder("L", AgeStage.ADULT).ticksSinceFed(3 * DAY).familiarity(0.05).familiar(Familiar.NO_LONGER)
                .ranAway(true);

        assertEquals(List.of("Size L · Adult", "Familiar 5%", "No longer familiar", "Ran away", "Last fed 3d ago"), english(cow));
        assertEquals(List.of(SIZE, FAMILIARITY, DETAIL, NOTE, NOT_FED), kinds(cow));
        assertEquals(List.of("Size L · Adult · 18d old", "Died on day 42", "Ran away", "Familiar 80%", "Fertile 90%"),
                english(cow().diedDay(42L).ranAway(true)));
    }

    @Test
    void aMaleHasNoPregnancyMilkOrFertility() {
        AnimalFacts.Builder old = AnimalFacts.builder("XL", AgeStage.OLD).ticksSinceFed(3 * DAY).familiarity(0.5);

        assertEquals(List.of("Size XL · Old", "Familiar 50%", "Last fed 3d ago"), english(old));
    }

    @Test
    void anAnimalNeverFedSaysSo() {
        AnimalFacts.Builder calf = AnimalFacts.builder("S", AgeStage.BABY).ageTicks(500L).familiarity(0.2).familiar(Familiar.NOT_YET);

        assertEquals(List.of("Size S · Baby · <1h old", "Familiar 20%", "Not familiar", "Never fed"), english(calf));
    }

    @Test
    void milkGoesAfterFertility() {
        AnimalFacts.Builder facts = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).milk(Text.plain("Milk 1/1"))
                .familiarity(0.8).fertility(0.9);

        assertEquals(List.of("Size M · Adult", "Familiar 80%", "Fed today", "Fertile 90%", "Milk 1/1"), english(facts));
    }

    @Test
    void aWildCaughtAnimalSaysSo() {
        assertEquals(List.of("Size L · Adult · 18d old", "Familiar 80%", "Wild: max 40%", "Fed today", "Fertile 90%", "Pregnant 62% · 10m 0s"),
                english(cow().cap(0.4)));
        assertEquals(english(cow()), english(cow().cap(1.0)));
    }

    @Test
    void aGrumpyPetSaysSo() {
        AnimalFacts.Builder wolf = AnimalFacts.builder("L", AgeStage.ADULT).ticksSinceFed(2 * DAY).familiarity(0.2).familiar(Familiar.NO_LONGER);

        assertEquals(List.of("Size L · Adult", "Familiar 20%", "No longer familiar", "Grumpy: won't take orders", "Last fed 2d ago"),
                english(wolf.grumpy(true)));
        assertEquals(List.of("Size L · Adult", "Familiar 20%", "No longer familiar", "Last fed 2d ago"), english(wolf.grumpy(false)));
    }

    @Test
    void aFedGrownBull() {
        assertEquals(List.of("Size L · Adult · 18d old", "Familiar 80%", "Fed today", "Growth 20m 0s", "Weight 50%", "Health 10/10", "Vigour 70%",
                "Breeding: Ready"), english(bull()));
        assertEquals(List.of(SIZE, FAMILIARITY, FED, GROWTH, WEIGHT, HEALTH, VIGOUR, BREEDING), kinds(bull()));
        List<StatLine> lines = StatSheet.lines(bull().build());
        assertEquals(1.0, lines.get(5).fill());
        assertEquals(0.7, lines.get(6).fill());
    }

    @Test
    void healthShowsOneDecimalWhenNotWhole() {
        assertEquals("Health 7.5/10", english(bull().health(7.5F)).get(5));
    }

    @Test
    void aHungryBullNotesTheCost() {
        assertEquals(List.of(SIZE, FAMILIARITY, FED, GROWTH, WEIGHT, HEALTH, VIGOUR, DETAIL, BREEDING), kinds(bull().vigourHungry(true)));
        assertEquals("Hungry: mating costs health", english(bull().vigourHungry(true)).get(7));
    }

    @Test
    void aHenSaysTomorrowThenToday() {
        AnimalFacts.Builder hen = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).familiarity(0.2)
                .eggs(outlook()).eggsToday(Text.plain("Eggs: 2/3 today"));

        assertEquals(List.of(SIZE, FAMILIARITY, FED, EGGS, DETAIL, DETAIL, DETAIL), kinds(hen));
        assertEquals(List.of("Size M · Adult", "Familiar 20%", "Fed today", "Plain eggs tomorrow (1)", "No rooster nearby",
                "Not familiar: one egg fewer", "Eggs: 2/3 today"), english(hen));
    }

    @Test
    void withoutAnOutlookTheEggRowIsToday() {
        AnimalFacts.Builder hen = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).familiarity(0.2)
                .eggsToday(Text.plain("Eggs: 2/3 today"));

        assertEquals(List.of(SIZE, FAMILIARITY, FED, EGGS), kinds(hen));
        assertEquals("Eggs: 2/3 today", english(hen).getLast());
    }

    @Test
    void aRoosterSaysHowManyEggsHeHasLeft() {
        AnimalFacts.Builder rooster = AnimalFacts.builder("M", AgeStage.ADULT).fedToday(true).ticksSinceFed(0L).familiarity(0.9)
                .eggsToday(Text.plain("Rooster: Fertile, 3 eggs left"));

        assertEquals(List.of(SIZE, FAMILIARITY, FED, EGGS), kinds(rooster));
        assertEquals("Rooster: Fertile, 3 eggs left", english(rooster).getLast());
    }

    @Test
    void pregnancyNamesTheFather() {
        AnimalFacts.Builder expecting = cow().father(StatText.father("L", "minecraft:cow"));

        assertEquals(List.of(SIZE, FAMILIARITY, FED, FERTILITY, PREGNANT, DETAIL), kinds(expecting));
        assertEquals("Father L bull", english(expecting).getLast());
    }

    @Test
    void afterBirthTheLastBirthIsADetailUnderBreeding() {
        AnimalFacts.Builder mother = AnimalFacts.builder("L", AgeStage.ADULT).familiarity(0.8).breeding(StatText.READY)
                .lastBirth(StatText.lastBirth("Calf", 0L, 3 * 60 * 20));

        assertEquals(List.of(SIZE, FAMILIARITY, NOT_FED, BREEDING, DETAIL), kinds(mother));
        assertEquals("Last birth: Calf, 3m ago", english(mother).getLast());
    }

    @Test
    void fertilityIsOnlyForGrownFemales() {
        assertEquals(List.of(SIZE, FAMILIARITY, NOT_FED), kinds(AnimalFacts.builder("M", AgeStage.YOUNG).familiarity(0.5).fertility(0.9)));
        assertEquals(List.of(SIZE, FAMILIARITY, NOT_FED, FERTILITY), kinds(AnimalFacts.builder("M", AgeStage.OLD).familiarity(0.5).fertility(0.9)));
    }

    @Test
    void hiddenValuesLeaveNoGap() {
        assertEquals(List.of(SIZE, FAMILIARITY, NOT_FED), kinds(AnimalFacts.builder("M", AgeStage.ADULT).familiarity(0.5)));
        assertEquals("Never fed", english(AnimalFacts.builder("M", AgeStage.ADULT).familiarity(0.5)).get(2));
    }

    @Test
    void theDeadKeepTodaysLines() {
        assertEquals(List.of(SIZE, NOTE, FAMILIARITY, FERTILITY),
                kinds(bull().fertility(0.9).diedDay(42L).breeding(StatText.READY).vigour(0.7)));
    }
}
