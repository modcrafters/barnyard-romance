package net.modcrafters.minecraft.barnyardromance.api;

// Where an animal is in its life. Babies and young animals can't breed; adults and old ones can.
public enum AgeStage {
    BABY,
    YOUNG,
    ADULT,
    OLD;

    public boolean canBreed() {
        return this == ADULT || this == OLD;
    }
}
