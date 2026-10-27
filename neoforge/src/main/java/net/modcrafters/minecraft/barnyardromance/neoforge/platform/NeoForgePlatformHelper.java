package net.modcrafters.minecraft.barnyardromance.neoforge.platform;

import net.modcrafters.minecraft.barnyardromance.platform.services.IPlatformHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        //? if >=1.21.9 {
        return !FMLLoader.getCurrent().isProduction();
        //?} else
        //return !FMLLoader.isProduction();
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public @Nullable AgeableMob onBabySpawn(Animal parentA, Animal parentB, @Nullable AgeableMob proposed) {
        BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(parentA, parentB, proposed);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return null;
        }
        return event.getChild();
    }
}
