package net.modcrafters.minecraft.barnyardromance.mixin.client;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 1.21.1 only: cow, sheep and goat models keep their head and body as fields, with no root part above them.
@Mixin(QuadrupedModel.class)
public interface QuadrupedModelAccessor {
    @Accessor("head")
    ModelPart barnyardromance$head();

    @Accessor("body")
    ModelPart barnyardromance$body();
}
