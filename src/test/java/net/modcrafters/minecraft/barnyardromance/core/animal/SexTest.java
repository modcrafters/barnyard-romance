package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.modcrafters.minecraft.barnyardromance.core.text.Names;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.text.English;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SexTest {
    @Test
    void eachSexHasAName() {
        assertEquals("Female", English.of(Names.of(Sex.FEMALE)));
        assertEquals("Male", English.of(Names.of(Sex.MALE)));
    }
}
