package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorSheet;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.text.Text;

import java.util.ArrayList;
import java.util.List;

// The status lines on a herd book animal page. Lines that don't apply are left out, so the rest close up.
public final class HerdPageText {
    // ageTicks, ticksSinceFed and seenTicksAgo are -1 when unknown, never, or loaded; fertility is negative when
    // not shown; diedDay is -1 while alive. Feeding is counted on the overworld clock, like the once-a-day rule.
    public record PageFacts(
            String sizeLetter,
            AgeStage stage,
            long ageTicks,
            boolean fedToday,
            long ticksSinceFed,
            boolean pregnant,
            double pregnancy,
            long dueTicks,
            Text milk,
            double familiarity,
            double fertility,
            long seenTicksAgo,
            Familiar familiar,
            long diedDay
    ) {
        public PageFacts seen(long ticksAgo) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, ticksAgo, familiar, diedDay);
        }

        public PageFacts familiar(Familiar state) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, state, diedDay);
        }

        public PageFacts died(long day) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, day);
        }
    }

    private HerdPageText() {
    }

    public static List<Text> lines(PageFacts facts) {
        List<Text> lines = new ArrayList<>();
        lines.add(facts.ageTicks() < 0L
                ? Text.of("gui.barnyard_romance.herd_book.size", facts.sizeLetter(), facts.stage().text())
                : Text.of("gui.barnyard_romance.herd_book.size_age", facts.sizeLetter(), facts.stage().text(), InspectorSheet.gameTime(facts.ageTicks())));
        if (facts.diedDay() >= 0L) {
            lines.add(Text.of("gui.barnyard_romance.herd_book.died", facts.diedDay()));
            values(lines, facts);
            return lines;
        }
        switch (facts.familiar()) {
            case NOT_YET -> lines.add(Text.of("gui.barnyard_romance.herd_book.not_familiar"));
            case NO_LONGER -> lines.add(Text.of("gui.barnyard_romance.herd_book.no_longer_familiar"));
            case FAMILIAR -> {
            }
        }
        if (facts.fedToday()) {
            lines.add(Text.of("gui.barnyard_romance.herd_book.fed_today"));
        } else if (facts.ticksSinceFed() < 0L) {
            lines.add(Text.of("gui.barnyard_romance.herd_book.never_fed"));
        } else {
            lines.add(Text.of("gui.barnyard_romance.herd_book.last_fed", InspectorSheet.gameTime(facts.ticksSinceFed())));
        }
        if (facts.pregnant()) {
            lines.add(PregnancyText.describe(facts.pregnancy(), facts.dueTicks()));
        }
        if (!facts.milk().isEmpty()) {
            lines.add(facts.milk());
        }
        values(lines, facts);
        if (facts.seenTicksAgo() >= 0L) {
            lines.add(Text.of("gui.barnyard_romance.herd_book.seen", InspectorSheet.gameTime(facts.seenTicksAgo())));
        }
        return lines;
    }

    private static void values(List<Text> lines, PageFacts facts) {
        lines.add(Text.of("gui.barnyard_romance.herd_book.familiarity", Familiarity.percent(facts.familiarity())));
        if (facts.fertility() >= 0.0) {
            lines.add(Text.of("gui.barnyard_romance.herd_book.fertility", Familiarity.percent(facts.fertility())));
        }
    }
}
