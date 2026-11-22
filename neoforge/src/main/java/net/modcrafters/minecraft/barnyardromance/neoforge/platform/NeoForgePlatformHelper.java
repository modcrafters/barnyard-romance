package net.modcrafters.minecraft.barnyardromance.neoforge.platform;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.IPlatformHelper;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluid;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {
    // Each milk's fluid type, added as its fluid is made. Fluids register before fluid types do, so every type is in by
    // the time this registers; one added later would throw rather than go missing.
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Constants.MOD_ID);

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

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
        //? if >=1.21.2 {
        return new BlockEntityType<>(factory::create, blocks);
        //?} else
        //return BlockEntityType.Builder.of(factory::create, blocks).build(null);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }

    @Override
    public Fluid milkFluid(String path, Supplier<Item> bucket, int colour) {
        // Thick and heavy like NeoForge's own milk, with the bucket sounds its milk delegates to.
        FluidType type = new FluidType(FluidType.Properties.create()
                .descriptionId("fluid." + Constants.MOD_ID + "." + path)
                .density(1024)
                .viscosity(1024)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY));
        FLUID_TYPES.register(path, () -> type);
        return new MilkFluid(bucket, colour) {
            @Override
            public FluidType getFluidType() {
                return type;
            }
        };
    }

    @Override
    public Optional<Holder<Fluid>> loaderMilk() {
        return Optional.of(NeoForgeMod.MILK);
    }

    @Override
    public Component fluidName(Fluid fluid) {
        return fluid.getFluidType().getDescription();
    }
}
