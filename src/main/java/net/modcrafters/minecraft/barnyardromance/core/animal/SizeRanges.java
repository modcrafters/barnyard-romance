package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import org.jspecify.annotations.Nullable;

// An animal's scale is kept with the size range it was set in. When the config's range changes, the animal moves to
// the same spot in the new range: the same distance from its sex's base, stretched by the change in variation.
public final class SizeRanges {
    public static final double MIN_SCALE = 0.0625;
    public static final double MAX_SCALE = 16.0;

    private SizeRanges() {
    }

    public static double base(Sex sex, SizeSettings range) {
        return sex == Sex.FEMALE ? range.femaleScale() : range.maleScale();
    }

    // Only clamped to the scale limits, so an animal outside its range stays as far outside. A range with no variation,
    // or a broken one from a corrupt save (negative or NaN), puts it at the base.
    public static double rescale(Sex sex, double scale, SizeSettings from, SizeSettings to) {
        double offset = !(from.variation() > 0.0) ? 0.0 : (scale - base(sex, from)) * (to.variation() / from.variation());
        return Math.clamp(base(sex, to) + offset, MIN_SCALE, MAX_SCALE);
    }

    // The traits moved into the current range. Traits with no range are taken to be in it already.
    public static AnimalTraits settled(AnimalTraits traits, SizeSettings current) {
        SizeSettings stored = traits.sizeRange().orElse(null);
        if (stored == null) {
            return traits.withSizeRange(current);
        }
        if (stored.equals(current)) {
            return traits;
        }
        return traits.withScale(rescale(traits.sex(), traits.scale(), stored, current)).withSizeRange(current);
    }

    // The range the traits' scale was set in, or the species' current one when they have none.
    public static SizeSettings rangeOf(AnimalTraits traits, String speciesId) {
        return orCurrent(traits.sizeRange().orElse(null), speciesId);
    }

    public static SizeSettings orCurrent(@Nullable SizeSettings stored, String speciesId) {
        return stored != null ? stored : BarnyardConfig.current().sizeFor(speciesId);
    }
}
