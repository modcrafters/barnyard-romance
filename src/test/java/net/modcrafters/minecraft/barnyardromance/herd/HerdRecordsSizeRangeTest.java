package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.nbt.CompoundTag;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCare;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdRecordsSizeRangeTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);
    private static final SizeSettings COW = new SizeSettings(0.85, 1.15, 0.1);
    private static final SizeSettings HORSE = new SizeSettings(0.9, 1.1, 0.05);
    private static final SizeSettings DONKEY = new SizeSettings(0.7, 0.9, 0.08);
    private static final Function<String, SizeSettings> RANGE_FOR = Map.of(
            "minecraft:cow", COW,
            "minecraft:horse", HORSE,
            "minecraft:donkey", DONKEY
    )::get;

    @Test
    void aRecordedPregnancyKeepsTheFatherRange() {
        Pregnancy pregnancy = pregnancy("minecraft:cow").withFatherSizeRange(OLD);

        Pregnancy recorded = HerdRecords.withoutLitter(pregnancy);

        assertTrue(recorded.litter().isEmpty());
        assertEquals(OLD, recorded.fatherSizeRange());
    }

    @Test
    void anEmptyRecordGetsItsSpeciesRange() {
        HerdRecord record = record("minecraft:cow", AnimalTraits.of(Sex.FEMALE, 0.9), null);

        HerdRecord filled = HerdRecords.withSizeRanges(record, RANGE_FOR);

        assertEquals(Optional.of(COW), filled.traits().sizeRange());
        assertEquals(0.9, filled.traits().scale());
    }

    @Test
    void aPregnancyFatherGetsHisSpeciesRange() {
        HerdRecord record = record("minecraft:horse", AnimalTraits.of(Sex.FEMALE, 0.9).withSizeRange(OLD), pregnancy("minecraft:donkey"));

        HerdRecord filled = HerdRecords.withSizeRanges(record, RANGE_FOR);

        assertEquals(DONKEY, filled.pregnancy().fatherSizeRange());
        assertEquals(Optional.of(OLD), filled.traits().sizeRange());
    }

    @Test
    void aRecordWithRangesIsReturnedAsIs() {
        HerdRecord record = record("minecraft:horse", AnimalTraits.of(Sex.FEMALE, 0.9).withSizeRange(OLD),
                pregnancy("minecraft:donkey").withFatherSizeRange(OLD));

        assertSame(record, HerdRecords.withSizeRanges(record, RANGE_FOR));
    }

    private static Pregnancy pregnancy(String fatherSpecies) {
        return new Pregnancy(1_000L, 48_000L, fatherSpecies, 1.25, 1, 0.5, 30_000, 0.75,
                List.of(new CompoundTag()), PregnancyCare.NONE);
    }

    private static HerdRecord record(String species, AnimalTraits traits, @Nullable Pregnancy pregnancy) {
        return new HerdRecord(species, "", traits, pregnancy, false, true, false, false, 0L, -1L, -1L, true);
    }
}
