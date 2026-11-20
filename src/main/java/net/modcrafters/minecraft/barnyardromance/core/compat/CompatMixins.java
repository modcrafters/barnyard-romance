package net.modcrafters.minecraft.barnyardromance.core.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// Which mixins CompatMixinPlugin lists. Apart from the plugin so it has no Mixin classes to load.
final class CompatMixins {
    private CompatMixins() {
    }

    private static final String UNTITLED_DUCK_MOD = "net/untitledduckmod/common/entity/WaterfowlEntity.class";
    private static final String FRIENDS_AND_FOES = "com/faboslav/friendsandfoes/common/entity/MoobloomEntity.class";
    private static final String AUTUMNITY_TURKEY = "com/teamabnormals/autumnity/common/entity/animal/Turkey.class";
    private static final String ENVIRONMENTAL_DUCK = "com/teamabnormals/environmental/common/entity/animal/Duck.class";
    private static final String NATURALIST_DUCK = "com/crispytwig/naturalist/world/entity/animal/duck/Duck.class";

    // Mixin (a name in the mixin package) -> the class file of its mod that must be there for the mixin to be listed.
    private static final Map<String, String> PROBES = Map.ofEntries(
            Map.entry("NaturalistDuckEggTimerMixin", NATURALIST_DUCK),
            Map.entry("NaturalistOstrichHatchMixin", NATURALIST_DUCK),
            Map.entry("NaturalistThrownEggMixin", "com/crispytwig/naturalist/world/entity/projectile/throwableitemprojectile/ThrownDuckEgg.class"),
            Map.entry("UntitledDuckEggTimerMixin", UNTITLED_DUCK_MOD),
            Map.entry("UntitledDuckThrownEggMixin", UNTITLED_DUCK_MOD),
            Map.entry("MoobloomConversionMixin", FRIENDS_AND_FOES),
            Map.entry("AutumnityTurkeyEggTimerMixin", AUTUMNITY_TURKEY),
            Map.entry("AutumnityThrownEggMixin", "com/teamabnormals/autumnity/common/entity/projectile/ThrownTurkeyEgg.class"),
            Map.entry("EnvironmentalDuckEggTimerMixin", ENVIRONMENTAL_DUCK),
            Map.entry("EnvironmentalThrownEggMixin", "com/teamabnormals/environmental/common/entity/projectile/ThrownDuckEgg.class"),
            Map.entry("EnvironmentalLitterMixin", "com/teamabnormals/environmental/core/other/EnvironmentalEvents.class")
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
