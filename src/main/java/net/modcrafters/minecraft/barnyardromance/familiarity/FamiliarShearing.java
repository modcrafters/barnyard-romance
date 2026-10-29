// Only the shared ShearsDispenseMixin uses this; the 1.21.1 copy asks NeoForge's IShearable instead.
//? if >=1.21.2 {
package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public final class FamiliarShearing {
    private FamiliarShearing() {
    }

    public static boolean tryShear(ServerLevel level, BlockPos pos, ItemStack tool) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(pos), EntitySelector.NO_SPECTATORS)) {
            if (entity.shearOffAllLeashConnections(null)) {
                return true;
            }
            if (!entity.isAlive() || !(entity instanceof Shearable shearable) || !shearable.readyForShearing()) {
                continue;
            }
            if (entity instanceof Animal animal && (FamiliarityActions.denies(animal) || ShearActions.shearedToday(animal))) {
                continue;
            }
            shearable.shear(level, SoundSource.BLOCKS, tool);
            level.gameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }
}
//?}
