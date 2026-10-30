package net.modcrafters.minecraft.barnyardromance.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public interface IPlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    Path configDirectory();

    // Lets the loader veto or swap the first baby at conception. Null means the breeding was cancelled.
    default @Nullable AgeableMob onBabySpawn(Animal parentA, Animal parentB, @Nullable AgeableMob proposed) {
        return proposed;
    }

    // A window type with no extra data to open it. Vanilla keeps MenuType's constructor private; both loaders open it.
    <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory);

    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory);
    }

    // A block entity type for one block. 1.21.1's vanilla keeps the factory type package-private.
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityFactory<T> factory, Block block);

    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
