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
