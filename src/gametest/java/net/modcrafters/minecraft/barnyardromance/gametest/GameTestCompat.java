package net.modcrafters.minecraft.barnyardromance.gametest;

import com.mojang.authlib.GameProfile;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return interact(mob, player, InteractionHand.MAIN_HAND);
    }

    // A right click on the mob with this hand.
    public static InteractionResult interact(Mob mob, Player player, InteractionHand hand) {
        return mob.interact(player, hand, mob.position());
    }

    // A mock player, as GameTestHelper.makeMockPlayer makes, that keeps the overlay messages it's sent.
    public static Player listeningPlayer(GameTestHelper helper, GameType gameType, List<Component> messages) {
        return new Player(helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public @Nullable GameType gameMode() {
                return gameType;
            }

            @Override
            public boolean isClientAuthoritative() {
                return false;
            }

            @Override
            public void sendOverlayMessage(Component message) {
                messages.add(message);
            }
        };
    }

    // The whole right click: the mob first, then the held item, as NeoForge's shears need on 1.21.1.
    public static InteractionResult useOn(Mob mob, Player player) {
        return player.interactOn(mob, InteractionHand.MAIN_HAND, mob.position());
    }

    // Spawned at a position relative to the test, like a natural mob that may despawn.
    public static <T extends Mob> T spawnDespawnable(GameTestHelper helper, EntityType<T> type, Vec3 position) {
        return helper.spawnEntity(type, position).requirePersistence(false).spawn();
    }

    // Moves the overworld clock (the time of day), as /time set does. Tests set it back within the same tick.
    public static void setOverworldClock(GameTestHelper helper, long ticks) {
        ServerLevel level = helper.getLevel();
        level.clockManager().setTotalTicks(level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow(), ticks);
    }

    // The pet's owner is this player.
    public static boolean ownedBy(TamableAnimal pet, Player player) {
        return pet.isOwnedBy(player);
    }

    // Spawned at a block position relative to the test, standing on the pen's floor at y 1.
    public static <T extends Entity> T spawn(GameTestHelper helper, EntityType<T> type, int x, int y, int z) {
        return helper.spawn(type, x, y, z);
    }

    // A new entity that is not in the level, so it never joined and has no traits.
    public static <T extends Entity> T unspawned(GameTestHelper helper, EntityType<T> type) {
        T entity = type.create(helper.getLevel(), EntitySpawnReason.LOAD);
        if (entity == null) {
            throw failure(helper, "could not create a " + type);
        }
        return entity;
    }

    // Finishes the mob as a spawn egg would, with the choices vanilla makes for a new mob, such as a sheep's colour.
    public static void finalizeSpawn(GameTestHelper helper, Mob mob) {
        mob.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
    }

    // The dye of this colour.
    public static Item dye(DyeColor color) {
        return Items.DYE.asList().stream()
                .filter(item -> new ItemStack(item).get(DataComponents.DYE) == color)
                .findFirst()
                .orElseThrow();
    }

    // A fresh copy of the entity made from its save, the way a chunk loads it. The copy is not in the level.
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T reloaded(GameTestHelper helper, T entity) {
        ServerLevel level = helper.getLevel();
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Constants.LOG)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            entity.saveWithoutId(output);
            T copy = (T) entity.getType().create(level, EntitySpawnReason.LOAD);
            if (copy == null) {
                throw failure(helper, "could not create a " + entity.getType());
            }
            copy.load(TagValueInput.create(reporter, level.registryAccess(), output.buildResult()));
            return copy;
        }
    }

    // A mock player in this game mode, not in the level.
    public static Player mockPlayer(GameTestHelper helper, GameType gameType) {
        return helper.makeMockPlayer(gameType);
    }

    // The live entities of this type in the test's structure.
    public static <T extends Entity> List<T> entities(GameTestHelper helper, EntityType<T> type) {
        return helper.getEntities(type);
    }

    // The body's scale, or null where there is none (1.20.1 has no scale attribute).
    public static @Nullable AttributeInstance scaleAttribute(LivingEntity entity) {
        return entity.getAttribute(Attributes.SCALE);
    }

    // The block entity's save, with its id and position, as a chunk writes it.
    public static CompoundTag saved(GameTestHelper helper, BlockEntity blockEntity) {
        return blockEntity.saveWithFullMetadata(helper.getLevel().registryAccess());
    }

    // A new block entity made from such a save. It is not in the level.
    public static @Nullable BlockEntity loaded(GameTestHelper helper, BlockPos pos, BlockState state, CompoundTag saved) {
        return BlockEntity.loadStatic(pos, state, saved, helper.getLevel().registryAccess());
    }

    // The crafting recipe the server finds for this grid.
    public static Optional<RecipeHolder<CraftingRecipe>> craftingRecipe(GameTestHelper helper, CraftingInput input) {
        return helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
    }
}
