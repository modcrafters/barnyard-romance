package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.inspect.InspectorRow.*;
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
    void everythingAboveTheListFitsInsideThePanel() {
        assertTrue(InspectorLayout.TITLE_Y + 9 < InspectorLayout.PORTRAIT_Y);
        assertTrue(InspectorLayout.HEADER_Y + 13 < InspectorLayout.ROW_START);
    }

    @Test
    void rowsStackWithNoGaps() {
        List<InspectorLayout.Placed> placed = InspectorLayout.stack(List.of(FAMILIAR, GROWTH, HEALTH, BREEDING, DETAIL), 1);

        assertEquals(List.of(38, 54, 80, 96, 108), placed.stream().map(InspectorLayout.Placed::y).toList());
        assertEquals(120, InspectorLayout.bottom(placed));
    }

    @Test
    void aHiddenRowLetsTheNextOneMoveUp() {
        List<InspectorLayout.Placed> cow = InspectorLayout.stack(List.of(WEIGHT, HEALTH, FERTILITY, BREEDING), 1);
        List<InspectorLayout.Placed> bull = InspectorLayout.stack(List.of(WEIGHT, HEALTH, BREEDING), 1);

        assertEquals(cow.get(2).y(), bull.get(2).y());
        assertEquals(BREEDING, bull.get(2).row());
    }

    @Test
    void twoRowsOfHeartsPushTheRestDown() {
        assertEquals(54, InspectorLayout.stack(List.of(HEALTH, BREEDING), 1).get(1).y());
        assertEquals(64, InspectorLayout.stack(List.of(HEALTH, BREEDING), 2).get(1).y());
    }

    @Test
    void thePanelFitsTheListButNeverCutsThePortrait() {
        int portraitBottom = InspectorLayout.PORTRAIT_Y + InspectorLayout.PORTRAIT + 1;
        List<InspectorLayout.Placed> everything = InspectorLayout.stack(List.of(FAMILIAR, FEED, GROWTH, WEIGHT, HEALTH, FERTILITY, BREEDING, DETAIL), 2);

        assertEquals(portraitBottom + InspectorLayout.BOTTOM_MARGIN, InspectorLayout.panelHeight(InspectorLayout.stack(List.of(HEALTH), 1)));
        assertEquals(InspectorLayout.bottom(everything) + InspectorLayout.BOTTOM_MARGIN, InspectorLayout.panelHeight(everything));
        assertTrue(InspectorLayout.BOTTOM_MARGIN >= InspectorLayout.BORDER);
    }
}
