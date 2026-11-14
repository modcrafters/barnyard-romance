package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.nbt.NbtOps;
*///?}

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

// Sixteen nests. A fertilized egg has a nest to itself and the box counts it down to hatching (EggData);
// plain eggs pile up in a nest to their stack size. Players may put fertilized eggs back but never plain ones,
// and hoppers may only take plain eggs out. A fertilized egg that leaves the box, any way at all, goes wild
// (WildEggs); one put back starts a fresh countdown and hatches a chick with no parents.
public class NestingBoxBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 16;
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOTS).toArray();

    // The countdowns are brought up to date once a second.
    private static final int COUNT_EVERY = 20;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private @Nullable NestOccupant occupant;
    // The game time and overworld clock time the countdowns were last brought up to date, saved so they catch up
    // after an unload.
    private static final long NEVER = Long.MIN_VALUE;
    private long lastCounted = NEVER;
    private long lastClock = NEVER;
    // Eggs whose chick couldn't be made, say because its animal's mod is gone. They stay in their nests and are tried
    // again when the box loads or the egg is put back, which makes a new stack.
    private final Set<ItemStack> stuck = Collections.newSetFromMap(new IdentityHashMap<>());
    // For the window: 0 for an empty box, else the sitting hen's nest plus one.
    private final ContainerData sitting = new ContainerData() {
        @Override
        public int get(int index) {
            return occupant == null ? 0 : occupant.slot() + 1;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public NestingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(EggsBlocks.NESTING_BOX_ENTITY, pos, state);
    }

    // The nest an egg would go in, or -1 when there's no room: a pile of the same plain egg with room, else an empty
    // nest. The nest of a hen sitting in the box is hers.
    public int canPlaceEgg(ItemStack egg) {
        if (!egg.is(EggsItems.FERTILIZED_EGG)) {
            for (int slot = 0; slot < SLOTS; slot++) {
                ItemStack pile = items.get(slot);
                if (!reserved(slot) && !pile.isEmpty() && ItemStack.isSameItemSameComponents(pile, egg) && pile.getCount() < pile.getMaxStackSize()) {
                    return slot;
                }
            }
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!reserved(slot) && items.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    public boolean occupied() {
        return occupant != null;
    }

    private boolean reserved(int slot) {
        return occupant != null && occupant.slot() == slot;
    }

    ContainerData sitting() {
        return sitting;
    }

    public @Nullable NestOccupant occupant() {
        return occupant;
    }

    // A copy of the sitting hen, for drawing her in the box on the client.
    public Optional<Entity> loadSittingHen(NestOccupant hen) {
        return level == null ? Optional.empty() : NestCompat.load(level, hen.hen());
    }

    // Clients are told when a hen goes in or comes out, so they can draw her.
    private void sendToClients() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // The hen leaves the world to sit in the box, saved whole, and comes back out with all her eggs laid. She sits
    // longer for more eggs (EggRules.sitTicks). Her nest is the one her first egg goes in.
    public boolean enter(Animal hen, List<ItemStack> eggs, long now) {
        if (eggs.isEmpty() || occupant != null || !(level instanceof ServerLevel server) || !LayInNestGoal.walks(hen)) {
            return false;
        }
        int slot = canPlaceEgg(eggs.getFirst());
        if (slot < 0) {
            return false;
        }
        CompoundTag saved = NestCompat.save(hen, server);
        NestingBoxes.entered(hen.getUUID());
        hen.discard();
        occupant = new NestOccupant(saved, eggs.stream().map(egg -> egg.copyWithCount(1)).toList(), now + EggRules.sitTicks(eggs.size()), slot, hen.getUUID());
        sendToClients();
        return true;
    }

    // Her time is up: she comes out, her first egg goes in her nest and the rest wherever they fit; any that don't are
    // left in front of the box. If she can't be brought back she stays in, and tries again a quarter of an hour later.
    public void finishSitting() {
        if (occupant == null || !(level instanceof ServerLevel server)) {
            return;
        }
        NestOccupant done = occupant;
        if (!release(server, done)) {
            occupant = new NestOccupant(done.hen(), done.eggs(), server.getGameTime() + EggRules.sitTicks(1), done.slot(), done.id());
            setChanged();
            return;
        }
        occupant = null;
        Vec3 front = exitPos();
        for (int i = 0; i < done.eggs().size(); i++) {
            ItemStack egg = done.eggs().get(i);
            if (!(i == 0 && intoNest(done.slot(), egg)) && !placeEgg(egg)) {
                Containers.dropItemStack(server, front.x, front.y, front.z, WildEggs.taint(egg.copy()));
            }
        }
        sendToClients();
    }

    private boolean intoNest(int slot, ItemStack egg) {
        ItemStack pile = items.get(slot);
        if (pile.isEmpty()) {
            setItem(slot, egg.copy());
            return true;
        }
        if (ItemStack.isSameItemSameComponents(pile, egg) && pile.getCount() < pile.getMaxStackSize()) {
            pile.grow(1);
            return true;
        }
        return false;
    }

    // Brings her back into the world in front of the hole. Another entity with her id (a box copied with its hen,
    // or /clone) would stop her joining, so she takes a new one then.
    private boolean release(ServerLevel server, NestOccupant hen) {
        Optional<Entity> loaded = NestCompat.load(server, hen.hen());
        if (loaded.isEmpty()) {
            Constants.LOG.error("Couldn't bring back the {} sitting in the nesting box at {}; she stays in it", hen.hen().get("id"), getBlockPos());
            return false;
        }
        Entity entity = loaded.get();
        Vec3 at = exitPos();
        //? if >=1.21.5 {
        entity.snapTo(at.x, at.y, at.z, entity.getYRot(), 0.0F);
        //?} else
        //entity.moveTo(at.x, at.y, at.z, entity.getYRot(), 0.0F);
        if (entity instanceof Animal animal) {
            EggLaying.resume(animal, server);
        }
        if (server.getEntity(entity.getUUID()) != null) {
            Constants.LOG.warn("Something else already has the id of the hen in the nesting box at {}, so she comes out with a new one", getBlockPos());
            entity.setUUID(UUID.randomUUID());
        }
        if (!server.addFreshEntity(entity)) {
            Constants.LOG.error("Couldn't put the hen from the nesting box at {} back in the world; she stays in it", getBlockPos());
            return false;
        }
        NestingBoxes.left(hen.id());
        return true;
    }

    public boolean placeEgg(ItemStack egg) {
        int slot = canPlaceEgg(egg);
        if (slot < 0) {
            return false;
        }
        ItemStack pile = items.get(slot);
        if (pile.isEmpty()) {
            setItem(slot, egg.copyWithCount(1));
        } else {
            pile.grow(1);
            setChanged();
        }
        if (egg.getCount() > 1) {
            return placeEgg(egg.copyWithCount(egg.getCount() - 1));
        }
        return true;
    }

    // Counts the fertilized eggs down, including the time the box was unloaded, and hatches those that are done.
    public static void serverTick(Level level, BlockPos pos, BlockState state, NestingBoxBlockEntity box) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        long now = level.getGameTime();
        long clock = server.getOverworldClockTime();
        if (box.occupant != null && box.occupant.exitAt() <= now) {
            box.finishSitting();
        }
        if (box.lastCounted == NEVER) {
            box.lastCounted = now;
        }
        if (box.lastClock == NEVER) {
            box.lastClock = clock;
        }
        // Time skipped with /time add or by sleeping moves the clock but not the game time, and a stopped daylight
        // cycle moves only the game time: whichever went further counts.
        long elapsed = Math.max(now - box.lastCounted, clock - box.lastClock);
        boolean counting = elapsed >= COUNT_EVERY;
        if (counting) {
            box.lastCounted = now;
            box.lastClock = clock;
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack egg = box.items.get(slot);
            if (WildEggs.wild(egg)) {
                box.countWild(server, slot, egg, counting ? elapsed : 0L, now);
                continue;
            }
            Optional<EggData> data = FertilizedEggItem.data(egg);
            if (data.isEmpty()) {
                continue;
            }
            // An egg laid before size ranges were kept takes the species' range the first time a box sees it, the
            // way animals take theirs when they join.
            boolean stamped = data.get().sizeRange() == null;
            EggData stored = stamped ? data.get().withSizeRange(BarnyardConfig.current().sizeFor(data.get().species())) : data.get();
            EggData left = counting ? stored.counted(elapsed) : stored;
            if (left.remainingTicks() <= 0L) {
                if (box.stuck.contains(egg)) {
                    continue;
                }
                // A chick that hatched while the box was unloaded, or while the night was slept through, was born
                // when its time ran out.
                long overdue = counting ? Math.max(0L, elapsed - stored.remainingTicks()) : 0L;
                if (Hatching.hatch(server, left, box.exitPos(), now - overdue).isPresent()) {
                    box.items.set(slot, ItemStack.EMPTY);
                } else {
                    egg.set(EggsComponents.EGG_DATA, left);
                    box.stuck.add(egg);
                }
            } else if (counting || stamped) {
                egg.set(EggsComponents.EGG_DATA, left);
            }
            box.setChanged();
        }
    }

    // A wild egg starts its countdown the first time the box sees it, then counts down and hatches like the others.
    private void countWild(ServerLevel server, int slot, ItemStack egg, long elapsed, long now) {
        if (WildEggs.start(egg, server.getRandom())) {
            setChanged();
            return;
        }
        WildEgg wild = egg.get(EggsComponents.WILD_EGG);
        if (wild == null || elapsed <= 0L && wild.remainingTicks() > 0L) {
            return;
        }
        WildEgg left = wild.counted(elapsed);
        if (left.remainingTicks() <= 0L) {
            if (stuck.contains(egg)) {
                return;
            }
            long overdue = Math.max(0L, elapsed - wild.remainingTicks());
            if (Hatching.hatchWild(server, left, exitPos(), now - overdue).isPresent()) {
                items.set(slot, ItemStack.EMPTY);
            } else {
                egg.set(EggsComponents.WILD_EGG, left);
                stuck.add(egg);
            }
        } else {
            egg.set(EggsComponents.WILD_EGG, left);
        }
        setChanged();
    }

    // Whatever leaves a nest by any way at all goes wild if it's a fertilized egg: taken by hand, swapped out with a
    // hotbar key, or replaced. The stack is changed in place, wherever it went.
    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack old = getItem(slot);
        if (old != stack) {
            WildEggs.taint(old);
        }
        super.setItem(slot, stack);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return WildEggs.taint(super.removeItem(slot, count));
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return WildEggs.taint(super.removeItemNoUpdate(slot));
    }

    // In front of the hole if there's room, else on top of the box, else the nearest free spot close by.
    public Vec3 exitPos() {
        BlockPos pos = getBlockPos();
        if (level == null) {
            return Vec3.atBottomCenterOf(pos.above());
        }
        BlockPos front = pos.relative(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        if (free(front)) {
            return Vec3.atBottomCenterOf(front);
        }
        if (free(pos.above())) {
            return Vec3.atBottomCenterOf(pos.above());
        }
        return BlockPos.betweenClosedStream(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))
                .filter(this::free)
                .min(Comparator.comparingDouble(candidate -> candidate.distSqr(pos)))
                .map(Vec3::atBottomCenterOf)
                .orElse(Vec3.atBottomCenterOf(pos.above()));
    }

    private boolean free(BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public static boolean allowedByHand(ItemStack stack) {
        return stack.is(EggsItems.FERTILIZED_EGG);
    }

    // Breaking the box drops the eggs a sitting hen was about to leave along with the rest, and lets her out. Every
    // fertilized egg comes out wild.
    void beforeRemoved() {
        items.forEach(WildEggs::taint);
        if (occupant != null && level instanceof ServerLevel server) {
            NestOccupant hen = occupant;
            occupant = null;
            for (ItemStack egg : hen.eggs()) {
                Containers.dropItemStack(server, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, WildEggs.taint(egg.copy()));
            }
            release(server, hen);
            sendToClients();
        }
    }

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        beforeRemoved();
        super.preRemoveSideEffects(pos, state);
    }
    //?}

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return allowedByHand(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return !stack.is(EggsItems.FERTILIZED_EGG);
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.barnyard_romance.nesting_box");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new NestingBoxMenu(containerId, inventory, this, sitting);
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        occupant = input.read("occupant", NestOccupant.CODEC).orElse(null);
        lastCounted = input.getLongOr("lastCounted", NEVER);
        lastClock = input.getLongOr("lastClock", NEVER);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.storeNullable("occupant", NestOccupant.CODEC, occupant);
        output.putLong("lastCounted", lastCounted);
        output.putLong("lastClock", lastClock);
    }
    //?} else {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        occupant = tag.contains("occupant")
                ? NestOccupant.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("occupant")).result().orElse(null)
                : null;
        lastCounted = tag.contains("lastCounted") ? tag.getLong("lastCounted") : NEVER;
        lastClock = tag.contains("lastClock") ? tag.getLong("lastClock") : NEVER;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (occupant != null) {
            NestOccupant.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), occupant).result().ifPresent(saved -> tag.put("occupant", saved));
        }
        tag.putLong("lastCounted", lastCounted);
        tag.putLong("lastClock", lastClock);
    }
    *///?}
}
