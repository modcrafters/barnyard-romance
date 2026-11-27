package net.modcrafters.minecraft.barnyardromance.forge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.modcrafters.minecraft.barnyardromance.compat.legacy.BaseContainerBlockEntity;
import net.modcrafters.minecraft.barnyardromance.feed.FeederBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumMap;
import java.util.Map;

// Pipes from other mods, and Forge's hoppers, reach the feeding blocks through the item capability, which follows the
// block's own rules for each side: food in, nothing out. Forge gives every container block entity one handler that
// ignores sides, so the feeder hands out a sided one instead, as it registers on NeoForge. A caller with no side is
// still automation, so it counts as coming from above.
@Mixin(FeederBlockEntity.class)
public abstract class FeederCapabilityMixin extends BaseContainerBlockEntity {
    @Unique
    private final Map<Direction, LazyOptional<IItemHandler>> barnyardromance$handlers = new EnumMap<>(Direction.class);

    private FeederCapabilityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved()) {
            Direction from = side == null ? Direction.UP : side;
            return barnyardromance$handlers
                    .computeIfAbsent(from, face -> LazyOptional.of(() -> new SidedInvWrapper((WorldlyContainer) this, face)))
                    .cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        barnyardromance$handlers.values().forEach(LazyOptional::invalidate);
        barnyardromance$handlers.clear();
    }
}
