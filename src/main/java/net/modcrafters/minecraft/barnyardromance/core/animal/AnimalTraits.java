package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

// What the mod keeps on an animal: the core facts every feature reads, and each feature's own slice (see AnimalSlice).
// Immutable; every with returns a copy.
public final class AnimalTraits {
    private final Sex sex;
    private final double scale;
    private final int growthTicks;
    private final double weight;
    private final long bornTick;
    private final Lineage lineage;
    // The size range the scale was set in (see SizeRanges), or null for a record saved before ranges were kept.
    private final @Nullable SizeSettings sizeRange;
    // Only slices set away from their default, so a record that set a slice back to its default equals one that never did.
    private final Map<AnimalSlice<?>, Object> slices;

    AnimalTraits(Sex sex, double scale, int growthTicks, double weight, long bornTick, Lineage lineage, @Nullable SizeSettings sizeRange,
            Map<AnimalSlice<?>, Object> slices) {
        this.sex = Objects.requireNonNull(sex);
        this.scale = scale;
        this.growthTicks = growthTicks;
        this.weight = weight;
        this.bornTick = bornTick;
        this.lineage = Objects.requireNonNull(lineage);
        this.sizeRange = sizeRange;
        this.slices = Map.copyOf(slices);
    }

    // A record with every other fact at its default; it grows at the wild rate.
    public static AnimalTraits of(Sex sex, double scale) {
        return new AnimalTraits(sex, scale, Growth.wild(), 0.5, -1L, Lineage.UNKNOWN, null, Map.of());
    }

    public Sex sex() {
        return sex;
    }

    public double scale() {
        return scale;
    }

    public int growthTicks() {
        return growthTicks;
    }

    public double weight() {
        return weight;
    }

    // The game tick the animal was born, or -1 when unknown; an unknown birth never ages (see Aging).
    public long bornTick() {
        return bornTick;
    }

    public Lineage lineage() {
        return lineage;
    }

    public Optional<SizeSettings> sizeRange() {
        return Optional.ofNullable(sizeRange);
    }

    // The slice's value, or its default when the record never set it.
    public <T> T get(AnimalSlice<T> slice) {
        Object value = slices.get(slice);
        return value == null ? slice.defaultValue() : castTo(slice, value);
    }

    public <T> AnimalTraits with(AnimalSlice<T> slice, T value) {
        Map<AnimalSlice<?>, Object> next = new HashMap<>(slices);
        if (Objects.equals(value, slice.defaultValue())) {
            next.remove(slice);
        } else {
            next.put(slice, Objects.requireNonNull(value));
        }
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, next);
    }

    public AnimalTraits withScale(double scale) {
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, slices);
    }

    public AnimalTraits withSizeRange(SizeSettings range) {
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, Objects.requireNonNull(range), slices);
    }

    public AnimalTraits withGrowthTicks(int growthTicks) {
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, slices);
    }

    public AnimalTraits withWeight(double weight) {
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, slices);
    }

    public AnimalTraits withBornTick(long tick) {
        return new AnimalTraits(sex, scale, growthTicks, weight, tick, lineage, sizeRange, slices);
    }

    public AnimalTraits withLineage(Lineage lineage) {
        return new AnimalTraits(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, slices);
    }

    // with only ever stores a T under an AnimalSlice<T>.
    @SuppressWarnings("unchecked")
    private static <T> T castTo(AnimalSlice<T> slice, Object value) {
        return (T) value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnimalTraits traits
                && sex == traits.sex
                && Double.compare(scale, traits.scale) == 0
                && growthTicks == traits.growthTicks
                && Double.compare(weight, traits.weight) == 0
                && bornTick == traits.bornTick
                && lineage.equals(traits.lineage)
                && Objects.equals(sizeRange, traits.sizeRange)
                && slices.equals(traits.slices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sex, scale, growthTicks, weight, bornTick, lineage, sizeRange, slices);
    }

    @Override
    public String toString() {
        Map<String, Object> named = new TreeMap<>();
        slices.forEach((slice, value) -> named.put(slice.name(), value));
        return "AnimalTraits[sex=" + sex + ", scale=" + scale + ", growthTicks=" + growthTicks + ", weight=" + weight
                + ", bornTick=" + bornTick + ", lineage=" + lineage + ", sizeRange=" + sizeRange + ", slices=" + named + "]";
    }
}
