package net.modcrafters.minecraft.barnyardromance.age;

import net.modcrafters.minecraft.barnyardromance.text.Text;

public enum AgeStage {
    BABY("gui.barnyard_romance.age.baby"),
    YOUNG("gui.barnyard_romance.age.young"),
    ADULT("gui.barnyard_romance.age.adult"),
    OLD("gui.barnyard_romance.age.old");

    private final String key;

    AgeStage(String key) {
        this.key = key;
    }

    public Text text() {
        return Text.of(key);
    }

    public boolean canBreed() {
        return this == ADULT || this == OLD;
    }
}
