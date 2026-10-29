package net.modcrafters.minecraft.barnyardromance.pregnancy;

import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.text.Text;

public final class PregnancyText {
    private PregnancyText() {
    }

    public static Text describe(double progress, long remainingTicks) {
        if (remainingTicks <= 0) {
            return Text.of("gui.barnyard_romance.pregnancy.due_line");
        }
        return Text.of("gui.barnyard_romance.pregnancy.line", percent(progress), Growth.clock(remainingTicks));
    }

    // Rounded, and never 100 while time is left: "due" says that.
    public static int percent(double progress) {
        return Math.clamp(Math.round(progress * 100.0), 0, 99);
    }

    public static Text withFather(Text line, String fatherSpecies) {
        if (fatherSpecies == null || fatherSpecies.isBlank()) {
            return line;
        }
        return Text.of("gui.barnyard_romance.pregnancy.with_father", line, fatherSpecies);
    }

    public static Text line(Pregnancy pregnancy, long now, boolean includeFather) {
        Text described = describe(
                PregnancyClock.progress(now, pregnancy),
                PregnancyClock.remainingTicks(now, pregnancy)
        );
        if (!includeFather) {
            return described;
        }
        return withFather(described, pregnancy.fatherSpecies());
    }

    public static Text clock(long remainingTicks) {
        if (remainingTicks <= 0) {
            return Text.of("gui.barnyard_romance.pregnancy.due");
        }
        return Growth.clock(remainingTicks);
    }
}
