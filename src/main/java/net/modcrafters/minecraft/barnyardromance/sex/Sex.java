package net.modcrafters.minecraft.barnyardromance.sex;

import net.modcrafters.minecraft.barnyardromance.text.Text;

public enum Sex {
    MALE,
    FEMALE;

    public Text text() {
        return this == FEMALE ? Text.of("gui.barnyard_romance.sex.female") : Text.of("gui.barnyard_romance.sex.male");
    }
}
