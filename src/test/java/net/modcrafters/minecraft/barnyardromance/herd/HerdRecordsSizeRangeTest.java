package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.nbt.CompoundTag;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyCare;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdRecordsSizeRangeTest {
    private static final SizeSettings OLD = new SizeSettings(0.8, 1.2, 0.1);

    @Test
    void aRecordedPregnancyKeepsTheFatherRange() {
        Pregnancy pregnancy = new Pregnancy(1_000L, 48_000L, "minecraft:cow", 1.25, 1, 0.5, 30_000, 0.75,
                List.of(new CompoundTag()), PregnancyCare.NONE).withFatherSizeRange(OLD);

        Pregnancy recorded = HerdRecords.withoutLitter(pregnancy);

        assertTrue(recorded.litter().isEmpty());
        assertEquals(OLD, recorded.fatherSizeRange());
    }
}
