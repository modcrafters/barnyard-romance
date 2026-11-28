package net.modcrafters.minecraft.barnyardromance.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.modcrafters.minecraft.barnyardromance.look.VanillaHornHider;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 1.20.1's copy of src/.../mixin/client/ModelPartMixin: a cube draws with a colour as four floats there.
// Skips the cubes VanillaHornHider hid for the animal being drawn.
@Mixin(ModelPart.class)
public abstract class ModelPartMixin {
    @WrapOperation(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart$Cube;compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
            )
    )
    private void barnyardromance$skipHiddenCubes(
            ModelPart.Cube cube,
            PoseStack.Pose pose,
            VertexConsumer buffer,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha,
            Operation<Void> original
    ) {
        if (!VanillaHornHider.isHidden(cube)) {
            original.call(cube, pose, buffer, light, overlay, red, green, blue, alpha);
        }
    }
}
