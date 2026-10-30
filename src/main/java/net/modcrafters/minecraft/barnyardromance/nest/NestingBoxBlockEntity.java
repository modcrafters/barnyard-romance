package net.modcrafters.minecraft.barnyardromance.nest;

import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.Hatching;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
*///?}

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

// Ten nests. A fertilized egg has a nest to itself and counts down to hatching while it's in the box (EggData);
// plain eggs pile up in a nest to their stack size. Players may put fertilized eggs back but never plain ones,
// and hoppers may only take plain eggs out.
public class NestingBoxBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 10;
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOTS).toArray();

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public NestingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(BarnyardBlocks.NESTING_BOX_ENTITY, pos, state);
    }

    // The nest an egg would go in, or -1 when there's no room: a pile of the same plain egg with room, else an empty nest.
    public int canPlaceEgg(ItemStack egg) {
        if (!egg.is(BarnyardItems.FERTILIZED_EGG)) {
            for (int slot = 0; slot < SLOTS; slot++) {
                ItemStack pile = items.get(slot);
                if (!pile.isEmpty() && ItemStack.isSameItemSameComponents(pile, egg) && pile.getCount() < pile.getMaxStackSize()) {
                    return slot;
                }
            }
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            if (items.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
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

    // Fertilized eggs whose time has come hatch in front of the hole, including any that came due while unloaded.
    public static void serverTick(Level level, BlockPos pos, BlockState state, NestingBoxBlockEntity box) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        long now = level.getGameTime();
        for (int slot = 0; slot < SLOTS; slot++) {
            Optional<EggData> data = FertilizedEggItem.data(box.items.get(slot));
            if (data.isPresent() && data.get().inBox() && data.get().hatchAt() <= now) {
                box.items.set(slot, ItemStack.EMPTY);
                box.setChanged();
                Hatching.hatch(server, data.get(), box.exitPos());
            }
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

    @Override
    public void setItem(int slot, ItemStack stack) {
        countDown(stack, true);
        super.setItem(slot, stack);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return countDown(super.removeItem(slot, count), false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return countDown(super.removeItemNoUpdate(slot), false);
    }

    // A fertilized egg's countdown runs while it's in the box and stops when it leaves.
    private ItemStack countDown(ItemStack stack, boolean entering) {
        if (level == null || level.isClientSide()) {
            return stack;
        }
        long now = level.getGameTime();
        FertilizedEggItem.data(stack).ifPresent(data -> stack.set(BarnyardComponents.EGG_DATA, entering ? data.enterBox(now) : data.leaveBox(now)));
        return stack;
    }

    // Eggs leave with their countdowns stopped when the box is broken.
    void beforeRemoved() {
        for (int slot = 0; slot < SLOTS; slot++) {
            countDown(items.get(slot), false);
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
        return new NestingBoxMenu(containerId, inventory, this);
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
    //?} else {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }
    *///?}
}
