package net.modcrafters.minecraft.barnyardromance.mixin;

import net.modcrafters.minecraft.barnyardromance.compat.environmental.EnvironmentalLitters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Environmental's EnvironmentalEvents.onBabySpawn is a static handler of NeoForge's BabyEntitySpawnEvent that, with its
// larger-litter options on (they are by default), spawns extra piglets for every pig breeding and extra hoglets for
// every hoglin breeding, on top of the litter the barnyard rolls. Read from the jar of 1.21.1 (NeoForge), the only
// build there is. The barnyard owns the litter of a species it tracks, so the handler is cancelled at its head when the
// mother is one; for a species a pack switches off it keeps its extras. The event is NeoForge's, which shared code
// can't name, so it comes in as an Object and EnvironmentalLitters reads it. require = 0 loses a hook the mod renames
// instead of crashing the game; the plugin only lists this when the mod is there.
@Pseudo
@Mixin(targets = "com.teamabnormals.environmental.core.other.EnvironmentalEvents", remap = false)
public class EnvironmentalLitterMixin {
    @Inject(method = "onBabySpawn", at = @At("HEAD"), cancellable = true, require = 0)
    private static void barnyardromance$ownLitter(@Coerce Object event, CallbackInfo callback) {
        if (EnvironmentalLitters.ownsLitter(event)) {
            callback.cancel();
        }
    }
}
