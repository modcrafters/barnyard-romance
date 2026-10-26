package net.modcrafters.minecraft.barnyardromance.age;

public enum AgeStage {
    BABY("Baby"),
    YOUNG("Young"),
    ADULT("Adult"),
    OLD("Old");

    private final String label;

    AgeStage(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean canBreed() {
        return this == ADULT || this == OLD;
    }
}
