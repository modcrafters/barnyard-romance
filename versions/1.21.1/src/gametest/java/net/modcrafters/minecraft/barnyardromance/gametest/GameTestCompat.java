package net.modcrafters.minecraft.barnyardromance.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// The 1.21.1 copy of src/.../gametest/GameTestCompat. Test positions here count from the structure block, which
// sits one block below the pen, so positions in the pen are one higher than the shared tests write them.
public final class GameTestCompat {
    private GameTestCompat() {
    }

    public static RuntimeException failure(GameTestHelper helper, String message) {
        return new GameTestAssertException(message);
    }

    public static InteractionResult interact(Mob mob, Player player) {
        return interact(mob, player, InteractionHand.MAIN_HAND);
    }

    public static InteractionResult interact(Mob mob, Player player, InteractionHand hand) {
        return mob.interact(player, hand);
    }

    // A mock player, as GameTestHelper.makeMockPlayer makes, that keeps the overlay messages it's sent.
    public static Player listeningPlayer(GameTestHelper helper, GameType gameType, List<Component> messages) {
        return new Player(helper.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return gameType == GameType.SPECTATOR;
            }

            @Override
            public boolean isCreative() {
                return gameType.isCreative();
            }

            @Override
            public boolean isLocalPlayer() {
                return true;
            }

            @Override
            public void displayClientMessage(Component message, boolean overlay) {
                if (overlay) {
                    messages.add(message);
                }
            }
        };
    }

    // The whole right click: the mob first, then the held item, as NeoForge's shears need here.
    public static InteractionResult useOn(Mob mob, Player player) {
        return player.interactOn(mob, InteractionHand.MAIN_HAND);
    }

    // GameTestHelper.spawn always makes the mob persistent here, so this adds it to the level directly.
    public static <T extends Mob> T spawnDespawnable(GameTestHelper helper, EntityType<T> type, Vec3 position) {
        T mob = Objects.requireNonNull(type.create(helper.getLevel()), () -> "could not create " + type);
        Vec3 absolute = helper.absoluteVec(position.add(0.0, 1.0, 0.0));
        mob.moveTo(absolute.x, absolute.y, absolute.z, mob.getYRot(), mob.getXRot());
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    // Moves the overworld clock (the time of day), as /time set does. Tests set it back within the same tick.
    public static void setOverworldClock(GameTestHelper helper, long ticks) {
        helper.getLevel().setDayTime(ticks);
    }

    // isOwnedBy looks the owner up among the level's players here, and a test's mock player is not one of them.
    public static boolean ownedBy(TamableAnimal pet, Player player) {
        return player.getUUID().equals(pet.getOwnerUUID());
    }

    public static <T extends Entity> T spawn(GameTestHelper helper, EntityType<T> type, int x, int y, int z) {
        return helper.spawn(type, x, y + 1, z);
    }

    // A new entity that is not in the level, so it never joined and has no traits.
    public static <T extends Entity> T unspawned(GameTestHelper helper, EntityType<T> type) {
        T entity = type.create(helper.getLevel());
        if (entity == null) {
            throw failure(helper, "could not create a " + type);
        }
        return entity;
    }

    @SuppressWarnings({"deprecation", "OverrideOnly"})
    public static void finalizeSpawn(GameTestHelper helper, Mob mob) {
        mob.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWN_EGG, null);
    }

    public static Item dye(DyeColor color) {
        return DyeItem.byColor(color);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T reloaded(GameTestHelper helper, T entity) {
        CompoundTag saved = entity.saveWithoutId(new CompoundTag());
        T copy = (T) entity.getType().create(helper.getLevel());
        if (copy == null) {
            throw failure(helper, "could not create a " + entity.getType());
        }
        copy.load(saved);
        return copy;
    }
}
