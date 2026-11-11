package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.ServerScoped;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.config.ExcludedTag;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.minecraft.world.level.block.DispenserBlock;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

public final class BarnyardRomance {
    private BarnyardRomance() {
    }

    // Once the items are registered: dispensers throw fertilized eggs like vanilla's.
    public static void setup() {
        DispenserBlock.registerProjectileBehavior(BarnyardItems.FERTILIZED_EGG);
    }

    // What's kept in memory for a server's levels is let go when it stops, so a world left for the title screen isn't
    // held on to, and the next world opened starts clean (see ServerScoped).
    public static void serverStopped() {
        ServerScoped.clearAll();
    }

    public static void init() {
        BarnyardFeatures.ensureInstalled();
        BarnyardConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance.json"));
        ExcludedTag.install();
        Constants.LOG.info(
                "Barnyard Romance common init on {} ({})",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName()
        );
    }
}
