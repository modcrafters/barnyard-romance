package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.core.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// The status lines of one animal, in the order the spec gives. Lines that don't apply are left out, so the rest close up.
// The words that explain a line (why familiarity is low, a hungry male's health cost, a hen's reasons, the father,
// the last birth) are DETAIL lines under it; deaths, running away and when the animal was last seen
// stay NOTE lines.
public final class StatSheet {
    private StatSheet() {
    }

    public static List<StatLine> lines(AnimalFacts facts) {
        List<StatLine> lines = new ArrayList<>();
        lines.add(StatLine.of(StatKind.SIZE, facts.ageTicks() < 0L
                ? Text.of("gui.barnyard_romance.herd_book.size", facts.sizeLetter(), facts.stage().text())
                : Text.of("gui.barnyard_romance.herd_book.size_age", facts.sizeLetter(), facts.stage().text(), StatText.gameTime(facts.ageTicks()))));
        if (facts.diedDay() >= 0L) {
            lines.add(StatLine.of(StatKind.NOTE, Text.of("gui.barnyard_romance.herd_book.died", facts.diedDay())));
            if (facts.killedByRival()) {
                lines.add(StatLine.of(StatKind.NOTE, Text.of("gui.barnyard_romance.herd_book.killed_by_rival")));
            }
            ranAway(lines, facts);
            familiarity(lines, facts, false);
            fertility(lines, facts);
            return lines;
        }
        familiarity(lines, facts, true);
        ranAway(lines, facts);
        if (facts.fedToday()) {
            lines.add(StatLine.of(StatKind.FED, facts.fedBy().isEmpty()
                    ? Text.of("gui.barnyard_romance.herd_book.fed_today")
                    : Text.of("gui.barnyard_romance.herd_book.fed_today_by", facts.fedBy())));
        } else if (facts.ticksSinceFed() < 0L) {
            lines.add(StatLine.of(StatKind.NOT_FED, Text.of("gui.barnyard_romance.herd_book.never_fed")));
        } else {
            lines.add(StatLine.of(StatKind.NOT_FED, Text.of("gui.barnyard_romance.herd_book.last_fed", StatText.gameTime(facts.ticksSinceFed()))));
        }
        if (facts.growthTicks() >= 0L) {
            lines.add(new StatLine(StatKind.GROWTH, Text.of("gui.barnyard_romance.sheet.growth", Growth.clock(facts.growthTicks())),
                    Growth.percent((int) Math.min(Integer.MAX_VALUE, facts.growthTicks()))));
        }
        if (facts.weight() >= 0.0) {
            lines.add(new StatLine(StatKind.WEIGHT, Text.of("gui.barnyard_romance.sheet.weight", Familiarity.percent(facts.weight())), facts.weight()));
        }
        if (facts.maxHealth() > 0.0F) {
            lines.add(new StatLine(StatKind.HEALTH,
                    Text.of("gui.barnyard_romance.sheet.health", hp(facts.health()) + "/" + hp(facts.maxHealth())),
                    Math.clamp(facts.health() / (double) facts.maxHealth(), 0.0, 1.0)));
        }
        fertility(lines, facts);
        if (facts.vigour() >= 0.0) {
            lines.add(new StatLine(StatKind.VIGOUR, Text.of("gui.barnyard_romance.sheet.vigour", Familiarity.percent(facts.vigour())), facts.vigour()));
            if (facts.vigourHungry()) {
                lines.add(StatLine.of(StatKind.DETAIL, StatText.VIGOUR_HUNGRY_TEXT));
            }
        }
        if (!facts.milk().isEmpty()) {
            lines.add(StatLine.of(StatKind.MILK, facts.milk()));
        }
        switch (facts.shear()) {
            case READY -> lines.add(StatLine.of(StatKind.SHEAR, Text.of("gui.barnyard_romance.herd_book.shear_ready")));
            case SHEARED_TODAY -> lines.add(StatLine.of(StatKind.SHEAR, Text.of("gui.barnyard_romance.herd_book.sheared_today")));
            case REGROWING -> lines.add(StatLine.of(StatKind.SHEAR, Text.of("gui.barnyard_romance.herd_book.wool_regrowing")));
            case NONE -> {
            }
        }
        eggs(lines, facts);
        if (facts.pregnant()) {
            lines.add(new StatLine(StatKind.PREGNANT, PregnancyText.describe(facts.pregnancy(), facts.dueTicks()), facts.pregnancy()));
            if (!facts.father().isEmpty()) {
                lines.add(StatLine.of(StatKind.DETAIL, facts.father()));
            }
        } else {
            boolean breeding = !facts.breeding().isEmpty();
            if (breeding) {
                lines.add(StatLine.of(StatKind.BREEDING, StatText.labelled(Text.of("gui.barnyard_romance.inspector.breeding"), facts.breeding())));
            }
            if (!facts.lastBirth().isEmpty()) {
                lines.add(StatLine.of(breeding ? StatKind.DETAIL : StatKind.NOTE, facts.lastBirth()));
            }
        }
        if (facts.seenTicksAgo() >= 0L) {
            lines.add(StatLine.of(StatKind.NOTE, Text.of("gui.barnyard_romance.herd_book.seen", StatText.gameTime(facts.seenTicksAgo()))));
        }
        return lines;
    }

