package net.modcrafters.minecraft.barnyardromance.herd;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Which animals each herd book holds, and who their parents are. No Minecraft types, so the rules are unit tested.
// A book keeps its living members, and the dead only while a living member descends from them through members.
public final class HerdLedger {
    public record Node(UUID id, @Nullable UUID mother, @Nullable UUID father, boolean alive, boolean everFamiliar) {
        Node withParents(@Nullable UUID mother, @Nullable UUID father) {
            return new Node(id, mother, father, alive, everFamiliar);
        }

        Node dead() {
            return new Node(id, mother, father, false, everFamiliar);
        }

        Node familiar() {
            return new Node(id, mother, father, alive, true);
        }
    }

    private final Map<UUID, Set<UUID>> books = new LinkedHashMap<>();
    private final Map<UUID, Node> nodes = new LinkedHashMap<>();

    public static HerdLedger of(Map<UUID, Set<UUID>> books, Map<UUID, Node> nodes) {
        HerdLedger ledger = new HerdLedger();
        books.forEach((book, members) -> ledger.books.put(book, new LinkedHashSet<>(members)));
        ledger.nodes.putAll(nodes);
        return ledger;
    }

    public Map<UUID, Set<UUID>> books() {
        Map<UUID, Set<UUID>> copy = new LinkedHashMap<>();
        // Set.copyOf would lose the order animals joined in, and the save is written from this.
        books.forEach((book, members) -> copy.put(book, Collections.unmodifiableSet(new LinkedHashSet<>(members))));
        return Collections.unmodifiableMap(copy);
    }

    public Map<UUID, Node> nodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public boolean register(UUID book, Node animal) {
        Node known = nodes.get(animal.id());
        if (known == null) {
            nodes.put(animal.id(), animal);
        } else {
            Node updated = known.withParents(animal.mother(), animal.father());
            nodes.put(animal.id(), animal.everFamiliar() ? updated.familiar() : updated);
        }
        return books.computeIfAbsent(book, key -> new LinkedHashSet<>()).add(animal.id());
    }

    // Young join every book their mother is in at the moment of birth; the father doesn't matter.
    public List<UUID> born(UUID mother, List<Node> young) {
        List<UUID> joined = new ArrayList<>();
        books.forEach((book, members) -> {
            if (members.contains(mother)) {
                joined.add(book);
            }
        });
        if (joined.isEmpty()) {
            return joined;
        }
        for (Node child : young) {
            nodes.put(child.id(), child);
            for (UUID book : joined) {
                books.get(book).add(child.id());
            }
        }
        return joined;
    }

    public void died(UUID animal) {
        Node node = nodes.get(animal);
        if (node == null) {
            return;
        }
        nodes.put(animal, node.dead());
        books.forEach((book, members) -> {
            if (members.contains(animal)) {
                prune(members);
            }
        });
    }

    public boolean remove(UUID book, UUID animal) {
        Set<UUID> members = books.get(book);
        if (members == null || !members.remove(animal)) {
            return false;
        }
        prune(members);
        return true;
    }

    public void familiar(UUID animal, boolean familiar) {
        Node node = nodes.get(animal);
        if (familiar && node != null && !node.everFamiliar()) {
            nodes.put(animal, node.familiar());
        }
    }

    public Set<UUID> members(UUID book) {
        Set<UUID> members = books.get(book);
        return members == null ? Set.of() : Collections.unmodifiableSet(members);
    }

    public boolean isMember(UUID book, UUID animal) {
        return members(book).contains(animal);
    }

    public @Nullable Node node(UUID animal) {
        return nodes.get(animal);
    }

    public Set<UUID> orphans() {
        Set<UUID> held = new HashSet<>();
        books.values().forEach(held::addAll);
        Set<UUID> orphans = new LinkedHashSet<>(nodes.keySet());
        orphans.removeAll(held);
        return orphans;
    }

    public void forget(Set<UUID> animals) {
        Set<UUID> gone = Set.copyOf(animals);
        gone.forEach(nodes::remove);
    }

    // Walks up from every living member through parents that are members too; whatever isn't reached goes.
    private void prune(Set<UUID> members) {
        Set<UUID> kept = new HashSet<>();
        Deque<UUID> todo = new ArrayDeque<>();
        for (UUID member : members) {
            Node node = nodes.get(member);
            if (node != null && node.alive()) {
                todo.add(member);
            }
        }
        while (!todo.isEmpty()) {
            UUID current = todo.poll();
            if (!kept.add(current)) {
                continue;
            }
            Node node = nodes.get(current);
            if (node == null) {
                continue;
            }
            for (UUID parent : new UUID[] {node.mother(), node.father()}) {
                if (parent != null && members.contains(parent)) {
                    todo.add(parent);
                }
            }
        }
        members.retainAll(kept);
    }
}
