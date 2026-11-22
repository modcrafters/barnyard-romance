package net.modcrafters.minecraft.barnyardromance.pail;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PailFluidsChoiceTest {
    @Test
    void thePreferredFluidComesFirstWhenSeveralShareABucket() {
        assertEquals(Optional.of("b"), PailFluids.choose(List.of("a", "b"), "b"::equals));
    }

    @Test
    void withoutAPreferenceTheFirstMatchWins() {
        assertEquals(Optional.of("a"), PailFluids.choose(List.of("a", "b"), fluid -> false));
        assertEquals(Optional.empty(), PailFluids.choose(List.<String>of(), fluid -> true));
    }
}
