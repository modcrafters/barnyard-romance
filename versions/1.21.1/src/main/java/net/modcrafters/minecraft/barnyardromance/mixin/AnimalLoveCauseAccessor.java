package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

// The 1.21.1 copy of src/.../mixin/AnimalLoveCauseAccessor: the love cause is a bare UUID here.
@Mixin(Animal.class)
public interface AnimalLoveCauseAccessor {
    @Accessor("loveCause")
    @Nullable UUID barnyardromance$loveCause();
}
