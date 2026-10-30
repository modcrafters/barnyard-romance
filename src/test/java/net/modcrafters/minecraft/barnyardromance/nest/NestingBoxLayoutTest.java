package net.modcrafters.minecraft.barnyardromance.nest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NestingBoxLayoutTest {
    @Test
    void nestsAreFourCentredRowsOfFour() {
        assertEquals(NestingBoxLayout.nestX(0), NestingBoxLayout.nestX(12));
        assertEquals(NestingBoxLayout.nestY(0) + 3 * NestingBoxLayout.ROW_STEP, NestingBoxLayout.nestY(12));
        int left = NestingBoxLayout.nestX(0);
        int right = NestingBoxLayout.nestX(3) + 16;
        assertEquals(NestingBoxLayout.WIDTH / 2, (left + right) / 2);
    }

    @Test
    void countdownsFitAboveTheInventoryLabel() {
        int lastTimerBottom = NestingBoxLayout.nestY(15) + 18 + 9;
        assertTrue(lastTimerBottom <= NestingBoxLayout.INVENTORY_LABEL_Y, "timers end at " + lastTimerBottom);
    }

    @Test
    void timerIsMinutesAndSeconds() {
        assertEquals("16:40", NestingBoxLayout.timer(20_000L));
        assertEquals("0:01", NestingBoxLayout.timer(1L));
        assertEquals("0:00", NestingBoxLayout.timer(0L));
        assertEquals("1:05", NestingBoxLayout.timer(1_300L));
    }
}
