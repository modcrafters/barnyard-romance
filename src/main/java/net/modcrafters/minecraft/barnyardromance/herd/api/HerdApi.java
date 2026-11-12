package net.modcrafters.minecraft.barnyardromance.herd.api;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

// What other features ask the herd books.
public interface HerdApi {
    // Whether any herd book lists the animal.
    boolean listed(MinecraftServer server, UUID animal);

    // Whether the herd books know the animal's mother or father. False for an animal no book has.
    boolean hasParentOnRecord(MinecraftServer server, UUID animal);
}
