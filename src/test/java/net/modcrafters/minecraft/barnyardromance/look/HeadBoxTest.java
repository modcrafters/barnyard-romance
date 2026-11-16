package net.modcrafters.minecraft.barnyardromance.look;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeadBoxTest {
    // Two corners of a head, in model space, as x, y, z triples.
    private static final float[] CORNERS = {-0.25f, 0.0f, -0.5f, 0.25f, 0.5f, 0.0f};

    @Test
    void boxSpansThePointsWhereTheModelIsPlaced() {
        float[] box = HeadBox.box(CORNERS, new Matrix4f().translation(1.0f, 2.0f, 3.0f));

        assertArrayEquals(new float[] {0.75f, 2.0f, 2.5f, 1.25f, 2.5f, 3.0f}, box, 1.0e-6f);
    }

    // Placement turns the model; the box is taken after turning, so it still holds every point.
    @Test
    void boxIsTakenAfterThePlacementTurns() {
        float[] box = HeadBox.box(CORNERS, new Matrix4f().rotationY((float) Math.PI));

        assertArrayEquals(new float[] {-0.25f, 0.0f, 0.0f, 0.25f, 0.5f, 0.5f}, box, 1.0e-6f);
    }

    @Test
    void noPointsIsNoBox() {
        assertNull(HeadBox.box(new float[0], new Matrix4f()));
    }
}
