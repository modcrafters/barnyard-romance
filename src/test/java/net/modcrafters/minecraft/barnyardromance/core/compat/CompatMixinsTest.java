package net.modcrafters.minecraft.barnyardromance.core.compat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatMixinsTest {
    private static final String DUCK = "com/crispytwig/naturalist/world/entity/animal/duck/Duck.class";

    @Test
    void listsNothingWhenNoModIsThere() {
        assertEquals(List.of(), CompatMixins.mixinsFor(probe -> false));
    }

    @Test
    void listsAModsMixinsOnlyWhenItsProbeClassIsThere() {
        List<String> mixins = CompatMixins.mixinsFor(DUCK::equals);
        assertEquals(Set.of("NaturalistDuckEggTimerMixin", "NaturalistOstrichHatchMixin"), Set.copyOf(mixins));
        assertEquals(mixins.size(), Set.copyOf(mixins).size());
    }

    @Test
    void untitledDuckModMixinsComeWithItsWaterfowlClass() {
        List<String> mixins = CompatMixins.mixinsFor("net/untitledduckmod/common/entity/WaterfowlEntity.class"::equals);
        assertEquals(Set.of("UntitledDuckEggTimerMixin", "UntitledDuckThrownEggMixin"), Set.copyOf(mixins));
    }

    @Test
    void autumnityAndEnvironmentalMixinsComeWithTheirAnimalClasses() {
        assertEquals(Set.of("AutumnityTurkeyEggTimerMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/teamabnormals/autumnity/common/entity/animal/Turkey.class"::equals)));
        assertEquals(Set.of("AutumnityThrownEggMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/teamabnormals/autumnity/common/entity/projectile/ThrownTurkeyEgg.class"::equals)));
        assertEquals(Set.of("EnvironmentalDuckEggTimerMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/teamabnormals/environmental/common/entity/animal/Duck.class"::equals)));
        assertEquals(Set.of("EnvironmentalThrownEggMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/teamabnormals/environmental/common/entity/projectile/ThrownDuckEgg.class"::equals)));
        assertEquals(Set.of("EnvironmentalLitterMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/teamabnormals/environmental/core/other/EnvironmentalEvents.class"::equals)));
        assertEquals(Set.of("NaturalistThrownEggMixin"),
                Set.copyOf(CompatMixins.mixinsFor("com/crispytwig/naturalist/world/entity/projectile/throwableitemprojectile/ThrownDuckEgg.class"::equals)));
    }

    @Test
    void theMoobloomMixinComesWithItsEntityClass() {
        List<String> mixins = CompatMixins.mixinsFor("com/faboslav/friendsandfoes/common/entity/MoobloomEntity.class"::equals);
        assertEquals(Set.of("MoobloomConversionMixin"), Set.copyOf(mixins));
    }

    @Test
    void theTestClassLoaderHasNoModClassesSoNothingIsListed() {
        ClassLoader loader = CompatMixinsTest.class.getClassLoader();
        assertTrue(CompatMixins.mixinsFor(probe -> loader.getResource(probe) != null).isEmpty());
    }
}
