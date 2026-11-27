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
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumMap;
import java.util.Map;

// Pipes from other mods, and Forge's hoppers, reach a container block entity through the item capability, before the
// container's own face rules. Forge gives every container one handler that ignores sides, so the mod's sided
// containers (the feeders: food in, nothing out; the nesting box: plain eggs out, nothing in) hand out a sided one
// instead, as NeoForge registers for them. A caller with no side is still automation, so it counts as coming from
// above. The mixin's superclass is vanilla's container, the stand-in's own.
@Mixin(BaseContainerBlockEntity.class)
public abstract class SidedContainerMixin extends net.minecraft.world.level.block.entity.BaseContainerBlockEntity {
    @Unique
    private final Map<Direction, LazyOptional<IItemHandler>> barnyardromance$handlers = new EnumMap<>(Direction.class);

    private SidedContainerMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved() && (Object) this instanceof WorldlyContainer container) {
            Direction from = side == null ? Direction.UP : side;
            return barnyardromance$handlers
                    .computeIfAbsent(from, face -> LazyOptional.of(() -> new SidedInvWrapper(container, face)))
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
