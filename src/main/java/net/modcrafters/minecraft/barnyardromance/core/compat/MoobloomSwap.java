package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

// Whether MoobloomConversionMixin's swap of the mod's cow (its @Redirect) was woven into transformToCow.
// The mixin carries a moobloom's record only when it was, because a carry without the swap would move the herd book to
// a cow that never joins the level. Set by CompatMixinPlugin.postApply, before any moobloom is sheared.
public final class MoobloomSwap {
    static final String METHOD = "transformToCow";
    // The handler keeps this in its name when Mixin renames it.
    static final String HANDLER = "barnyardromance$sameCow";

    private static volatile boolean applied;

    private MoobloomSwap() {
    }

    public static boolean applied() {
        return applied;
    }

    static void record(ClassNode target) {
        applied = hasSwap(target);
    }

    // Whether the method has a call to the swap handler.
    static boolean hasSwap(ClassNode target) {
        for (MethodNode method : target.methods) {
            if (!method.name.equals(METHOD)) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.name.contains(HANDLER)) {
                    return true;
                }
            }
        }
        return false;
    }
}
