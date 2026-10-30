package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;

public final class EggRules {
    // A thrown egg was out in the weather: its chick is small, and only big parents give it a chance of medium.
    private static final double THROWN_MEDIUM_CHANCE = 0.1;

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

    public static SizeClass thrownChickSize(SizeClass mother, SizeClass father, double roll) {
        return (large(mother) || large(father)) && roll < THROWN_MEDIUM_CHANCE ? SizeClass.M : SizeClass.S;
    }

    private static boolean large(SizeClass size) {
        return size == SizeClass.L || size == SizeClass.XL;
    }
}
