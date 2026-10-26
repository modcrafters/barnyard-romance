package net.modcrafters.minecraft.barnyardromance.age;

// Age is worked out from the birth tick and the clock, so nothing ticks and the client needs no extra sync.
// An unknown birth (-1: old worlds, wild adults) is adult forever and never dies.
public final class Aging {
    private Aging() {
    }

    public static AgeStage stage(boolean baby, long bornTick, int growthTicks, long now, AgeSettings age) {
        if (baby) {
            return AgeStage.BABY;
        }
        if (bornTick < 0L) {
            return AgeStage.ADULT;
        }
        if (now >= oldFrom(bornTick, age)) {
            return AgeStage.OLD;
        }
        if (age.youngTicks() > 0L && now < bornTick + Math.max(0, growthTicks) + age.youngTicks()) {
            return AgeStage.YOUNG;
        }
        return AgeStage.ADULT;
    }

    public static long oldFrom(long bornTick, AgeSettings age) {
        if (bornTick < 0L || age.oldAfterTicks() <= 0L) {
            return Long.MAX_VALUE;
        }
        return bornTick + age.oldAfterTicks();
    }

    // spreadRandom 0.5 is the plain deathAfterTicks; 0 and 1 move it by -spread and +spread of itself.
    public static long deathAt(long bornTick, AgeSettings age, double spread, double spreadRandom) {
        if (bornTick < 0L || age.deathAfterTicks() <= 0L) {
            return Long.MAX_VALUE;
        }
        double factor = 1.0 + (Math.clamp(spreadRandom, 0.0, 1.0) * 2.0 - 1.0) * Math.clamp(spread, 0.0, 1.0);
        return bornTick + Math.round(age.deathAfterTicks() * factor);
    }

    // A steady 0-1 value per animal (from its UUID bits), so its death tick never changes and needs no storage.
    public static double spreadRandom(long bits) {
        long mixed = bits * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 32;
        return (mixed >>> 11) * 0x1.0p-53;
    }
}
