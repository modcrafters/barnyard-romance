package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoobloomSwapTest {
    private static ClassNode classWith(String method, String called) {
        ClassNode node = new ClassNode();
        MethodNode m = new MethodNode(Opcodes.ACC_PRIVATE, method, "()V", null, null);
        InsnList code = new InsnList();
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "x/Y", called, "()V", false));
        m.instructions = code;
        node.methods.add(m);
        return node;
    }

    @Test
    void seesTheSwapHandlerCalledInTransformToCow() {
        assertTrue(MoobloomSwap.hasSwap(classWith("transformToCow", "handler$zzz000$barnyard_romance$barnyardromance$sameCow")));
    }

    @Test
    void missesItWhenItIsNotThereOrIsInAnotherMethod() {
        assertFalse(MoobloomSwap.hasSwap(classWith("transformToCow", "discard")));
        assertFalse(MoobloomSwap.hasSwap(classWith("shear", "barnyardromance$sameCow")));
    }
}
