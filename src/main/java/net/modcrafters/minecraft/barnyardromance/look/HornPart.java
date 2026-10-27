package net.modcrafters.minecraft.barnyardromance.look;

import java.util.List;

// Rotation is in degrees, applied Z, then Y, then X, like vanilla model parts. Children are placed relative to it.
public record HornPart(Xyz pivot, Xyz rotation, HornSide side, List<HornCube> cubes, List<HornPart> children) {
    public HornPart {
        cubes = List.copyOf(cubes);
        children = List.copyOf(children);
    }
}
