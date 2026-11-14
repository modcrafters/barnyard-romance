package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggOutlook;
import net.modcrafters.minecraft.barnyardromance.eggs.api.RoosterNear;

import java.util.ArrayList;
import java.util.List;

public final class EggRules {
    // A thrown egg was out in the weather: its chick is small, and only big parents give it a chance of medium.
    private static final double THROWN_MEDIUM_CHANCE = 0.1;
    // In in-game minutes; an in-game hour is 1,000 ticks.
    private static final int SIT_MINUTES = 15;
    private static final int EXTRA_EGG_MINUTES = 5;

    private EggRules() {
    }

    // Eggs a hen lays today: by size, one more when she's happy (familiar), none after a hungry day.
    public static int perDay(SizeClass size, boolean hungryYesterday, boolean happy, EggSettings settings) {
        if (!settings.enabled() || hungryYesterday) {
            return 0;
        }
        int count = size == SizeClass.L || size == SizeClass.XL ? settings.largePerDay() : settings.smallPerDay();
        return happy ? count + settings.happyBonus() : count;
    }

    // What she will lay tomorrow, for the herd book (see EggOutlook).
    public static EggOutlook tomorrow(SizeClass size, boolean ateToday, boolean familiar, RoosterNear rooster, boolean boxWithRoom, EggSettings settings, String speciesId) {
        int count = perDay(size, !ateToday, familiar, settings);
        if (count <= 0) {
            return new EggOutlook(EggOutlook.Kind.NO_EGGS, 0, List.of(EggOutlook.Reason.NOT_FED));
        }
        List<EggOutlook.Reason> reasons = new ArrayList<>();
        // Only a chicken's mate is a rooster; a duck's herd book mustn't send you looking for one.
        boolean chicken = "minecraft:chicken".equals(speciesId);
        if (rooster == RoosterNear.NONE) {
            reasons.add(chicken ? EggOutlook.Reason.NO_ROOSTER : EggOutlook.Reason.NO_MALE);
        } else if (rooster == RoosterNear.HUNGRY) {
            reasons.add(chicken ? EggOutlook.Reason.ROOSTER_HUNGRY : EggOutlook.Reason.MALE_HUNGRY);
        }
        if (!boxWithRoom) {
            reasons.add(EggOutlook.Reason.NO_BOX);
        }
        EggOutlook.Kind kind = reasons.isEmpty() ? EggOutlook.Kind.FERTILIZED : EggOutlook.Kind.PLAIN;
        if (!familiar) {
            reasons.add(EggOutlook.Reason.NOT_FAMILIAR);
        }
        return new EggOutlook(kind, count, reasons);
    }

    // How long a hen sits in a nesting box to lay her eggs: a quarter of an hour, and five minutes more for each egg
    // after the first.
    public static int sitTicks(int eggs) {
        return (SIT_MINUTES + EXTRA_EGG_MINUTES * Math.max(0, eggs - 1)) * 1_000 / 60;
    }

    public static SizeClass thrownChickSize(SizeClass mother, SizeClass father, double roll) {
        return (large(mother) || large(father)) && roll < THROWN_MEDIUM_CHANCE ? SizeClass.M : SizeClass.S;
    }

    private static boolean large(SizeClass size) {
        return size == SizeClass.L || size == SizeClass.XL;
    }
}
