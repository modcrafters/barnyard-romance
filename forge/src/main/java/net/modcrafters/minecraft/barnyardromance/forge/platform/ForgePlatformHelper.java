package net.modcrafters.minecraft.barnyardromance.forge.platform;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.IPlatformHelper;
import net.modcrafters.minecraft.barnyardromance.forge.client.ForgeMilkTextures;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluid;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgePlatformHelper implements IPlatformHelper {
    // Each milk's fluid type, added as its fluid is made. Fluids register before fluid types do (Forge's own registries
    // come after the vanilla ones), so every type is in by the time this registers; one added later would throw rather
    // than go missing.
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Constants.MOD_ID);

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public @Nullable AgeableMob onBabySpawn(Animal parentA, Animal parentB, @Nullable AgeableMob proposed) {
        BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(parentA, parentB, proposed);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return null;
        }
        return event.getChild();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::create, blocks).build(null);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }

    @Override
    public Fluid milkFluid(String path, Supplier<Item> bucket, int colour) {
        // Thick and heavy like Forge's own milk, with the bucket sounds its milk delegates to. Milk can't be placed, so the
        // textures are for other mods' tanks, which draw a fluid's still and flowing textures.
        FluidType type = new FluidType(FluidType.Properties.create()
                .descriptionId("fluid." + Constants.MOD_ID + "." + path)
                .density(1024)
                .viscosity(1024)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(ForgeMilkTextures.tinted(0xFF000000 | colour));
            }
        };
        FLUID_TYPES.register(path, () -> type);
        return new MilkFluid(bucket, colour) {
            @Override
            public FluidType getFluidType() {
                return type;
            }
        };
    }

    // Forge's minecraft:milk, which the mod switches on (see BarnyardRomanceForge).
    @Override
    public Optional<Holder<Fluid>> loaderMilk() {
        return Optional.of(new LaterHolder(ForgeMod.MILK));
    }

    @Override
    public Component fluidName(Fluid fluid) {
        return fluid.getFluidType().getDescription();
    }

    // A holder for a fluid Forge makes only once fluids register. MilkFluids reads the loader's milk while the first
    // registry fills, which comes before fluids, and NeoForge's DeferredHolder is already a holder then; Forge's
    // RegistryObject isn't. This one knows its key straight away and asks the fluid for the rest once there is one.
    private record LaterHolder(RegistryObject<Fluid> object) implements Holder<Fluid> {
        private ResourceKey<Fluid> key() {
            return ResourceKey.create(Registries.FLUID, object.getId());
        }

        private Holder<Fluid> bound() {
            return object.getHolder().orElseThrow(() -> new IllegalStateException(object.getId() + " isn't registered yet"));
        }

        @Override
        public Fluid value() {
            return object.get();
        }

        @Override
        public boolean isBound() {
            return object.isPresent();
        }

        @Override
        public boolean is(ResourceLocation id) {
            return object.getId().equals(id);
        }

        @Override
        public boolean is(ResourceKey<Fluid> key) {
            return key().equals(key);
        }

        @Override
        public boolean is(Predicate<ResourceKey<Fluid>> predicate) {
            return predicate.test(key());
        }

        @Override
        public boolean is(TagKey<Fluid> tag) {
            return bound().is(tag);
        }

        @Override
        public Stream<TagKey<Fluid>> tags() {
            return bound().tags();
        }

        @Override
        public Either<ResourceKey<Fluid>, Fluid> unwrap() {
            return Either.left(key());
        }

        @Override
        public Optional<ResourceKey<Fluid>> unwrapKey() {
            return Optional.of(key());
        }

        @Override
        public Kind kind() {
            return Kind.REFERENCE;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Fluid> owner) {
            return bound().canSerializeIn(owner);
        }
    }
}
