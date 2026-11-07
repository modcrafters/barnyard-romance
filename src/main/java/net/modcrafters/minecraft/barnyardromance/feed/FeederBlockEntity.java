package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
*///?}

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

// Nine slots of plain food for a feeding block's animals. Food goes in by hand, hopper or pipe from any side, but
// only what FeederFoods allows, and only the animals take it out (takeOne). The block remembers its herd (FeederHerd)
// and, when its pen loads after missed days, settles them for the whole herd at once (FeederCatchUp).
public class FeederBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOTS = 9;
    private static final int[] ALL_SLOTS = IntStream.range(0, SLOTS).toArray();
    private static final long NEVER = -1L;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private final FeederHerd herd = new FeederHerd();
    // The herd's days before this one are settled.
    private long settledDay = NEVER;
    // The last day the block ticked after sunrise, when its herd could walk over and eat from it.
    private long liveDay = NEVER;
    // Not saved: a catch-up cut short by an unload starts again from where each animal's own days have reached.
    private @Nullable FeederCatchUp catchUp;
    // A block just placed finds its herd on its first tick; one loaded from a save has its herd already.
    private boolean placed = true;
    // For the window: the days of food and the herd's size.
    private final ContainerData windowData = new ContainerData() {
        @Override
        public int get(int index) {
            int animals = herd.size();
            if (index == FeederMenu.ANIMALS) {
                return animals;
            }
            return food() / Math.max(1, animals);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeederBlockEntity feeder) {
        if (level instanceof ServerLevel server) {
            feeder.tick(server, server.getOverworldClockTime());
        }
    }

    // One tick at this overworld clock time (a test passes its own): the herd is found again on the block's own tick
    // of the minute, and the herd's missed days are settled once a day has turned.
    public void tick(ServerLevel level, long clock) {
        if (placed || FeederHerd.due(level.getGameTime(), refreshSlot())) {
            placed = false;
            refreshHerd(level);
        }
        long today = GameDay.of(clock);
        if (catchUp == null && settledDay < today) {
            catchUp = FeederCatchUp.start(this, level, today, liveDay);
        }
        if (catchUp != null && catchUp.step(this, level, FeederCatchUp.ANIMAL_DAYS_PER_TICK)) {
            settledDay = catchUp.today();
            catchUp = null;
            setChanged();
        }
        if (catchUp == null && liveDay < today && GameDay.afterSunrise(clock)) {
            liveDay = today;
            setChanged();
        }
    }

    public void refreshHerd(ServerLevel level) {
        HerdRefresh.refresh(this, level);
    }

    // The animals that eat here, nearest first when last found.
    public List<UUID> herd() {
        return herd.members();
    }

    public boolean herdHolds(UUID id) {
        return herd.contains(id);
    }

    void setHerd(List<UUID> ids) {
        herd.set(ids);
        index();
        setChanged();
    }

    public long settledDay() {
        return settledDay;
    }

    public int refreshSlot() {
        return FeederHerd.refreshSlot(getBlockPos().hashCode());
    }

    // One item eaten from the first slot with food in it; empty when there's nothing left.
    public ItemStack takeOne(ServerLevel level) {
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                ItemStack taken = stack.split(1);
                setChanged();
                return taken;
            }
        }
        return ItemStack.EMPTY;
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
        return new FeederMenu(containerId, inventory, this, windowData);
    }

    ContainerData windowData() {
        return windowData;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        index();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        index();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FeederIndex.remove(this);
    }

    private void index() {
        if (level instanceof ServerLevel && !isRemoved()) {
            FeederIndex.add(this);
        }
    }

    @Nullable GlobalPos globalPos() {
        return level == null ? null : GlobalPos.of(level.dimension(), getBlockPos());
    }

    // Loaded on the animal's server and ticking, so it will get to the animal.
    boolean ticking(Animal animal) {
        return !isRemoved() && level instanceof ServerLevel server && animal.level() instanceof ServerLevel other
                && server.getServer() == other.getServer() && server.shouldTickBlocksAt(getBlockPos());
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        herd.set(input.read("herd", UUIDUtil.CODEC.listOf()).orElse(List.of()));
        settledDay = input.getLongOr("settledDay", NEVER);
        liveDay = input.getLongOr("liveDay", NEVER);
        loaded();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.store("herd", UUIDUtil.CODEC.listOf(), herd.members());
        output.putLong("settledDay", settledDay);
        output.putLong("liveDay", liveDay);
    }
    //?} else {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        herd.set(tag.contains("herd") ? UUIDUtil.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("herd")).result().orElse(List.of()) : List.of());
        settledDay = tag.contains("settledDay") ? tag.getLong("settledDay") : NEVER;
        liveDay = tag.contains("liveDay") ? tag.getLong("liveDay") : NEVER;
        loaded();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        UUIDUtil.CODEC.listOf().encodeStart(NbtOps.INSTANCE, herd.members()).result().ifPresent(saved -> tag.put("herd", saved));
        tag.putLong("settledDay", settledDay);
        tag.putLong("liveDay", liveDay);
    }
    *///?}

    // Loaded from a save, or given new data in place: the herd is known, and the index is told of it if the block is
    // in a level already.
    private void loaded() {
        placed = false;
        index();
    }
}
