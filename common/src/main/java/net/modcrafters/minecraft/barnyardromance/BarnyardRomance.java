package net.modcrafters.minecraft.barnyardromance;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;

public final class BarnyardRomance {
    private BarnyardRomance() {
    }

    public static void init() {
        BarnyardConfig.load(Services.PLATFORM.configDirectory().resolve("barnyard_romance.json"));
        Constants.LOG.info(
                "Barnyard Romance common init on {} ({})",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName()
        );
    }
}
