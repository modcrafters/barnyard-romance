package net.modcrafters.minecraft.barnyardromance.mixin.client;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Normal, warm and baby cows have their horns as cubes inside the head, so hiding them needs the cube list.
@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("cubes")
    List<ModelPart.Cube> barnyardromance$cubes();
}
