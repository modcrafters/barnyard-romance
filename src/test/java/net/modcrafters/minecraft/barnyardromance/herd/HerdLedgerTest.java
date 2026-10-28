package net.modcrafters.minecraft.barnyardromance.herd;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdLedgerTest {
    private static final UUID BOOK = id(100);
    private static final UUID OTHER_BOOK = id(101);

    private static UUID id(int n) {
        return new UUID(0L, n);
    }

    private static HerdLedger.Node node(int n) {
        return new HerdLedger.Node(id(n), null, null, true, true);
    }

    private static HerdLedger.Node child(int n, int mother, int father) {
        return new HerdLedger.Node(id(n), mother < 0 ? null : id(mother), father < 0 ? null : id(father), true, false);
    }

    @Test
    void registerAddsOnce() {
        HerdLedger ledger = new HerdLedger();

        assertTrue(ledger.register(BOOK, node(1)));
        assertFalse(ledger.register(BOOK, node(1)));
        assertEquals(Set.of(id(1)), ledger.members(BOOK));
        assertTrue(ledger.isMember(BOOK, id(1)));
    }

    @Test
    void bornJoinsEveryBookWithTheMother() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(OTHER_BOOK, node(1));

        List<UUID> books = ledger.born(id(1), List.of(child(2, 1, 9), child(3, 1, 9)));

        assertEquals(Set.of(BOOK, OTHER_BOOK), Set.copyOf(books));
        assertEquals(Set.of(id(1), id(2), id(3)), ledger.members(BOOK));
        assertEquals(Set.of(id(1), id(2), id(3)), ledger.members(OTHER_BOOK));
    }

    @Test
    void bornIgnoresTheFather() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(9));

        List<UUID> books = ledger.born(id(1), List.of(child(2, 1, 9)));

        assertTrue(books.isEmpty());
        assertEquals(Set.of(id(9)), ledger.members(BOOK));
    }

    @Test
    void bornDoesNotJoinBooksWithoutTheMother() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(OTHER_BOOK, node(5));

        ledger.born(id(1), List.of(child(2, 1, -1)));

        assertEquals(Set.of(id(5)), ledger.members(OTHER_BOOK));
    }

    @Test
    void deadMemberLeavesWhenNoLivingDescendant() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(BOOK, node(2));

        ledger.died(id(1));

        assertEquals(Set.of(id(2)), ledger.members(BOOK));
    }

    @Test
    void deadAncestorStaysForALivingGrandchild() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));
        ledger.born(id(2), List.of(child(3, 2, -1)));

        ledger.died(id(2));
        ledger.died(id(1));

        assertEquals(Set.of(id(1), id(2), id(3)), ledger.members(BOOK));
        assertFalse(ledger.node(id(1)).alive());
    }

    @Test
    void removingTheLinkDropsDeadAncestors() {
        // Old Bess (1) is dead, Daisy (2) is her calf, and Daisy's calves (3, 4) are in the book.
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));
        ledger.born(id(2), List.of(child(3, 2, -1), child(4, 2, -1)));
        ledger.died(id(1));

        assertTrue(ledger.remove(BOOK, id(2)));

        assertEquals(Set.of(id(3), id(4)), ledger.members(BOOK));
    }

    @Test
    void removingKeepsLivingParentsAndYoung() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));
        ledger.born(id(2), List.of(child(3, 2, -1)));

        ledger.remove(BOOK, id(2));

        assertEquals(Set.of(id(1), id(3)), ledger.members(BOOK));
        assertFalse(ledger.remove(BOOK, id(2)));
    }

    @Test
    void deathPrunesEveryBookOnItsOwn() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(OTHER_BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));
        ledger.remove(OTHER_BOOK, id(2));

        ledger.died(id(1));

        assertEquals(Set.of(id(1), id(2)), ledger.members(BOOK));
        assertTrue(ledger.members(OTHER_BOOK).isEmpty());
    }

    @Test
    void twoHoldersShareOneList() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(BOOK, node(2));

        // Two copies of the same book are the same ID, so a removal through one shows in the other.
        ledger.remove(BOOK, id(1));

        assertEquals(Set.of(id(2)), ledger.members(BOOK));
    }

    @Test
    void pruningSurvivesALineageLoop() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, new HerdLedger.Node(id(1), id(1), null, true, true));
        ledger.register(BOOK, new HerdLedger.Node(id(2), id(3), null, true, true));
        ledger.register(BOOK, new HerdLedger.Node(id(3), id(2), null, true, true));

        ledger.died(id(3));
        ledger.died(id(1));

        assertEquals(Set.of(id(2), id(3)), ledger.members(BOOK));
    }

    @Test
    void orphansAreNodesNoBookHolds() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.register(BOOK, node(2));
        ledger.register(OTHER_BOOK, node(2));

        ledger.remove(BOOK, id(1));
        ledger.remove(BOOK, id(2));

        assertEquals(Set.of(id(1)), ledger.orphans());
        ledger.forget(ledger.orphans());
        assertTrue(ledger.orphans().isEmpty());
        assertTrue(ledger.node(id(2)) != null);
    }

    @Test
    void everFamiliarNeverResets() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));

        assertFalse(ledger.node(id(2)).everFamiliar());
        ledger.familiar(id(2), true);
        ledger.familiar(id(2), false);

        assertTrue(ledger.node(id(2)).everFamiliar());
    }

    @Test
    void registeringAgainUpdatesParents() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(2));
        ledger.register(OTHER_BOOK, child(2, 1, 9));

        assertEquals(id(1), ledger.node(id(2)).mother());
    }

    @Test
    void loadsWhatItSaved() {
        HerdLedger ledger = new HerdLedger();
        ledger.register(BOOK, node(1));
        ledger.born(id(1), List.of(child(2, 1, -1)));

        HerdLedger loaded = HerdLedger.of(ledger.books(), ledger.nodes());

        assertEquals(ledger.books(), loaded.books());
        assertEquals(ledger.nodes(), loaded.nodes());
        assertEquals(Map.of(BOOK, Set.of(id(1), id(2))), loaded.books());
    }
}
