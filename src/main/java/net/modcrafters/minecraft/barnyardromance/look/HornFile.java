package net.modcrafters.minecraft.barnyardromance.look;

import java.util.List;

// One assets/<ns>/barnyard_horns/*.json file. Mirrored parts are already expanded, so male and female are plain parts.
public record HornFile(
        List<String> entities,
        List<String> attach,
        Xyz offset,
        String texture,
        int textureWidth,
        int textureHeight,
        double femaleChance,
        List<HideRule> hide,
        List<HornPart> male,
        List<HornPart> female
) {
    public HornFile {
        entities = List.copyOf(entities);
        attach = List.copyOf(attach);
        hide = List.copyOf(hide);
        male = List.copyOf(male);
        female = List.copyOf(female);
    }
}
