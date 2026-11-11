package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.stats.StatKind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JadeLinesTest {
    private static final long DAY = 24_000L;

    // A bull who isn't familiar yet, so the sheet has notes and a health line as well as the short ones.
    private static List<StatLine> bull() {
        return StatSheet.lines(AnimalFacts.builder("L", AgeStage.ADULT).ageTicks(18 * DAY).fedToday(true).ticksSinceFed(0L)
                .familiarity(0.2).familiar(Familiar.NOT_YET).growthTicks(24_000L).weight(0.5).health(10.0F).maxHealth(10.0F)
                .vigour(0.7).vigourHungry(true).breeding(StatText.READY).build());
    }

    private static List<StatKind> kinds(List<StatLine> lines) {
        return lines.stream().map(StatLine::kind).toList();
    }

    @Test
    void detailsShowsEverythingButHealth() {
        List<StatLine> full = bull();

        assertEquals(List.of(SIZE, FAMILIARITY, DETAIL, FED, GROWTH, WEIGHT, HEALTH, VIGOUR, DETAIL, BREEDING), kinds(full));
        assertEquals(full.stream().filter(line -> line.kind() != HEALTH).toList(), JadeLines.of(full, true));
    }

    @Test
    void shortShowsSizeFamiliarityAndFeed() {
        List<StatLine> full = bull();

        assertEquals(List.of(full.get(0), full.get(1), full.get(3)), JadeLines.of(full, false));
    }

    @Test
    void shortKeepsNeverFedToo() {
        List<StatLine> full = StatSheet.lines(AnimalFacts.builder("S", AgeStage.BABY).ageTicks(500L).familiarity(0.9).build());

        assertEquals(List.of(SIZE, FAMILIARITY, NOT_FED), kinds(JadeLines.of(full, false)));
    }
}
