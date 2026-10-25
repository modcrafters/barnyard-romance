package net.modcrafters.minecraft.barnyardromance.platform;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.data.EntityDataService;
import net.modcrafters.minecraft.barnyardromance.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final EntityDataService DATA = load(EntityDataService.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
