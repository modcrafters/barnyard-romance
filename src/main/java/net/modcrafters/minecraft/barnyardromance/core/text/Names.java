package net.modcrafters.minecraft.barnyardromance.core.text;

import net.modcrafters.minecraft.barnyardromance.api.AgeStage;
import net.modcrafters.minecraft.barnyardromance.api.Sex;

// The shown names of the public API's values; the API itself carries no text, so its language keys stay here.
public final class Names {
    private Names() {
    }

    public static Text of(Sex sex) {
        return sex == Sex.FEMALE ? Text.of("gui.barnyard_romance.sex.female") : Text.of("gui.barnyard_romance.sex.male");
    }

    public static Text of(AgeStage stage) {
        return Text.of(switch (stage) {
            case BABY -> "gui.barnyard_romance.age.baby";
            case YOUNG -> "gui.barnyard_romance.age.young";
            case ADULT -> "gui.barnyard_romance.age.adult";
            case OLD -> "gui.barnyard_romance.age.old";
        });
    }
}
