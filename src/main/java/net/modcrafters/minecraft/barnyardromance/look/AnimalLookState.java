package net.modcrafters.minecraft.barnyardromance.look;

// Added to the living-entity render state on versions that draw from one, so the later model pass can read it.
public interface AnimalLookState {
    float barnyardromance$belly();

    void barnyardromance$setBelly(float belly);

    HornDraw barnyardromance$horns();

    void barnyardromance$setHorns(HornDraw horns);
}
