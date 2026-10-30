package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.Hatching;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
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

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

// Ten nests. A fertilized egg has a nest to itself and the box counts it down to hatching (EggData);
// plain eggs pile up in a nest to their stack size. Players may put fertilized eggs back but never plain ones,
// and hoppers may only take plain eggs out.
public class NestingBoxBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 10;
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOTS).toArray();

    // A hen sits for an in-game hour before her egg is in the nest.
    public static final int SIT_TICKS = 1_000;
    // The countdowns are brought up to date once a second.
    private static final int COUNT_EVERY = 20;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private @Nullable NestOccupant occupant;
    // The game time the countdowns were last brought up to date, saved so they catch up after an unload.
    private static final long NEVER = Long.MIN_VALUE;
    private long lastCounted = NEVER;
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
        super(BarnyardBlocks.NESTING_BOX_ENTITY, pos, state);
    }

    // The nest an egg would go in, or -1 when there's no room: a pile of the same plain egg with room, else an empty
    // nest. The nest of a hen sitting in the box is hers.
    public int canPlaceEgg(ItemStack egg) {
        if (!egg.is(BarnyardItems.FERTILIZED_EGG)) {
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

    // The hen leaves the world to sit in the box, saved whole, and comes back out with her egg laid.
    public boolean enter(Animal hen, ItemStack egg, long now) {
        if (occupant != null || !(level instanceof ServerLevel server)) {
            return false;
        }
        int slot = canPlaceEgg(egg);
        if (slot < 0) {
            return false;
        }
        CompoundTag saved = NestCompat.save(hen, server);
        NestingBoxes.entered(hen.getUUID());
        hen.discard();
        occupant = new NestOccupant(saved, egg.copyWithCount(1), now + SIT_TICKS, slot, hen.getUUID());
        sendToClients();
        return true;
    }

    // Her time is up: the egg goes in her nest and she comes out.
    public void finishSitting() {
        if (occupant == null || !(level instanceof ServerLevel server)) {
            return;
        }
        NestOccupant done = occupant;
        occupant = null;
        ItemStack pile = items.get(done.slot());
        if (pile.isEmpty()) {
            setItem(done.slot(), done.egg().copy());
        } else if (ItemStack.isSameItemSameComponents(pile, done.egg()) && pile.getCount() < pile.getMaxStackSize()) {
            pile.grow(1);
        } else if (!placeEgg(done.egg())) {
            Containers.dropItemStack(server, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.0, getBlockPos().getZ() + 0.5, done.egg().copy());
        }
        release(server, done);
        sendToClients();
    }

    private void release(ServerLevel server, NestOccupant hen) {
        NestingBoxes.left(hen.id());
        Vec3 at = exitPos();
        NestCompat.load(server, hen.hen()).ifPresent(entity -> {
            //? if >=1.21.5 {
            entity.snapTo(at.x, at.y, at.z, entity.getYRot(), 0.0F);
            //?} else
            //entity.moveTo(at.x, at.y, at.z, entity.getYRot(), 0.0F);
            if (entity instanceof Animal animal) {
                EggLaying.resume(animal, server);
            }
            server.addFreshEntity(entity);
        });
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
        if (box.occupant != null && box.occupant.exitAt() <= now) {
            box.finishSitting();
        }
        if (box.lastCounted == NEVER) {
            box.lastCounted = now;
        }
        long elapsed = now - box.lastCounted;
        boolean counting = elapsed >= COUNT_EVERY;
        if (counting) {
            box.lastCounted = now;
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack egg = box.items.get(slot);
            Optional<EggData> data = FertilizedEggItem.data(egg);
            if (data.isEmpty()) {
                continue;
            }
            EggData left = counting ? data.get().counted(elapsed) : data.get();
            if (left.remainingTicks() <= 0L) {
                box.items.set(slot, ItemStack.EMPTY);
                Hatching.hatch(server, left, box.exitPos());
            } else if (counting) {
                egg.set(BarnyardComponents.EGG_DATA, left);
            }
            box.setChanged();
        }
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
        return stack.is(BarnyardItems.FERTILIZED_EGG);
    }

    // A sitting hen gets out without laying when the box is broken.
    void beforeRemoved() {
        if (occupant != null && level instanceof ServerLevel server) {
            NestOccupant hen = occupant;
            occupant = null;
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
        return !stack.is(BarnyardItems.FERTILIZED_EGG);
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
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.storeNullable("occupant", NestOccupant.CODEC, occupant);
        output.putLong("lastCounted", lastCounted);
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (occupant != null) {
            NestOccupant.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), occupant).result().ifPresent(saved -> tag.put("occupant", saved));
        }
        tag.putLong("lastCounted", lastCounted);
    }
    *///?}
}
