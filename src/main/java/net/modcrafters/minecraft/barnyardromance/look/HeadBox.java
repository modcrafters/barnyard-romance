package net.modcrafters.minecraft.barnyardromance.look;

import org.joml.Matrix4f;
import org.joml.Vector3f;

// The herd book frames an animal's head by the corners of its cubes, measured in model space where the model was
// last drawn, then placed the way the renderer places the model.
public final class HeadBox {
    private HeadBox() {
    }

    // corners holds x, y, z triples. {minX, minY, minZ, maxX, maxY, maxZ} after placing, or null without corners.
    public static float[] box(float[] corners, Matrix4f placed) {
        if (corners.length < 3) {
            return null;
        }
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        Vector3f point = new Vector3f();
        for (int i = 0; i + 2 < corners.length; i += 3) {
            placed.transformPosition(corners[i], corners[i + 1], corners[i + 2], point);
            box[0] = Math.min(box[0], point.x);
            box[1] = Math.min(box[1], point.y);
            box[2] = Math.min(box[2], point.z);
            box[3] = Math.max(box[3], point.x);
            box[4] = Math.max(box[4], point.y);
            box[5] = Math.max(box[5], point.z);
        }
        return box;
    }
}
