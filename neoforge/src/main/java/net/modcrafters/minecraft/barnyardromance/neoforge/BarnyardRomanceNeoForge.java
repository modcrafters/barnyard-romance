package net.modcrafters.minecraft.barnyardromance.neoforge;

import net.modcrafters.minecraft.barnyardromance.BarnyardRomance;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.command.BarnyardCommands;
import net.modcrafters.minecraft.barnyardromance.neoforge.client.BarnyardRomanceNeoForgeClient;
import net.modcrafters.minecraft.barnyardromance.neoforge.data.NeoForgeAnimalData;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@Mod(Constants.MOD_ID)
public class BarnyardRomanceNeoForge {
    public BarnyardRomanceNeoForge(IEventBus modBus) {
        NeoForgeAnimalData.ATTACHMENT_TYPES.register(modBus);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            BarnyardRomanceNeoForgeClient.register(modBus);
        }
        BarnyardRomance.init();
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(BarnyardRomanceNeoForge::onRegisterCommands);
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }
        AnimalSex.onJoin(animal);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        BarnyardCommands.register(event.getDispatcher());
    }
}