    // The familiarity bar, with the notes that explain a low or capped one.
    private static void familiarity(List<StatLine> lines, AnimalFacts facts, boolean notes) {
        lines.add(new StatLine(StatKind.FAMILIARITY, Text.of("gui.barnyard_romance.herd_book.familiarity", Familiarity.percent(facts.familiarity())),
                facts.familiarity()));
        if (!notes) {
            return;
        }
        switch (facts.familiar()) {
            case NOT_YET -> lines.add(StatLine.of(StatKind.DETAIL, Text.of("gui.barnyard_romance.herd_book.not_familiar")));
            case NO_LONGER -> lines.add(StatLine.of(StatKind.DETAIL, Text.of("gui.barnyard_romance.herd_book.no_longer_familiar")));
            case FAMILIAR -> {
            }
        }
        if (facts.grumpy()) {
            lines.add(StatLine.of(StatKind.DETAIL, Text.of("gui.barnyard_romance.herd_book.grumpy")));
        }
        if (facts.cap() < 1.0) {
            lines.add(StatLine.of(StatKind.DETAIL, Text.of("gui.barnyard_romance.herd_book.wild_caught", Familiarity.percent(facts.cap()))));
        }
    }

    // Kept until it's familiar again: under the familiarity lines that explain it, or with the death notes.
    private static void ranAway(List<StatLine> lines, AnimalFacts facts) {
        if (facts.ranAway()) {
            lines.add(StatLine.of(StatKind.NOTE, Text.of("gui.barnyard_romance.herd_book.ran_away")));
        }
    }

    // Fathers never wear out, and the young haven't bred yet, so only adult and old females show fertility.
    private static void fertility(List<StatLine> lines, AnimalFacts facts) {
        if (facts.fertility() >= 0.0 && (facts.stage() == AgeStage.ADULT || facts.stage() == AgeStage.OLD)) {
            lines.add(new StatLine(StatKind.FERTILITY, Text.of("gui.barnyard_romance.herd_book.fertility", Familiarity.percent(facts.fertility())),
                    facts.fertility()));
        }
    }

    // A hen's outlook for tomorrow, its reasons, then today's count; a rooster or a hen with no outlook just has today's row.
    private static void eggs(List<StatLine> lines, AnimalFacts facts) {
        boolean today = !facts.eggsToday().isEmpty();
        if (facts.eggs() != null) {
            lines.add(StatLine.of(StatKind.EGGS, facts.eggs().title()));
            facts.eggs().reasons().forEach(reason -> lines.add(StatLine.of(StatKind.DETAIL, reason.text())));
            if (today) {
                lines.add(StatLine.of(StatKind.DETAIL, facts.eggsToday()));
            }
        } else if (today) {
            lines.add(StatLine.of(StatKind.EGGS, facts.eggsToday()));
        }
    }

    // Whole numbers bare, otherwise one decimal.
    private static String hp(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
