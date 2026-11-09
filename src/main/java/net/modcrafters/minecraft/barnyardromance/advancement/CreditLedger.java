package net.modcrafters.minecraft.barnyardromance.advancement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Each player's births per breeding ladder, and the advancements waiting for a player who was offline when they
// earned them. CreditData saves it with the world.
public final class CreditLedger {
    // One advancement criterion to grant: the advancement's id and the criterion's name.
    public record Held(String advancement, String criterion) {
    }

    private final Map<UUID, Map<String, Integer>> births = new LinkedHashMap<>();
    private final Map<UUID, List<Held>> held = new LinkedHashMap<>();

    public static CreditLedger of(Map<UUID, Map<String, Integer>> births, Map<UUID, List<Held>> held) {
        CreditLedger ledger = new CreditLedger();
        births.forEach((player, counts) -> ledger.births.put(player, new LinkedHashMap<>(counts)));
        held.forEach((player, grants) -> ledger.held.put(player, new ArrayList<>(grants)));
        return ledger;
    }

    public int addBirths(UUID player, String ladder, int count) {
        return births.computeIfAbsent(player, id -> new LinkedHashMap<>()).merge(ladder, count, Integer::sum);
    }

    public int births(UUID player, String ladder) {
        return births.getOrDefault(player, Map.of()).getOrDefault(ladder, 0);
    }

    public void hold(UUID player, Held grant) {
        List<Held> grants = held.computeIfAbsent(player, id -> new ArrayList<>());
        if (!grants.contains(grant)) {
            grants.add(grant);
        }
    }

    public List<Held> held(UUID player) {
        return List.copyOf(held.getOrDefault(player, List.of()));
    }

    public List<Held> takeHeld(UUID player) {
        List<Held> grants = held.remove(player);
        return grants == null ? List.of() : List.copyOf(grants);
    }

    public Map<UUID, Map<String, Integer>> births() {
        Map<UUID, Map<String, Integer>> copy = new LinkedHashMap<>();
        births.forEach((player, counts) -> copy.put(player, Map.copyOf(counts)));
        return copy;
    }

    public Map<UUID, List<Held>> held() {
        Map<UUID, List<Held>> copy = new LinkedHashMap<>();
        held.forEach((player, grants) -> copy.put(player, List.copyOf(grants)));
        return copy;
    }
}
