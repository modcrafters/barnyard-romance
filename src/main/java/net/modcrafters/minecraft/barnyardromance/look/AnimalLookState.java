package net.modcrafters.minecraft.barnyardromance.look;

// Added to the living-entity render state on versions that draw from one, so the later model pass can read it.
public interface AnimalLookState {
    float barnyardromance$belly();

    void barnyardromance$setBelly(float belly);

    HornDraw barnyardromance$horns();

    void barnyardromance$setHorns(HornDraw horns);

    // The renderer's own model, the only one of the state's models that gets horns (a sheep's wool is another model
    // with the same parts). Object, so this interface doesn't depend on a version's Model class.
    Object barnyardromance$hornModel();

    void barnyardromance$setHornModel(Object model);

    // Set on a herd book portrait's state: where the model pass reports the head it drew (HerdBookHeads.Measure).
    Object barnyardromance$portrait();

    void barnyardromance$setPortrait(Object portrait);
}
