package net.modcrafters.minecraft.barnyardromance.look;

import java.util.List;

// A vanilla horn to hide: the whole part at path when size is null, else that part's cubes of this size (and from, if set).
public record HideRule(List<String> path, Xyz size, Xyz from) {
    public HideRule {
        path = List.copyOf(path);
    }
}
