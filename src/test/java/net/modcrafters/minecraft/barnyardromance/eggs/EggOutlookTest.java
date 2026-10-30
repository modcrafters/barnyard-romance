package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.EggSettings;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EggOutlookTest {
    @Test
    void fedFamiliarHenWithRoosterAndBoxLaysFertilizedEggs() {
        EggOutlook outlook = EggOutlook.tomorrow(SizeClass.L, true, true, RoosterNear.FED, true, EggSettings.CHICKEN);

        assertEquals(EggOutlook.Kind.FERTILIZED, outlook.kind());
        assertEquals(3, outlook.count());
        assertEquals(List.of(), outlook.reasons());
        assertEquals("Fertilized eggs tomorrow (3)", English.of(outlook.tooltip()));
    }

    @Test
    void noRoosterMeansPlainEggs() {
        EggOutlook outlook = EggOutlook.tomorrow(SizeClass.M, true, true, RoosterNear.NONE, true, EggSettings.CHICKEN);

        assertEquals(EggOutlook.Kind.PLAIN, outlook.kind());
        assertEquals(List.of(EggOutlook.Reason.NO_ROOSTER), outlook.reasons());
        assertEquals("Plain eggs tomorrow (2). No rooster nearby", English.of(outlook.tooltip()));
    }

    @Test
    void hungryRoosterOrNoBoxMeansPlainEggs() {
        assertEquals(List.of(EggOutlook.Reason.ROOSTER_HUNGRY),
                EggOutlook.tomorrow(SizeClass.M, true, true, RoosterNear.HUNGRY, true, EggSettings.CHICKEN).reasons());
        assertEquals(List.of(EggOutlook.Reason.NO_BOX),
                EggOutlook.tomorrow(SizeClass.M, true, true, RoosterNear.FED, false, EggSettings.CHICKEN).reasons());
    }

    @Test
    void unfamiliarHenLaysOneFewerAndSaysSo() {
        EggOutlook outlook = EggOutlook.tomorrow(SizeClass.M, true, false, RoosterNear.FED, true, EggSettings.CHICKEN);

        assertEquals(EggOutlook.Kind.FERTILIZED, outlook.kind());
        assertEquals(1, outlook.count());
        assertEquals(List.of(EggOutlook.Reason.NOT_FAMILIAR), outlook.reasons());
        assertEquals("Fertilized eggs tomorrow (1). Not familiar: one egg fewer", English.of(outlook.tooltip()));
    }

    @Test
    void unfedHenLaysNothingTomorrow() {
        EggOutlook outlook = EggOutlook.tomorrow(SizeClass.XL, false, true, RoosterNear.FED, true, EggSettings.CHICKEN);

        assertEquals(EggOutlook.Kind.NO_EGGS, outlook.kind());
        assertEquals(0, outlook.count());
        assertEquals(List.of(EggOutlook.Reason.NOT_FED), outlook.reasons());
        assertEquals("No eggs tomorrow. Not fed today", English.of(outlook.tooltip()));
    }
}
