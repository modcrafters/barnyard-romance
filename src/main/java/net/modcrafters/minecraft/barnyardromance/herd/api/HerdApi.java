package net.modcrafters.minecraft.barnyardromance.herd.api;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

// What other features ask the herd books.
public interface HerdApi {
    // Whether any herd book lists the animal.
    boolean listed(MinecraftServer server, UUID animal);
}
