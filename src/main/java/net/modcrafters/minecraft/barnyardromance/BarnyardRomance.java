package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.ExcludedTag;
import net.modcrafters.minecraft.barnyardromance.familiarity.Escapes;
import net.modcrafters.minecraft.barnyardromance.feed.FeederFoods;
import net.modcrafters.minecraft.barnyardromance.feed.EatAtFeederGoal;
import net.modcrafters.minecraft.barnyardromance.feed.FeederIndex;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.minecraft.world.level.block.DispenserBlock;
import net.modcrafters.minecraft.barnyardromance.platform.Services;

public final class BarnyardRomance {
    private BarnyardRomance() {
    }

    // Once the items are registered: dispensers throw fertilized eggs like vanilla's.
    public static void setup() {
        DispenserBlock.registerProjectileBehavior(BarnyardItems.FERTILIZED_EGG);
    }

    // What's kept in memory for a server's levels is let go when it stops, so a world left for the title screen isn't
    // held on to.
    public static void serverStopped() {
        FeederIndex.clear();
        FeederFoods.clear();
        Escapes.clear();
        EatAtFeederGoal.clear();
    }

    public static void init() {
        BarnyardConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance.json"));
        ExcludedTag.install();
        Constants.LOG.info(
                "Barnyard Romance common init on {} ({})",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName()
        );
    }
}
