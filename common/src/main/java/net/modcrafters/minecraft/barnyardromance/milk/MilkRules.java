package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;

public final class MilkRules {
    public static final String MALE_TEXT = "Only mothers give milk.";
    public static final String NO_MILK_TEXT = "She has no milk.";
    public static final String ENOUGH_TEXT = "Milked enough for today.";

    public enum Check {
        OK,
        MALE,
        NO_MILK,
        ENOUGH_TODAY
    }

    private MilkRules() {
    }

    public static Check check(Sex sex, SizeClass size, Lactation lactation, long now, Tuning tuning) {
        if (sex == Sex.MALE) {
            return Check.MALE;
        }
        if (!lactation.hasMilk(now, tuning.milkKeepsFlowingTicks())) {
            return Check.NO_MILK;
        }
        if (lactation.milkingsLeft(now, perDay(size, tuning)) <= 0) {
            return Check.ENOUGH_TODAY;
        }
        return Check.OK;
    }

    public static int perDay(SizeClass size, Tuning tuning) {
        return size == SizeClass.L || size == SizeClass.XL ? tuning.milkingsPerDayLarge() : tuning.milkingsPerDaySmall();
    }

    // An animal's first join since Stage 5. Babies and males are only marked. An adult female from before Stage 5,
    // or a wild one, gets one milk window so dairy farms keep working; a mother with a recorded birth instead gets
    // the milk that birth would have started, with her young growing at the wild rate.
    public static Lactation firstJoin(Sex sex, boolean baby, long lastBirthTick, Lactation lactation, long now, Tuning tuning) {
        if (lactation.graceGiven()) {
            return lactation;
        }
        if (sex != Sex.FEMALE || baby) {
            return lactation.withGraceGiven();
        }
        if (lastBirthTick >= 0L) {
            return lactation.afterBirth(lastBirthTick, lastBirthTick + tuning.growthWildTicks(), tuning.lactationTicks());
        }
        return lactation.grace(now, tuning.milkKeepsFlowingTicks());
    }

    public static String text(Check check) {
        return switch (check) {
            case MALE -> MALE_TEXT;
            case NO_MILK -> NO_MILK_TEXT;
            case ENOUGH_TODAY -> ENOUGH_TEXT;
            case OK -> "";
        };
    }
}
