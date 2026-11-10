package net.modcrafters.minecraft.barnyardromance.neoforge;

import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.modcrafters.minecraft.barnyardromance.food.FoodData;
import net.modcrafters.minecraft.barnyardromance.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.advancement.BarnyardAdvancements;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.food.FoodReloadListeners;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
//? if <1.21.6
//import net.modcrafters.minecraft.barnyardromance.client.HerdBookClient;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.nest.BarnyardBlocks;
import net.modcrafters.minecraft.barnyardromance.neoforge.client.BarnyardRomanceNeoForgeClient;
import net.modcrafters.minecraft.barnyardromance.neoforge.data.NeoForgeAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.resources.Identifier;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.2 {
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
//?} else
//import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
//?} else
//import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

@Mod(Constants.MOD_ID)
public class BarnyardRomanceNeoForge {
    public BarnyardRomanceNeoForge(IEventBus modBus) {
        NeoForgeAnimalData.ATTACHMENT_TYPES.register(modBus);
        modBus.addListener(BarnyardRomanceNeoForge::registerContent);
        modBus.addListener(BarnyardRomanceNeoForge::addToCreativeTabs);
        modBus.addListener(BarnyardRomanceNeoForge::registerPayloads);
        modBus.addListener(BarnyardRomanceNeoForge::registerCapabilities);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(BarnyardRomance::setup));
        //? if >=1.21.9 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
        //?} else
        //if (FMLEnvironment.dist == Dist.CLIENT) {
            BarnyardRomanceNeoForgeClient.register(modBus);
        }
        BarnyardRomance.init();
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onServerTick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BarnyardRomance.serverStopped());
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                BarnyardAdvancements.loggedIn(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> FoodData.tagsReloaded());
    }

    // Each RegisterEvent keeps the entries for its own registry.
    private static void registerContent(RegisterEvent event) {
        BarnyardRegistrations.registerAll(new BarnyardRegistrations.Sink() {
            @Override
            public <T> void register(ResourceKey<? extends Registry<T>> registry, Identifier id, Supplier<? extends T> value) {
                event.register(registry, id, value::get);
            }
        }, () -> BarnyardTab.titled(CreativeModeTab.builder())
                .displayItems((parameters, output) -> BarnyardTab.contents().forEach(output::accept))
                .build());
    }

    // Pipes from other mods reach the feeding blocks through the item capability, which follows the block's own rules
    // for each side: food in, nothing out. The wrappers let a caller with no side take items out, but that's still
    // automation, so it counts as coming from above.
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BarnyardBlocks.FEEDER_ENTITY,
                (feeder, side) -> new WorldlyContainerWrapper(feeder, side == null ? Direction.UP : side));
        //?} else {
        /*event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BarnyardBlocks.FEEDER_ENTITY,
                (feeder, side) -> new SidedInvWrapper(feeder, side == null ? Direction.UP : side));
        *///?}
    }

    // From 1.21.6 on, the client registers its own handler for ShowBook (see BarnyardRomanceNeoForgeClient).
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        //? if >=1.21.6 {
        registrar.playToClient(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC);
        //?} else
        //registrar.playToClient(HerdBookPayloads.ShowBook.TYPE, HerdBookPayloads.ShowBook.STREAM_CODEC, (payload, context) -> HerdBookClient.show(payload));
        registrar.playToServer(HerdBookPayloads.RemoveFromBook.TYPE, HerdBookPayloads.RemoveFromBook.STREAM_CODEC,
                (payload, context) -> HerdBooks.removeRequested(context.player(), payload));
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BarnyardItems.FOOD_AND_DRINKS) {
            BarnyardTab.foodAndDrinks().forEach(event::accept);
        }
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        AnimalSex.onJoin(animal);
    }

    private static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        HerdBooks.leftLevel(animal, animal.getRemovalReason());
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        HerdBooks.tick(event.getServer(), event.getServer().getTickCount());
    }

    //? if >=1.21.2 {
    private static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(FoodReloadListeners.DIETS_ID, FoodReloadListeners.diets());
        event.addListener(FoodReloadListeners.FOODS_ID, FoodReloadListeners.foods());
    }
    //?} else {
    /*private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FoodReloadListeners.diets());
        event.addListener(FoodReloadListeners.foods());
    }
    *///?}

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        BarnyardCommands.register(event.getDispatcher());
    }
}
