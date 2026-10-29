package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.text.English;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HerdPageTextTest {
    private static final long DAY = 24_000L;

    private static HerdPageText.PageFacts cow() {
        return new HerdPageText.PageFacts("L", AgeStage.ADULT, 18 * DAY, true, 0L, true, 0.62, 12_000L, Text.EMPTY,
                0.8, 0.9, -1L, Familiar.FAMILIAR, -1L);
    }

    private static List<String> english(HerdPageText.PageFacts facts) {
        return HerdPageText.lines(facts).stream().map(line -> English.of(line.text())).toList();
    }

    private static List<HerdPageText.Kind> kinds(HerdPageText.PageFacts facts) {
        return HerdPageText.lines(facts).stream().map(HerdPageText.Line::kind).toList();
    }

    @Test
    void aPregnantFedLoadedCow() {
        assertEquals(List.of("Size L · Adult · 18d old", "Fed today", "Pregnant 62% · 10m 0s", "Familiar 80%", "Fertile 90%"),
                english(cow()));
    }

    @Test
    void eachLineSaysWhatItIsAndHowFullItsBarIs() {
        List<HerdPageText.Line> lines = HerdPageText.lines(cow());

        assertEquals(List.of(HerdPageText.Kind.SIZE, HerdPageText.Kind.FED, HerdPageText.Kind.PREGNANT,
                HerdPageText.Kind.FAMILIARITY, HerdPageText.Kind.FERTILITY), kinds(cow()));
        assertEquals(0.62, lines.get(2).fill());
        assertEquals(0.8, lines.get(3).fill());
        assertEquals(0.9, lines.get(4).fill());
    }

    @Test
    void notesAndHungerHaveTheirOwnKinds() {
        HerdPageText.PageFacts calf = new HerdPageText.PageFacts("S", AgeStage.BABY, 500L, false, -1L, false, 0.0, 0L, Text.plain("Milk 1/1"),
                0.2, -1.0, 3 * DAY, Familiar.NOT_YET, -1L);

        assertEquals(List.of(HerdPageText.Kind.SIZE, HerdPageText.Kind.NOTE, HerdPageText.Kind.NOT_FED, HerdPageText.Kind.MILK,
                HerdPageText.Kind.FAMILIARITY, HerdPageText.Kind.NOTE), kinds(calf));
        assertEquals(HerdPageText.Kind.NOTE, kinds(cow().died(42L)).get(1));
    }

    @Test
    void anUnloadedCowSaysWhenItWasSeen() {
        HerdPageText.PageFacts facts = cow().seen(2 * DAY);

        assertEquals("Seen 2d ago", english(facts).getLast());
    }

    @Test
    void frozenAnimalsSayWhy() {
        assertEquals("Not familiar", english(cow().familiar(Familiar.NOT_YET)).get(1));
        assertEquals("No longer familiar", english(cow().familiar(Familiar.NO_LONGER)).get(1));
    }

    @Test
    void theDeadKeepTheirValuesButNoLiveStatus() {
        assertEquals(List.of("Size L · Adult · 18d old", "Died on day 42", "Familiar 80%", "Fertile 90%"),
                english(cow().died(42L)));
    }

    @Test
    void aMaleHasNoPregnancyMilkOrFertility() {
        HerdPageText.PageFacts bull = new HerdPageText.PageFacts("XL", AgeStage.OLD, -1L, false, 3 * DAY, false, 0.0, 0L, Text.EMPTY,
                0.5, -1.0, -1L, Familiar.FAMILIAR, -1L);

        assertEquals(List.of("Size XL · Old", "Last fed 3d ago", "Familiar 50%"), english(bull));
    }

    @Test
    void anAnimalNeverFedSaysSo() {
        HerdPageText.PageFacts calf = new HerdPageText.PageFacts("S", AgeStage.BABY, 500L, false, -1L, false, 0.0, 0L, Text.EMPTY,
                0.2, -1.0, -1L, Familiar.NOT_YET, -1L);

        assertEquals(List.of("Size S · Baby · <1h old", "Not familiar", "Never fed", "Familiar 20%"), english(calf));
    }

    @Test
    void milkGoesUnderPregnancy() {
        HerdPageText.PageFacts facts = new HerdPageText.PageFacts("M", AgeStage.ADULT, -1L, true, 0L, false, 0.0, 0L, Text.plain("Milk 1/1"),
                0.8, 0.9, -1L, Familiar.FAMILIAR, -1L);

        assertEquals(List.of("Size M · Adult", "Fed today", "Milk 1/1", "Familiar 80%", "Fertile 90%"), english(facts));
    }
}
