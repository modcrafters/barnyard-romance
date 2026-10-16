package net.modcrafters.minecraft.barnyardromance.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    Path configDirectory();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
