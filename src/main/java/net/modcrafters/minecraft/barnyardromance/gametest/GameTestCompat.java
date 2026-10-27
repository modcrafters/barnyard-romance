package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

// Game test steps that look different on older versions. versions/1.21.1 has the same class for 1.21.1.
public final class GameTestCompat {
    private GameTestCompat() {
    }

    // Thrown to fail the running test with this message.
    public static RuntimeException failure(GameTestHelper helper, String message) {
        return helper.assertionException(Component.literal(message));
    }

    // A right click on the mob with the player's main hand.
    public static InteractionResult interact(Mob mob, Player player) {
        return mob.interact(player, InteractionHand.MAIN_HAND, mob.position());
    }

    // Spawned at a position relative to the test, like a natural mob that may despawn.
    public static <T extends Mob> T spawnDespawnable(GameTestHelper helper, EntityType<T> type, Vec3 position) {
        return helper.spawnEntity(type, position).requirePersistence(false).spawn();
    }

    // The pet's owner is this player.
    public static boolean ownedBy(TamableAnimal pet, Player player) {
        return pet.isOwnedBy(player);
    }

    // Spawned at a block position relative to the test, standing on the pen's floor at y 1.
    public static <T extends Entity> T spawn(GameTestHelper helper, EntityType<T> type, int x, int y, int z) {
        return helper.spawn(type, x, y, z);
    }
}
