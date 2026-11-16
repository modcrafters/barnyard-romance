package net.modcrafters.minecraft.barnyardromance.look;

import java.util.List;
import java.util.function.Predicate;

// One place a horn file can hang its horns: a part path from the model's root, and an offset from that part in
// pixels. A file lists several, and the first part the model has wins, so one file covers vanilla's model and a
// resource pack's replacement (Fresh Animations draws its head as body/EMF_body/EMF_head2).
public record HornAttach(List<String> path, Xyz offset) {
    public HornAttach {
        path = List.copyOf(path);
    }

    // null when the model has none of the parts.
    public static HornAttach first(List<HornAttach> candidates, Predicate<List<String>> has) {
        for (HornAttach candidate : candidates) {
            if (has.test(candidate.path())) {
                return candidate;
            }
        }
        return null;
    }

    // For warnings: "body/EMF_body/EMF_head2, head".
    public static String paths(List<HornAttach> candidates) {
        return String.join(", ", candidates.stream().map(candidate -> String.join("/", candidate.path())).toList());
    }
}
