package net.modcrafters.minecraft.barnyardromance.fabric.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.client.AnimalHoverTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class BarnyardRomanceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animal_tooltip"),
                AnimalHoverTooltip::render
        );
    }
}
