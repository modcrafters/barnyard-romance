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
        int percent = (int) Math.round(progress * 100.0);
        if (percent >= 100) {
            percent = 99;
        }
        if (percent < 0) {
            percent = 0;
        }
        return Text.of("gui.barnyard_romance.pregnancy.line", percent, Growth.clock(remainingTicks));
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
