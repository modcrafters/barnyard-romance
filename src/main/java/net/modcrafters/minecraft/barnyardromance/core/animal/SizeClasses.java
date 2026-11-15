package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.api.SizeClass;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;

// An animal's size class from its species' size settings.
public final class SizeClasses {
    private SizeClasses() {
    }

    public static SizeClass of(Sex sex, double scale, SizeSettings size) {
        return SizeClass.of(scale, sex == Sex.FEMALE ? size.femaleScale() : size.maleScale(), size.variation());
    }
}
