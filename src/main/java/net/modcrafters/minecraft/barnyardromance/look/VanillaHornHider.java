package net.modcrafters.minecraft.barnyardromance.look;

import net.modcrafters.minecraft.barnyardromance.mixin.client.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// Hides vanilla's horns while one animal's model is drawn. Every animal of a kind shares one model, so the
// caller undoes it straight after the draw. Only the render thread draws models.
public final class VanillaHornHider {
    private static final float MATCH = 0.01f;
    private static final Set<ModelPart.Cube> HIDDEN = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Runnable NOTHING = () -> {
    };

    private VanillaHornHider() {
    }

    // Read by ModelPartMixin for every cube drawn, so the empty case stays cheap.
    public static boolean isHidden(ModelPart.Cube cube) {
        return !HIDDEN.isEmpty() && HIDDEN.contains(cube);
    }

    // Returns the undo. Rules whose part the model does not have are skipped.
    public static Runnable hide(Function<List<String>, ModelPart> find, List<HideRule> rules) {
        if (rules.isEmpty()) {
            return NOTHING;
        }
        List<ModelPart> parts = new ArrayList<>();
        List<ModelPart.Cube> cubes = new ArrayList<>();
        for (HideRule rule : rules) {
            ModelPart part = find.apply(rule.path());
            if (part == null) {
                continue;
            }
            if (rule.size() == null) {
                if (part.visible) {
                    part.visible = false;
                    parts.add(part);
                }
                continue;
            }
            for (ModelPart.Cube cube : ((ModelPartAccessor) (Object) part).barnyardromance$cubes()) {
                if (matches(cube, rule) && HIDDEN.add(cube)) {
                    cubes.add(cube);
                }
            }
        }
        if (parts.isEmpty() && cubes.isEmpty()) {
            return NOTHING;
        }
        return () -> {
            parts.forEach(part -> part.visible = true);
            cubes.forEach(HIDDEN::remove);
        };
    }

    private static boolean matches(ModelPart.Cube cube, HideRule rule) {
        Xyz size = rule.size();
        if (!near(cube.maxX - cube.minX, size.x()) || !near(cube.maxY - cube.minY, size.y()) || !near(cube.maxZ - cube.minZ, size.z())) {
            return false;
        }
        Xyz from = rule.from();
        return from == null || (near(cube.minX, from.x()) && near(cube.minY, from.y()) && near(cube.minZ, from.z()));
    }

    private static boolean near(float a, float b) {
        return Math.abs(a - b) < MATCH;
    }
}
