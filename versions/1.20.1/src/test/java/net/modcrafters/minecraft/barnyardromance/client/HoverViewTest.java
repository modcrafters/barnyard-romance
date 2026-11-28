package net.modcrafters.minecraft.barnyardromance.client;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 1.20.1 only: the hover card's anchor is projected through the view the game draws the world with. A point the
// player looks straight at sits straight ahead in the view (OpenGL's -z), whichever way they face.
class HoverViewTest {
    private static final float EPSILON = 1.0e-4F;

    @Test
    void whatThePlayerLooksAtIsStraightAhead() {
        for (float yaw : new float[]{0.0F, 45.0F, 90.0F, 180.0F, -90.0F, 270.0F}) {
            for (float pitch : new float[]{0.0F, 20.0F, -35.0F, 80.0F}) {
                Vector3f view = AnimalHoverTooltip.viewRotation(pitch, yaw).transformDirection(look(yaw, pitch));
                String where = "yaw " + yaw + ", pitch " + pitch + ": " + view;
                assertEquals(0.0F, view.x, EPSILON, where);
                assertEquals(0.0F, view.y, EPSILON, where);
                assertEquals(-1.0F, view.z, EPSILON, where);
            }
        }
    }

    @Test
    void aPointAboveWhatThePlayerLooksAtIsHigherOnScreen() {
        Vector3f view = AnimalHoverTooltip.viewRotation(0.0F, 180.0F).transformDirection(new Vector3f(0.0F, 1.0F, -1.0F));
        assertEquals(1.0F, view.y, EPSILON);
        assertEquals(-1.0F, view.z, EPSILON);
    }

    // Entity.calculateViewVector: yaw 0 faces south (+z), positive pitch looks down.
    private static Vector3f look(float yaw, float pitch) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        return new Vector3f((float) (-Math.sin(y) * Math.cos(p)), (float) -Math.sin(p), (float) (Math.cos(y) * Math.cos(p)));
    }
}
