package net.modcrafters.minecraft.barnyardromance.core.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

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

    // A block entity type for one or more blocks. 1.21.1's vanilla keeps the factory type package-private.
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityFactory<T> factory, Block... blocks);

    interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    // A milk that can't be placed, for barnyard_romance:<path>, with the name fluid.barnyard_romance.<path>. The caller
    // registers it. NeoForge gives it a fluid type of its own, which it registers itself.
    Fluid milkFluid(String path, Supplier<Item> bucket, int colour);

    // The loader's own cow milk, if it has one. NeoForge's needs switching on in the mod's constructor.
    default Optional<Holder<Fluid>> loaderMilk() {
        return Optional.empty();
    }

    // A fluid's name as its loader gives it: NeoForge from the fluid type, Fabric from the fluid's attributes.
    Component fluidName(Fluid fluid);

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
