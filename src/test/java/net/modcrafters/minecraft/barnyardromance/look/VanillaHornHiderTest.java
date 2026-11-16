package net.modcrafters.minecraft.barnyardromance.look;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaHornHiderTest {
    private final ModelPart tip = new ModelPart(List.of(), Map.of());
    private final ModelPart horns = new ModelPart(List.of(), Map.of("tip", tip));
    private final ModelPart root = new ModelPart(List.of(), Map.of("horns", horns));

    private Runnable hideHorns() {
        return VanillaHornHider.hide(this::find, List.of(new HideRule(List.of("horns"), null, null)));
    }

    private ModelPart find(List<String> path) {
        ModelPart part = root;
        for (String name : path) {
            if (!part.hasChild(name)) {
                return null;
            }
            part = part.getChild(name);
        }
        return part;
    }

    // Fresh Animations' cow animation sets its horns' visible flag on every draw, after we hid them; it can't set
    // skipDraw, so the hidden part and everything under it skip their cubes.
    @Test
    void pathRuleSkipsThePartAndItsChildrenEvenWhenMadeVisibleAgain() {
        hideHorns();
        horns.visible = true;

        assertTrue(horns.skipDraw);
        assertTrue(tip.skipDraw);
    }

    @Test
    void pathRuleStillHidesThePart() {
        hideHorns();

        assertFalse(horns.visible);
    }

    @Test
    void undoRestoresVisibleAndSkipDraw() {
        hideHorns().run();

        assertTrue(horns.visible);
        assertFalse(horns.skipDraw);
        assertFalse(tip.skipDraw);
    }

    @Test
    void undoKeepsSkipDrawAPartAlreadyHad() {
        tip.skipDraw = true;

        hideHorns().run();

        assertTrue(tip.skipDraw);
    }
}
