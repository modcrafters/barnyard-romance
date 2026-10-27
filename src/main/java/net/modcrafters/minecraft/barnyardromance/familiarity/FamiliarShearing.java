package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
//? if <1.21.6
//import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public final class FamiliarShearing {
    private FamiliarShearing() {
    }

    public static boolean tryShear(ServerLevel level, BlockPos pos, ItemStack tool) {
        //? if >=1.21.6 {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(pos), EntitySelector.NO_SPECTATORS)) {
            if (entity.shearOffAllLeashConnections(null)) {
                return true;
            }
        //?} else {
        /*// Shears don't cut leashes before 1.21.6.
        for (Entity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), EntitySelector.NO_SPECTATORS)) {
        *///?}
            if (!entity.isAlive() || !(entity instanceof Shearable shearable) || !shearable.readyForShearing()) {
                continue;
            }
            if (entity instanceof Animal animal && FamiliarityActions.denies(animal)) {
                continue;
            }
            //? if >=1.21.2 {
            shearable.shear(level, SoundSource.BLOCKS, tool);
            //?} else
            //shearable.shear(SoundSource.BLOCKS);
            level.gameEvent(null, GameEvent.SHEAR, pos);
            return true;
        }
        return false;
    }
}
