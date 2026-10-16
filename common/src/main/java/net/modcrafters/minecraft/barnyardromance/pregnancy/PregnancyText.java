package net.modcrafters.minecraft.barnyardromance.pregnancy;

public final class PregnancyText {
    private PregnancyText() {
    }

    public static String describe(double progress, long remainingTicks) {
        if (remainingTicks <= 0) {
            return "Pregnant, due";
        }
        int percent = (int) Math.round(progress * 100.0);
        if (percent >= 100) {
            percent = 99;
        }
        if (percent < 0) {
            percent = 0;
        }
        return "Pregnant " + percent + "% · " + formatRemaining(remainingTicks);
    }

    public static String withFather(String line, String fatherSpecies) {
        if (fatherSpecies == null || fatherSpecies.isBlank()) {
            return line;
        }
        return line + " · father " + fatherSpecies;
    }

    public static String line(Pregnancy pregnancy, long now, boolean includeFather) {
        String described = describe(
                PregnancyClock.progress(now, pregnancy.conceivedAt(), pregnancy.durationTicks()),
                PregnancyClock.remainingTicks(now, pregnancy.conceivedAt(), pregnancy.durationTicks())
        );
        if (!includeFather) {
            return described;
        }
        return withFather(described, pregnancy.fatherSpecies());
    }

    public static String clock(long remainingTicks) {
        if (remainingTicks <= 0) {
            return "due";
        }
        return formatRemaining(remainingTicks);
    }

    private static String formatRemaining(long ticks) {
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
