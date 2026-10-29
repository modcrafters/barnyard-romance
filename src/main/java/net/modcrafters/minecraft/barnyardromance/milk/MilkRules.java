package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;

public final class MilkRules {
    public static final Text MALE_TEXT = Text.of("message.barnyard_romance.milk.male");
    public static final Text NO_MILK_TEXT = Text.of("message.barnyard_romance.milk.none");
    public static final Text ENOUGH_TEXT = Text.of("message.barnyard_romance.milk.enough");
    public static final Text DISABLED_TEXT = Text.of("message.barnyard_romance.milk.disabled");
    public static final Text HUNGRY_TEXT = Text.of("message.barnyard_romance.milk.hungry");

    public enum Check {
        OK,
        MALE,
        DISABLED,
        NO_MILK,
        HUNGRY,
        ENOUGH_TODAY
    }

    private MilkRules() {
    }

    // now is game time, for how long milk flows; clock is the overworld clock, for the daily count.
    public static Check check(Sex sex, SizeClass size, Lactation lactation, long now, long clock, Tuning tuning) {
        return check(sex, size, lactation, now, clock, tuning, false);
    }

    // A female that went hungry yesterday has no milk today (see Hunger).
    public static Check check(Sex sex, SizeClass size, Lactation lactation, long now, long clock, Tuning tuning, boolean hungryYesterday) {
        if (sex == Sex.MALE) {
            return Check.MALE;
        }
        if (perDay(size, tuning) <= 0) {
            return Check.DISABLED;
        }
        if (!lactation.hasMilk(now, tuning.milkKeepsFlowingTicks())) {
            return Check.NO_MILK;
        }
        if (hungryYesterday) {
            return Check.HUNGRY;
        }
        if (lactation.milkingsLeft(clock, perDay(size, tuning)) <= 0) {
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

    public static Text text(Check check) {
        return switch (check) {
            case MALE -> MALE_TEXT;
            case DISABLED -> DISABLED_TEXT;
            case NO_MILK -> NO_MILK_TEXT;
            case HUNGRY -> HUNGRY_TEXT;
            case ENOUGH_TODAY -> ENOUGH_TEXT;
            case OK -> Text.EMPTY;
        };
    }
}
