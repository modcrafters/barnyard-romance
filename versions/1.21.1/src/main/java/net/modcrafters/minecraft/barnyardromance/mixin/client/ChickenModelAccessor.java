package net.modcrafters.minecraft.barnyardromance.mixin.client;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 1.21.1 only: the chicken model keeps its head, beak and wattle as separate fields, with no root part above them.
@Mixin(ChickenModel.class)
public interface ChickenModelAccessor {
    @Accessor("head")
    ModelPart barnyardromance$head();

    @Accessor("beak")
    ModelPart barnyardromance$beak();

    @Accessor("redThing")
    ModelPart barnyardromance$redThing();
}
