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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.modcrafters.minecraft.barnyardromance.gametest.legacy.CraftingInput;
import net.modcrafters.minecraft.barnyardromance.gametest.legacy.RecipeHolder;
import net.modcrafters.minecraft.barnyardromance.gametest.mixin.GameTestHelperAccessor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// The 1.20.1 copy of src/.../gametest/GameTestCompat, from the 1.21.1 one. Test positions count from the structure
// block here too, so positions in the pen are one higher than the shared tests write them.
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

    public static <T extends Entity> T unspawned(GameTestHelper helper, EntityType<T> type) {
        T entity = type.create(helper.getLevel());
        if (entity == null) {
            throw failure(helper, "could not create a " + type);
        }
        return entity;
    }

    @SuppressWarnings({"deprecation", "OverrideOnly"})
    public static void finalizeSpawn(GameTestHelper helper, Mob mob) {
        mob.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWN_EGG, null, null);
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

    // GameTestHelper.makeMockPlayer takes no game mode here: it always makes a creative player.
    public static Player mockPlayer(GameTestHelper helper, GameType gameType) {
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
        };
    }

    // GameTestHelper only finds entities around a position here, so this looks in the test's structure itself.
    public static <T extends Entity> List<T> entities(GameTestHelper helper, EntityType<T> type) {
        return helper.getLevel().getEntities(type, ((GameTestHelperAccessor) helper).barnyardromance$testInfo().getStructureBounds(),
                Entity::isAlive);
    }

    // There is no scale attribute before 1.20.5: the body stays vanilla-sized.
    public static @Nullable AttributeInstance scaleAttribute(LivingEntity entity) {
        return null;
    }

    public static CompoundTag saved(GameTestHelper helper, BlockEntity blockEntity) {
        return blockEntity.saveWithFullMetadata();
    }

    public static @Nullable BlockEntity loaded(GameTestHelper helper, BlockPos pos, BlockState state, CompoundTag saved) {
        return BlockEntity.loadStatic(pos, state, saved);
    }

    // A recipe has no holder here: it carries its own id, so this puts the two in the stand-in holder.
    public static Optional<RecipeHolder<CraftingRecipe>> craftingRecipe(GameTestHelper helper, CraftingInput input) {
        return helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(recipe -> new RecipeHolder<>(recipe.getId(), recipe));
    }
}
