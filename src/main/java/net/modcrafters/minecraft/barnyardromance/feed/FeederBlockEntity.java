package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
*///?}

import java.util.stream.IntStream;

// Nine slots of plain food for a feeding block's animals. Food goes in by hand, hopper or pipe from any side, but
// only what FeederFoods allows, and only the animals take it out (takeOne).
public class FeederBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 9;
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOTS).toArray();

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    // For the window: the days of food and the herd's size.
    private final ContainerData herd = new ContainerData() {
        @Override
        public int get(int index) {
            int animals = herdSize();
            if (index == FeederMenu.ANIMALS) {
                return animals;
            }
            return animals == 0 ? 0 : food() / animals;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return FeederMenu.DATA;
        }
    };

    public FeederBlockEntity(BlockPos pos, BlockState state) {
        super(BarnyardBlocks.FEEDER_ENTITY, pos, state);
    }

    public FeederKind kind() {
        return getBlockState().getBlock() instanceof FeederBlock block ? block.kind() : FeederKind.MANGER;
    }

    // How many animals eat here. No block has a herd yet.
    int herdSize() {
        return 0;
    }

    // One item eaten from the first slot with food in it; false when there's nothing left.
    public boolean takeOne(ServerLevel level) {
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                setChanged();
                return true;
            }
        }
        return false;
    }

    // Every item inside, counted one by one.
    public int food() {
        int food = 0;
        for (ItemStack stack : items) {
            food += stack.getCount();
        }
        return food;
    }

    // The server decides; the client's copy takes anything and is corrected by it.
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(level instanceof ServerLevel server) || FeederFoods.accepts(server, kind(), stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
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
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FeederMenu(containerId, inventory, this, herd);
    }

    ContainerData herd() {
        return herd;
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
