package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.server.MinecraftServer;
import net.modcrafters.minecraft.barnyardromance.herd.api.HerdApi;

import java.util.UUID;

final class HerdApiImpl implements HerdApi {
    @Override
    public boolean listed(MinecraftServer server, UUID animal) {
        return HerdBooks.listed(server, animal);
    }

    @Override
    public boolean hasParentOnRecord(MinecraftServer server, UUID animal) {
        HerdLedger.Node node = HerdBookData.get(server).ledger().node(animal);
        return node != null && (node.mother() != null || node.father() != null);
    }
}
