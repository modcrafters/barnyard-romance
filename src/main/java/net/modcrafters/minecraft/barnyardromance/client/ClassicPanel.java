package net.modcrafters.minecraft.barnyardromance.client;

import java.util.ArrayList;
import java.util.List;

// The light grey bevelled panel behind vanilla containers, drawn with fills so no texture can go missing.
public final class ClassicPanel {
    public static final int OUTLINE = 0xFF000000;
    public static final int LIGHT = 0xFFFFFFFF;
    public static final int FACE = 0xFFC6C6C6;
    public static final int SHADOW = 0xFF555555;

    private ClassicPanel() {
    }

    public record Rect(int x0, int y0, int x1, int y1, int color) {
    }

    public static List<Rect> rects(int w, int h) {
        List<Rect> rects = new ArrayList<>();
        rects.add(new Rect(3, 3, w - 3, h - 3, FACE));
        rects.add(new Rect(w - 3, 2, w - 2, 3, FACE));
        rects.add(new Rect(2, h - 3, 3, h - 2, FACE));

        rects.add(new Rect(2, 1, w - 3, 2, LIGHT));
        rects.add(new Rect(1, 2, w - 3, 3, LIGHT));
        rects.add(new Rect(1, 3, 3, h - 3, LIGHT));
        rects.add(new Rect(3, 3, 4, 4, LIGHT));

        rects.add(new Rect(w - 3, 3, w - 1, h - 3, SHADOW));
        rects.add(new Rect(3, h - 3, w - 1, h - 2, SHADOW));
        rects.add(new Rect(3, h - 2, w - 2, h - 1, SHADOW));
        rects.add(new Rect(w - 4, h - 4, w - 3, h - 3, SHADOW));

        rects.add(new Rect(2, 0, w - 3, 1, OUTLINE));
        rects.add(new Rect(1, 1, 2, 2, OUTLINE));
        rects.add(new Rect(w - 3, 1, w - 2, 2, OUTLINE));
        rects.add(new Rect(0, 2, 1, h - 3, OUTLINE));
        rects.add(new Rect(w - 2, 2, w - 1, 3, OUTLINE));
        rects.add(new Rect(w - 1, 3, w, h - 2, OUTLINE));
        rects.add(new Rect(1, h - 3, 2, h - 2, OUTLINE));
        rects.add(new Rect(2, h - 2, 3, h - 1, OUTLINE));
        rects.add(new Rect(w - 2, h - 2, w - 1, h - 1, OUTLINE));
        rects.add(new Rect(3, h - 1, w - 2, h, OUTLINE));
        return rects;
    }
}
