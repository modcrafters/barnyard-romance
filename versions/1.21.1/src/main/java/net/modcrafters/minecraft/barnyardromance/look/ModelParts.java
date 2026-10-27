package net.modcrafters.minecraft.barnyardromance.look;

import com.mojang.blaze3d.vertex.PoseStack;
import net.modcrafters.minecraft.barnyardromance.mixin.client.QuadrupedModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

// 1.21.1's copy: a path starts at the root of a hierarchical model, or at the head or body of a quadruped model,
// whose parts are drawn straight in model space.
public final class ModelParts {
    private ModelParts() {
    }

    // null when the model has no such part.
    public static ModelPart find(EntityModel<?> model, List<String> path) {
        if (model instanceof HierarchicalModel<?> hierarchical) {
            return walk(hierarchical.root(), path, 0, null);
        }
        ModelPart top = quadrupedPart(model, path.getFirst());
        return top == null ? null : walk(top, path, 1, null);
    }

    // Moves the pose into the part's space, the way vanilla attaches a held item or a worn head. False when missing.
    public static boolean poseTo(EntityModel<?> model, List<String> path, PoseStack poseStack) {
        if (model instanceof HierarchicalModel<?> hierarchical) {
            ModelPart root = hierarchical.root();
            root.translateAndRotate(poseStack);
            return walk(root, path, 0, poseStack) != null;
        }
        ModelPart top = quadrupedPart(model, path.getFirst());
        if (top == null) {
            return false;
        }
        top.translateAndRotate(poseStack);
        return walk(top, path, 1, poseStack) != null;
    }

    private static ModelPart quadrupedPart(EntityModel<?> model, String name) {
        if (!(model instanceof QuadrupedModel<?> quadruped)) {
            return null;
        }
        QuadrupedModelAccessor parts = (QuadrupedModelAccessor) quadruped;
        return switch (name) {
            case "head" -> parts.barnyardromance$head();
            case "body" -> parts.barnyardromance$body();
            default -> null;
        };
    }

    private static ModelPart walk(ModelPart part, List<String> path, int from, PoseStack poseStack) {
        for (String name : path.subList(from, path.size())) {
            if (!part.hasChild(name)) {
                return null;
            }
            part = part.getChild(name);
            if (poseStack != null) {
                part.translateAndRotate(poseStack);
            }
        }
        return part;
    }
}
