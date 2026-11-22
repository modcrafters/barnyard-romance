package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

// A tracked sheep's fleece: white when it spawns and white for its lambs unless colouredSheep is on, dye works on it
// only with sheepDyeing on, and a water bucket washes it white whatever the settings. Untracked sheep keep vanilla's.
public final class SheepColours {
    private SheepColours() {
    }

    // The colour a new sheep or lamb gets: vanilla's choice, or white for a tracked sheep with colouredSheep off. For a
    // lamb, sheep is its parent, which is the same species.
    public static DyeColor newColour(Sheep sheep, DyeColor vanilla) {
        return !BarnyardConfig.current().colouredSheep() && tracked(sheep) ? DyeColor.WHITE : vanilla;
    }

    // Whether dye does nothing on this sheep.
    public static boolean refusesDye(Sheep sheep) {
        return !BarnyardConfig.current().sheepDyeing() && tracked(sheep);
    }

    // A water bucket held out to a coloured, tracked sheep washes it white and empties. Sheared sheep wash too, since
    // their wool grows back in their colour. Null when this isn't a washing, so the click goes on as before.
    public static InteractionResult wash(Animal animal, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(animal instanceof Sheep sheep) || !stack.is(Items.WATER_BUCKET) || sheep.getColor() == DyeColor.WHITE || !tracked(sheep)) {
            return null;
        }
        if (sheep.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        sheep.setColor(DyeColor.WHITE);
        sheep.level().playSound(null, sheep.getX(), sheep.getY(), sheep.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (sheep.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SPLASH, sheep.getX(), sheep.getY(0.6), sheep.getZ(), 30,
                    sheep.getBbWidth() / 2.0, sheep.getBbHeight() / 4.0, sheep.getBbWidth() / 2.0, 0.0);
        }
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
        return VersionCompat.SUCCESS_SERVER;
    }

    private static boolean tracked(Sheep sheep) {
        return BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(sheep));
    }
}
