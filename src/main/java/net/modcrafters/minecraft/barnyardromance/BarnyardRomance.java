package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.ExcludedTag;
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
