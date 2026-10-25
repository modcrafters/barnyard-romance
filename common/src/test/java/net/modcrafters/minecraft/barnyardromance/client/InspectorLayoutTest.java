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
        assertTrue(InspectorLayout.barX() > InspectorLayout.STATS_X);
        assertTrue(InspectorLayout.valueX() + 40 < InspectorLayout.WIDTH - InspectorLayout.BORDER);
        assertTrue(InspectorLayout.PORTRAIT_X + InspectorLayout.PORTRAIT < InspectorLayout.STATS_X);
    }

    @Test
    void everythingFitsInsideThePanelBorder() {
        int lastLine = InspectorLayout.ROW_BREEDING + InspectorLayout.DETAIL_OFFSET + 3 + 9;
        int twoHeartRows = InspectorLayout.ROW_HEALTH + 2 + 10 + 9;

        assertTrue(lastLine <= InspectorLayout.HEIGHT - InspectorLayout.BORDER);
        assertTrue(InspectorLayout.PORTRAIT_Y + InspectorLayout.PORTRAIT + 1 <= InspectorLayout.HEIGHT - InspectorLayout.BORDER);
        assertTrue(twoHeartRows < InspectorLayout.ROW_FERTILITY);
        assertTrue(InspectorLayout.ROW_GROWTH + 14 + 9 <= InspectorLayout.ROW_WEIGHT);
        assertTrue(InspectorLayout.ROW_WEIGHT + 16 <= InspectorLayout.ROW_HEALTH);
        assertTrue(InspectorLayout.ROW_FERTILITY + 16 <= InspectorLayout.ROW_BREEDING);
        assertTrue(InspectorLayout.TITLE_Y + 9 < InspectorLayout.PORTRAIT_Y);
    }
}
