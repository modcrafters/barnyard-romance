package net.modcrafters.minecraft.barnyardromance.fabric.platform;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Set;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.modcrafters.minecraft.barnyardromance.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityFactory<T> factory, Block block) {
        return new BlockEntityType<>(factory::create, Set.of(block));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }
}
