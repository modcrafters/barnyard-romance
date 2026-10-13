package com.barnyardromance;

import com.barnyardromance.platform.Services;

public final class BarnyardRomance {
    private BarnyardRomance() {
    }

    public static void init() {
        Constants.LOG.info(
                "Barnyard Romance common init on {} ({})",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName()
        );
    }
}
