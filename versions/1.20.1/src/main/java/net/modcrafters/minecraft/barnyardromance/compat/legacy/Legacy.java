package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.modcrafters.minecraft.barnyardromance.mixin.EntityRandomAccessor;
import net.modcrafters.minecraft.barnyardromance.mixin.MobGoalsAccessor;
import org.jspecify.annotations.Nullable;

// Methods later versions have on vanilla classes, written out the way they do it for Minecraft 1.20.1. Stonecutter
// writes these calls in place of the vanilla ones on 1.20.1 (see stonecutter.gradle.kts).
public final class Legacy {
    private Legacy() {
    }

    // ItemStack.consume: a player in creative keeps the item.
    public static void consume(ItemStack stack, int amount, @Nullable LivingEntity entity) {
        if (entity == null || !(entity instanceof Player player && player.getAbilities().instabuild)) {
            stack.shrink(amount);
        }
    }

    // PathfinderMob.isPanicking: a brain's panic memory, or a running panic goal.
    public static boolean isPanicking(PathfinderMob mob) {
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING)) {
            return mob.getBrain().getMemory(MemoryModuleType.IS_PANICKING).isPresent();
        }
        for (WrappedGoal goal : ((MobGoalsAccessor) mob).barnyardromance$goalSelector().getAvailableGoals()) {
            if (goal.isRunning() && goal.getGoal() instanceof PanicGoal) {
                return true;
            }
        }
        return false;
    }

    // Entity.isInLiquid.
    public static boolean isInLiquid(Entity entity) {
        return entity.isInWaterOrBubble() || entity.isInLava();
    }

    // Containers.dropContentsOnDestroy: a container block that is replaced by another block drops what it holds.
    public static void dropContentsOnDestroy(BlockState state, BlockState newState, Level level, BlockPos pos) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    // Entity.getRandom: only living entities have the getter here.
    public static RandomSource random(Entity entity) {
        return ((EntityRandomAccessor) entity).barnyardromance$random();
    }
}
