package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

// Finds model parts by a path from the model's root. versions/1.21.1 has its own copy, where models have no root.
public final class ModelParts {
    private ModelParts() {
    }

    // null when the model has no such part.
    public static ModelPart find(Model<?> model, List<String> path) {
        ModelPart part = model.root();
        for (String name : path) {
            if (!part.hasChild(name)) {
                return null;
            }
            part = part.getChild(name);
        }
        return part;
    }

    // Moves the pose into the part's space, the way vanilla attaches a held item or a worn head. False when missing.
    public static boolean poseTo(Model<?> model, List<String> path, PoseStack poseStack) {
        ModelPart part = model.root();
        part.translateAndRotate(poseStack);
        for (String name : path) {
            if (!part.hasChild(name)) {
                return false;
            }
            part = part.getChild(name);
            part.translateAndRotate(poseStack);
        }
        return true;
    }
}
