package net.modcrafters.minecraft.barnyardromance.milk;

import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;

import java.util.Locale;

public final class MilkRules {
    public static final Text MALE_TEXT = Text.of("message.barnyard_romance.milk.male");
    public static final Text NO_MILK_TEXT = Text.of("message.barnyard_romance.milk.none");
    public static final Text ENOUGH_TEXT = Text.of("message.barnyard_romance.milk.enough");
    public static final Text DISABLED_TEXT = Text.of("message.barnyard_romance.milk.disabled");
    public static final Text HUNGRY_TEXT = Text.of("message.barnyard_romance.milk.hungry");
    public static final Text START_MALE_TEXT = Text.of("message.barnyard_romance.dairy_mash.male");
    public static final Text START_BABY_TEXT = Text.of("message.barnyard_romance.dairy_mash.baby");
    public static final Text START_NO_MILK_SPECIES_TEXT = Text.of("message.barnyard_romance.dairy_mash.no_milk_species");
    public static final Text START_ALREADY_TEXT = Text.of("message.barnyard_romance.dairy_mash.already");
    public static final Text START_NOT_FAMILIAR_TEXT = Text.of("message.barnyard_romance.not_familiar");
    public static final Text REFUSED_TEXT = Text.of("message.barnyard_romance.refused.milk");
    public static final Text REFUSED_TODAY_TEXT = Text.of("message.barnyard_romance.refused.today");

    public enum Check {
        OK,
        MALE,
        DISABLED,
        NO_MILK,
        HUNGRY,
        ENOUGH_TODAY
    }

    // Whether Dairy Mash can start a female's milk.
    public enum Start {
        OK,
        MALE,
        BABY,
        NO_MILK_SPECIES,
        ALREADY,
        NOT_FAMILIAR
    }

    private MilkRules() {
    }

    public static Start start(Sex sex, boolean baby, boolean givesMilk, boolean hasMilk, boolean familiar) {
        if (!givesMilk) {
            return Start.NO_MILK_SPECIES;
        }
        if (sex == Sex.MALE) {
            return Start.MALE;
        }
        if (baby) {
            return Start.BABY;
        }
        if (!familiar) {
            return Start.NOT_FAMILIAR;
        }
        return hasMilk ? Start.ALREADY : Start.OK;
    }

    public static Text text(Start start) {
        return switch (start) {
            case MALE -> START_MALE_TEXT;
            case BABY -> START_BABY_TEXT;
            case NO_MILK_SPECIES -> START_NO_MILK_SPECIES_TEXT;
            case ALREADY -> START_ALREADY_TEXT;
            case NOT_FAMILIAR -> START_NOT_FAMILIAR_TEXT;
            case OK -> Text.EMPTY;
        };
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

    // What Dairy Mash says it does: how many days its milk lasts, whole or to one decimal place.
    public static Text dairyMashEffect(long ticks) {
        double days = ticks / 24_000.0;
        if (days == 1.0) {
            return Text.of("item.barnyard_romance.dairy_mash.effect_one");
        }
        String shown = days == Math.rint(days) ? Long.toString((long) days) : String.format(Locale.ROOT, "%.1f", days);
        return Text.of("item.barnyard_romance.dairy_mash.effect", shown);
    }
}
