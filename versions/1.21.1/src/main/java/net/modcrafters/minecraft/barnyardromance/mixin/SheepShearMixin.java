package net.modcrafters.minecraft.barnyardromance.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.drops.DropStacks;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The 1.21.1 copy of src/.../mixin/SheepShearMixin. Shearing rolls 1-3 wool and spawns them one at a time; the
// roll is sized here instead (any wool stands in for the colour: the sized_drops tag holds #minecraft:wool).
@Mixin(Sheep.class)
public class SheepShearMixin {
    // Shears (through NeoForge's IShearable) and dispensers both end up here, so this counts the once-a-day shearing.
    @Inject(method = "shear(Lnet/minecraft/sounds/SoundSource;)V", at = @At("HEAD"))
    private void barnyardromance$countShearing(CallbackInfo callback) {
        ShearActions.recordShearing((Sheep) (Object) this);
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
        ItemStack wool = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(sheep.getColor().getName() + "_wool")), sized);
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
