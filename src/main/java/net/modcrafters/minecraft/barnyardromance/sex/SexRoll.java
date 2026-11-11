package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;

public final class SexRoll {
    private SexRoll() {
    }

    public static Sex roll(double unitRandom, double femaleRatio) {
        return unitRandom < femaleRatio ? Sex.FEMALE : Sex.MALE;
    }
}
