package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;

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
