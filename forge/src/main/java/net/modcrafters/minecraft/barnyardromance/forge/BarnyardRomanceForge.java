package net.modcrafters.minecraft.barnyardromance.forge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.api.forge.ForgeBridge;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.core.AnimalTicks;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.food.FoodReloadListeners;
import net.modcrafters.minecraft.barnyardromance.forge.client.BarnyardRomanceForgeClient;
import net.modcrafters.minecraft.barnyardromance.forge.data.AnimalDataSync;
import net.modcrafters.minecraft.barnyardromance.forge.data.ForgeAnimalData;
import net.modcrafters.minecraft.barnyardromance.forge.pail.PailFluidHandler;
import net.modcrafters.minecraft.barnyardromance.forge.platform.ForgeNetworkService;
import net.modcrafters.minecraft.barnyardromance.forge.platform.ForgePlatformHelper;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class BarnyardRomanceForge {
    private static final ResourceLocation PAIL_FLUID = new ResourceLocation(Constants.MOD_ID, "pail_fluid");

    // Forge 47.1 hands a mod no event bus in its constructor; the loading context has it.
    public BarnyardRomanceForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Installs the features first: the animal data keys and registry content come from them.
        BarnyardRomance.init();
        // The public API's events become Forge events here.
        ForgeBridge.register();
        ForgeAnimalData.register(modBus);
        SpeciesTrackedCondition.register();
        ConfigEnabledCondition.register();
        // Cow milk in pails is Forge's own milk fluid, which is off unless a mod asks for it.
        ForgeMod.enableMilkFluid();
        ForgePlatformHelper.FLUID_TYPES.register(modBus);
        registerPayloads();
        modBus.addListener(BarnyardRomanceForge::registerContent);
        modBus.addListener(BarnyardRomanceForge::addToCreativeTabs);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(BarnyardRomance::setup));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BarnyardRomanceForgeClient.register(modBus);
        }
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, BarnyardRomanceForge::attachItemCapabilities);
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceForge::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceForge::onEntityLeave);
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceForge::onServerTick);
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BarnyardRomance.serverStopped());
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                BarnyardAdvancements.loggedIn(player);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(BarnyardRomanceForge::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> FoodData.tagsReloaded());
    }

    // Each RegisterEvent keeps the entries for its own registry, as on NeoForge. The content is made while its registry
    // is open (MilkFluids makes its fluids as it loads), so it can't be handed to a DeferredRegister up front.
    private static void registerContent(RegisterEvent event) {
        BarnyardRegistrations.registerAll(new BarnyardRegistrations.Sink() {
            @Override
            public <T> void register(ResourceKey<? extends Registry<T>> registry, ResourceLocation id, Supplier<? extends T> value) {
                event.register(registry, id, value::get);
            }
        }, () -> BarnyardTab.titled(CreativeModeTab.builder())
                .displayItems((parameters, output) -> BarnyardTab.contents().forEach(output::accept))
                .build());
    }

    // The payloads, in the order that gives each its discriminator. Both sides register them the same way.
    private static void registerPayloads() {
        ForgeNetworkService.toClient(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.class, HerdBookPayloads.ShowBook.STREAM_CODEC);
        ForgeNetworkService.toServer(HerdBookPayloads.RemoveFromBook.class, HerdBookPayloads.RemoveFromBook.STREAM_CODEC,
                (payload, player) -> HerdBooks.removeRequested(player, payload));
        AnimalDataSync.register();
    }

    // Tanks and pipes from other mods fill and drain pails. The goat and sheep milk buckets get theirs from
    // MilkBucketItemMixin; Forge's own bucket handler covers vanilla's milk bucket, as the mod switches its milk on, and
    // filling an empty bucket.
    private static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof PailItem) {
            event.addCapability(PAIL_FLUID, new PailFluidHandler(event.getObject()));
        }
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BarnyardTab.FOOD_AND_DRINKS) {
            BarnyardTab.foodAndDrinks().forEach(event::accept);
        }
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        AnimalTicks.joined(animal);
    }

    private static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        HerdBooks.leftLevel(animal, animal.getRemovalReason());
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HerdBooks.tick(event.getServer(), event.getServer().getTickCount());
        }
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FoodReloadListeners.diets());
        event.addListener(FoodReloadListeners.foods());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        BarnyardCommands.register(event.getDispatcher());
    }
}
