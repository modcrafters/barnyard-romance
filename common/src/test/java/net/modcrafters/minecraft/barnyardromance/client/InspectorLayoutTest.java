package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InspectorLayoutTest {
    @Test
    void theGrowthMarkerSitsOnTheBar() {
        int slow = InspectorLayout.markerX(0, 100, 0.0);
        int mid = InspectorLayout.markerX(0, 100, 0.4);
        int fast = InspectorLayout.markerX(0, 100, 1.0);

        assertEquals(0, slow);
        assertEquals(38, mid);
        assertEquals(94, fast);
        assertTrue(mid < 50);
    }

    @Test
    void thePortraitLeavesRoomForTheWholeAnimal() {
        int zoomedToTheFace = Math.round(76 / 0.9F);
        int fitted = InspectorLayout.portraitScale(76, 0.9F, 0.9F);

        assertTrue(fitted <= zoomedToTheFace / 2);
        assertTrue(fitted >= 8);
    }

    @Test
    void theStatColumnStaysInsideThePanel() {
        int valueStart = InspectorLayout.barX() + InspectorLayout.BAR_W + 6;
        assertTrue(InspectorLayout.barX() > InspectorLayout.STATS_X);
        assertTrue(valueStart + 40 < InspectorLayout.WIDTH - 8);
        assertTrue(InspectorLayout.NAME_Y > InspectorLayout.PORTRAIT_Y + InspectorLayout.PORTRAIT);
    }
}
