package net.modcrafters.minecraft.barnyardromance.advancement;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreditLedgerTest {
    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Test
    void birthsAddUpPerPlayerAndLadder() {
        CreditLedger ledger = new CreditLedger();
        assertEquals(1, ledger.addBirths(A, "cow", 1));
        assertEquals(3, ledger.addBirths(A, "cow", 2));
        assertEquals(3, ledger.births(A, "cow"));
        assertEquals(0, ledger.births(A, "sheep"));
        assertEquals(0, ledger.births(B, "cow"));
    }

    @Test
    void heldGrantsAreTakenOnce() {
        CreditLedger ledger = new CreditLedger();
        CreditLedger.Held root = new CreditLedger.Held("barnyard_romance:barnyard/root", "done");
        CreditLedger.Held cow = new CreditLedger.Held("minecraft:husbandry/bred_all_animals", "minecraft:cow");
        ledger.hold(A, root);
        ledger.hold(A, cow);
        assertEquals(List.of(root, cow), ledger.takeHeld(A));
        assertEquals(List.of(), ledger.takeHeld(A));
        assertEquals(List.of(), ledger.takeHeld(B));
    }

    @Test
    void holdingTheSameGrantTwiceKeepsOne() {
        CreditLedger ledger = new CreditLedger();
        CreditLedger.Held root = new CreditLedger.Held("barnyard_romance:barnyard/root", "done");
        ledger.hold(A, root);
        ledger.hold(A, root);
        assertEquals(List.of(root), ledger.held(A));
    }
}
