package net.modcrafters.minecraft.barnyardromance.api;

import java.util.Optional;

/**
 * One tracked animal, as the stat card shows it. scale is its size multiplier; ageTicks is -1 when its birth isn't
 * known; familiarity runs from 0 to 1, and familiar is whether it has reached the familiar mark; wildCaught is an
 * animal tamed as an adult, whose familiarity is capped.
 */
public record AnimalInfo(
        Sex sex,
        double scale,
        SizeClass sizeClass,
        AgeStage ageStage,
        long ageTicks,
        double familiarity,
        boolean familiar,
        boolean wildCaught,
        Optional<PregnancyInfo> pregnancy
) {
}
