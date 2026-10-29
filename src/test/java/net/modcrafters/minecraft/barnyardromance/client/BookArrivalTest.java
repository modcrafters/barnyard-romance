package net.modcrafters.minecraft.barnyardromance.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookArrivalTest {
    @Test
    void usingTheBookOpensIt() {
        assertEquals(BookArrival.OPEN, BookArrival.of(false, true));
    }

    @Test
    void usingTheBookWhileItIsOpenRefreshesIt() {
        assertEquals(BookArrival.REFRESH, BookArrival.of(true, true));
    }

    @Test
    void aReplyRefreshesTheOpenBook() {
        assertEquals(BookArrival.REFRESH, BookArrival.of(true, false));
    }

    @Test
    void aReplyAfterTheBookWasClosedIsDropped() {
        assertEquals(BookArrival.DROP, BookArrival.of(false, false));
    }
}
