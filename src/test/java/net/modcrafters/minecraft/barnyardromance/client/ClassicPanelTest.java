package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassicPanelTest {
    private static final int W = 176;
    private static final int H = 222;

    // Corners copied from vanilla 26.3 textures/gui/container/generic_54.png (176x222).
    // . clear, K outline, L light, F face, S shadow
    private static final String[] TOP_LEFT = {
            "..KKKK",
            ".KLLLL",
            "KLLLLL",
            "KLLLFF",
            "KLLFFF",
            "KLLFFF"
    };
    private static final String[] TOP_RIGHT = {
            "KKK...",
            "LLLK..",
            "LLLFK.",
            "FFFSSK",
            "FFFSSK",
            "FFFSSK"
    };
    private static final String[] BOTTOM_LEFT = {
            "KLLFFF",
            "KLLFFF",
            "KLLFFF",
            ".KFSSS",
            "..KSSS",
            "...KKK"
    };
    private static final String[] BOTTOM_RIGHT = {
            "FFFSSK",
            "FFFSSK",
            "FFSSSK",
            "SSSSSK",
            "SSSSK.",
            "KKKK.."
    };

    private static char[][] draw(int w, int h) {
        char[][] grid = new char[h][w];
        for (char[] row : grid) {
            java.util.Arrays.fill(row, '.');
        }
        for (ClassicPanel.Rect rect : ClassicPanel.rects(w, h)) {
            char c = switch (rect.color()) {
                case ClassicPanel.OUTLINE -> 'K';
                case ClassicPanel.LIGHT -> 'L';
                case ClassicPanel.FACE -> 'F';
                case ClassicPanel.SHADOW -> 'S';
                default -> '?';
            };
            for (int y = rect.y0(); y < rect.y1(); y++) {
                for (int x = rect.x0(); x < rect.x1(); x++) {
                    grid[y][x] = c;
                }
            }
        }
        return grid;
    }

    private static void assertCorner(String[] expected, char[][] grid, int x0, int y0) {
        for (int y = 0; y < expected.length; y++) {
            assertEquals(expected[y], new String(grid[y0 + y], x0, expected[y].length()), "row " + (y0 + y));
        }
    }

    @Test
    void cornersMatchTheVanillaChestPanel() {
        char[][] grid = draw(W, H);

        assertCorner(TOP_LEFT, grid, 0, 0);
        assertCorner(TOP_RIGHT, grid, W - 6, 0);
        assertCorner(BOTTOM_LEFT, grid, 0, H - 6);
        assertCorner(BOTTOM_RIGHT, grid, W - 6, H - 6);
    }

    @Test
    void edgesAndMiddleAreSolid() {
        char[][] grid = draw(W, H);

        assertEquals("KLLFFF", new String(grid[H / 2], 0, 6));
        assertEquals("FFFSSK", new String(grid[H / 2], W - 6, 6));
        assertEquals('K', grid[0][W / 2]);
        assertEquals('L', grid[1][W / 2]);
        assertEquals('F', grid[3][W / 2]);
        assertEquals('S', grid[H - 2][W / 2]);
        assertEquals('K', grid[H - 1][W / 2]);
    }

    @Test
    void anyPanelSizeHasNoHoles() {
        char[][] grid = draw(320, 152);

        for (int y = 3; y < 149; y++) {
            for (int x = 3; x < 317; x++) {
                assertEquals('F', grid[y][x] == 'L' || grid[y][x] == 'S' ? 'F' : grid[y][x], "at " + x + "," + y);
            }
        }
    }
}
