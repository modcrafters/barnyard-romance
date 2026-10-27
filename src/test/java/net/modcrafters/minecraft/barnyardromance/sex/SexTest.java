package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SexTest {
    @Test
    void eachSexHasAName() {
        assertEquals("Female", English.of(Sex.FEMALE.text()));
        assertEquals("Male", English.of(Sex.MALE.text()));
    }
}
