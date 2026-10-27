package net.modcrafters.minecraft.barnyardromance.look;

import java.util.List;

// What to draw for one animal this frame. No parts still means vanilla's horns get hidden: the animal is hornless.
public record HornDraw(String entityId, LoadedHorns horns, List<LoadedHorns.Baked> parts, float growth) {
    public HornDraw {
        parts = List.copyOf(parts);
    }
}
