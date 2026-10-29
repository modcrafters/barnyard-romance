package net.modcrafters.minecraft.barnyardromance.client;

// What the client does with a herd book from the server. Only using the book opens it; a reply to a removal
// refreshes the book if it is still open, and is dropped once the player has closed it.
public enum BookArrival {
    OPEN,
    REFRESH,
    DROP;

    public static BookArrival of(boolean bookOpen, boolean openRequested) {
        if (bookOpen) {
            return REFRESH;
        }
        return openRequested ? OPEN : DROP;
    }
}
