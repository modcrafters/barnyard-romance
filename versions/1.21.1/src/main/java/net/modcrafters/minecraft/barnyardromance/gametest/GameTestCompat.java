package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

// The 1.21.1 copy of src/.../gametest/GameTestCompat.
public final class GameTestCompat {
    private GameTestCompat() {
    }

    public static RuntimeException failure(GameTestHelper helper, String message) {
        return new GameTestAssertException(message);
    }

    public static InteractionResult interact(Mob mob, Player player) {
        return mob.interact(player, InteractionHand.MAIN_HAND);
    }

    // GameTestHelper.spawn always makes the mob persistent here, so this adds it to the level directly.
    public static <T extends Mob> T spawnDespawnable(GameTestHelper helper, EntityType<T> type, Vec3 position) {
        T mob = Objects.requireNonNull(type.create(helper.getLevel()), () -> "could not create " + type);
        Vec3 absolute = helper.absoluteVec(position);
        mob.moveTo(absolute.x, absolute.y, absolute.z, mob.getYRot(), mob.getXRot());
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }
}
