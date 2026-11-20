package net.modcrafters.minecraft.barnyardromance.core.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// Which mixins CompatMixinPlugin lists. Apart from the plugin so it has no Mixin classes to load.
final class CompatMixins {
    private CompatMixins() {
    }

    // Mixin (a name in the mixin package) -> the class file of its mod that must be there for the mixin to be listed.
    private static final Map<String, String> PROBES = Map.of(
            "NaturalistDuckEggTimerMixin", "com/crispytwig/naturalist/world/entity/animal/duck/Duck.class",
            "NaturalistOstrichHatchMixin", "com/crispytwig/naturalist/world/entity/animal/duck/Duck.class"
    );

    // The mixins whose probe class file is present.
    static List<String> mixinsFor(Predicate<String> present) {
        List<String> mixins = new ArrayList<>();
        PROBES.forEach((mixin, probe) -> {
            if (present.test(probe)) {
                mixins.add(mixin);
            }
        });
        return mixins;
    }
}
