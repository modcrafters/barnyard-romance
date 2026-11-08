package net.modcrafters.minecraft.barnyardromance.feed;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeederKindTest {
    @Test
    void idsAndTags() {
        assertEquals(List.of("bird_feeder", "trough", "feeder", "manger", "pet_bowl"),
                Arrays.stream(FeederKind.values()).map(FeederKind::id).toList());
        for (FeederKind kind : FeederKind.values()) {
            assertEquals("feeds_from/" + kind.id(), kind.tagPath());
        }
    }
}
