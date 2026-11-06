package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatCardLayoutTest {
    @Test
    void heightGrowsByALinePerRow() {
        assertEquals(StatCardLayout.height(7) + StatCardLayout.LINE, StatCardLayout.height(8));
    }

    @Test
    void widthNeverPassesTheCap() {
        assertEquals(StatCardLayout.MAX_WIDTH, StatCardLayout.width(50, List.of(400)));
    }

    @Test
    void fitsAGuiScaledSmallScreen() {
        int width = StatCardLayout.width(60, List.of(120, 90));
        int height = StatCardLayout.height(14);
        ScreenPoint origin = TooltipPlacement.place(300, 10, width, height, 320, 240);
        assertTrue(origin.x() >= 0 && origin.x() + width <= 320, "x " + origin.x());
        assertTrue(origin.y() >= 0 && origin.y() + height <= 240, "y " + origin.y());
    }
}
