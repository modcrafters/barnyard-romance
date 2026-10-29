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
            long diedDay,
            ShearState shear,
            boolean grumpy
    ) {
        public PageFacts(String sizeLetter, AgeStage stage, long ageTicks, boolean fedToday, long ticksSinceFed, boolean pregnant, double pregnancy,
                         long dueTicks, Text milk, double familiarity, double fertility, long seenTicksAgo, Familiar familiar, long diedDay, ShearState shear) {
            this(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, diedDay,
                    shear, false);
        }

        public PageFacts(String sizeLetter, AgeStage stage, long ageTicks, boolean fedToday, long ticksSinceFed, boolean pregnant, double pregnancy,
                         long dueTicks, Text milk, double familiarity, double fertility, long seenTicksAgo, Familiar familiar, long diedDay) {
            this(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, diedDay,
                    ShearState.NONE);
        }

        public PageFacts shear(ShearState state) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, diedDay, state, grumpy);
        }

        public PageFacts seen(long ticksAgo) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, ticksAgo, familiar, diedDay, shear, grumpy);
        }

        public PageFacts familiar(Familiar state) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, state, diedDay, shear, grumpy);
        }

        public PageFacts grumpy(boolean grumpy) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, diedDay, shear, grumpy);
        }

        public PageFacts died(long day) {
            return new PageFacts(sizeLetter, stage, ageTicks, fedToday, ticksSinceFed, pregnant, pregnancy, dueTicks, milk, familiarity, fertility, seenTicksAgo, familiar, day, shear, grumpy);
        }
    }

    // What a line is about, so the page can give it an icon or a bar. NOTE lines (died, not familiar, seen) get neither.
    public enum Kind {
        SIZE,
        NOTE,
        FED,
        NOT_FED,
        PREGNANT,
        MILK,
        SHEAR,
        FAMILIARITY,
        FERTILITY
    }

    // fill is how far along a pregnancy, familiarity or fertility is, from 0 to 1; 0 for the other kinds.
    public record Line(Kind kind, Text text, double fill) {
        static Line of(Kind kind, Text text) {
            return new Line(kind, text, 0.0);
        }
    }

    private HerdPageText() {
    }

    public static List<Line> lines(PageFacts facts) {
        List<Line> lines = new ArrayList<>();
        lines.add(Line.of(Kind.SIZE, facts.ageTicks() < 0L
                ? Text.of("gui.barnyard_romance.herd_book.size", facts.sizeLetter(), facts.stage().text())
                : Text.of("gui.barnyard_romance.herd_book.size_age", facts.sizeLetter(), facts.stage().text(), InspectorSheet.gameTime(facts.ageTicks()))));
        if (facts.diedDay() >= 0L) {
            lines.add(Line.of(Kind.NOTE, Text.of("gui.barnyard_romance.herd_book.died", facts.diedDay())));
            values(lines, facts);
            return lines;
        }
        switch (facts.familiar()) {
            case NOT_YET -> lines.add(Line.of(Kind.NOTE, Text.of("gui.barnyard_romance.herd_book.not_familiar")));
            case NO_LONGER -> lines.add(Line.of(Kind.NOTE, Text.of("gui.barnyard_romance.herd_book.no_longer_familiar")));
            case FAMILIAR -> {
            }
        }
        if (facts.grumpy()) {
            lines.add(Line.of(Kind.NOTE, Text.of("gui.barnyard_romance.herd_book.grumpy")));
        }
        if (facts.fedToday()) {
            lines.add(Line.of(Kind.FED, Text.of("gui.barnyard_romance.herd_book.fed_today")));
        } else if (facts.ticksSinceFed() < 0L) {
            lines.add(Line.of(Kind.NOT_FED, Text.of("gui.barnyard_romance.herd_book.never_fed")));
        } else {
            lines.add(Line.of(Kind.NOT_FED, Text.of("gui.barnyard_romance.herd_book.last_fed", InspectorSheet.gameTime(facts.ticksSinceFed()))));
        }
        if (facts.pregnant()) {
            lines.add(new Line(Kind.PREGNANT, PregnancyText.describe(facts.pregnancy(), facts.dueTicks()), facts.pregnancy()));
        }
        if (!facts.milk().isEmpty()) {
            lines.add(Line.of(Kind.MILK, facts.milk()));
        }
        switch (facts.shear()) {
            case READY -> lines.add(Line.of(Kind.SHEAR, Text.of("gui.barnyard_romance.herd_book.shear_ready")));
            case SHEARED_TODAY -> lines.add(Line.of(Kind.SHEAR, Text.of("gui.barnyard_romance.herd_book.sheared_today")));
            case REGROWING -> lines.add(Line.of(Kind.SHEAR, Text.of("gui.barnyard_romance.herd_book.wool_regrowing")));
            case NONE -> {
            }
        }
        values(lines, facts);
        if (facts.seenTicksAgo() >= 0L) {
            lines.add(Line.of(Kind.NOTE, Text.of("gui.barnyard_romance.herd_book.seen", InspectorSheet.gameTime(facts.seenTicksAgo()))));
        }
        return lines;
    }

    private static void values(List<Line> lines, PageFacts facts) {
        lines.add(new Line(Kind.FAMILIARITY, Text.of("gui.barnyard_romance.herd_book.familiarity", Familiarity.percent(facts.familiarity())),
                facts.familiarity()));
        if (facts.fertility() >= 0.0) {
            lines.add(new Line(Kind.FERTILITY, Text.of("gui.barnyard_romance.herd_book.fertility", Familiarity.percent(facts.fertility())),
                    facts.fertility()));
        }
    }
}
