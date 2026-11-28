package net.modcrafters.minecraft.barnyardromance.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.drops.DropStacks;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// The 1.20.1 copy of src/.../mixin/SheepShearMixin. Shearing rolls 1-3 wool and spawns them one at a time; the
// roll is sized here instead (any wool stands in for the colour: the sized_drops tag holds #minecraft:wool).
@Mixin(Sheep.class)
public class SheepShearMixin {
    // A dispenser shears here, and counts the once-a-day shearing.
    @Inject(method = "shear(Lnet/minecraft/sounds/SoundSource;)V", at = @At("HEAD"))
    private void barnyardromance$countShearing(CallbackInfo callback) {
        ShearActions.recordShearing((Sheep) (Object) this);
    }

    // Shears in a player's hand go through Forge's IForgeShearable.onSheared instead, which rolls the same 1-3 wool and
    // hands it back for the shears to drop. Forge adds the method, so it has no obfuscated name.
    @Inject(method = "onSheared", remap = false, at = @At("HEAD"))
    private void barnyardromance$countPlayerShearing(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune,
                                                     CallbackInfoReturnable<List<ItemStack>> callback) {
        if (!level.isClientSide()) {
            ShearActions.recordShearing((Sheep) (Object) this);
        }
    }

    // The wool the shears drop, sized the same way. Only the wool is sized, as the first wool in the list; anything
    // another mod added to the list stays as it is.
    @ModifyReturnValue(method = "onSheared", remap = false, at = @At("RETURN"))
    private List<ItemStack> barnyardromance$sizedPlayerWool(List<ItemStack> drops) {
        ItemStack wool = ItemStack.EMPTY;
        int count = 0;
        List<ItemStack> others = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.is(ItemTags.WOOL)) {
                wool = wool.isEmpty() ? drop : wool;
                count += drop.getCount();
            } else {
                others.add(drop);
            }
        }
        if (count == 0) {
            return drops;
        }
        int sized = barnyardromance$sizedWool(count);
        List<ItemStack> result = new ArrayList<>(sized + others.size());
        for (int i = 0; i < sized; i++) {
            result.add(wool.copyWithCount(1));
        }
        result.addAll(others);
        return result;
    }

    // The sized wool is offered to other mods as the sheep's own colour. Left as it was, vanilla spawns it as before;
    // cancelled, the count is 0; changed, it is spawned here and vanilla spawns nothing.
    @ModifyVariable(method = "shear(Lnet/minecraft/sounds/SoundSource;)V", at = @At("STORE"), ordinal = 0)
    private int barnyardromance$sizedWool(int count) {
        Sheep sheep = (Sheep) (Object) this;
        int sized = count;
        try {
            sized = ProductDrops.resize(sheep, new ItemStack(Items.WHITE_WOOL, count), sheep.getRandom()).getCount();
        } catch (RuntimeException exception) {
            Constants.LOG.error("Failed to size a sheep's wool, leaving it to vanilla", exception);
            return count;
        }
        if (sized <= 0 || Tracked.traits(sheep).isEmpty()) {
            return sized;
        }
        ItemStack wool = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(sheep.getColor().getName() + "_wool")), sized);
        ShearYield yield = BarnyardEvents.SHEAR_YIELD.post(new ShearYield(sheep, wool.copy()));
        if (yield.isCancelled()) {
            return 0;
        }
        ItemStack result = yield.stack();
        if (ItemStack.matches(result, wool)) {
            return sized;
        }
        for (int part : DropStacks.sizes(result.getCount(), result.getMaxStackSize())) {
            sheep.spawnAtLocation(result.copyWithCount(part), 1.0F);
        }
        return 0;
    }
}
