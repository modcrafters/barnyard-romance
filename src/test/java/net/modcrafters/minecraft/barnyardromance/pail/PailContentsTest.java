package net.modcrafters.minecraft.barnyardromance.pail;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents.Moved;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PailContentsTest {
    private static final Identifier GOAT = Identifier.fromNamespaceAndPath("barnyard_romance", "goat_milk");
    private static final Identifier SHEEP = Identifier.fromNamespaceAndPath("barnyard_romance", "sheep_milk");

    @Test
    void fillingAnEmptyPailSetsItsFluid() {
        Moved moved = PailContents.EMPTY.fill(GOAT, 1000, 2000);
        assertEquals(new PailContents(GOAT, 1000), moved.contents());
        assertEquals(1000, moved.amount());
    }

    @Test
    void aPailRefusesASecondFluid() {
        Moved moved = new PailContents(GOAT, 1000).fill(SHEEP, 1000, 2000);
        assertEquals(0, moved.amount());
        assertEquals(new PailContents(GOAT, 1000), moved.contents());
    }

    @Test
    void fillingStopsAtCapacity() {
        assertEquals(500, new PailContents(GOAT, 1500).fill(GOAT, 1000, 2000).amount());
    }

    @Test
    void drainingTheLastDropEmptiesTheFluid() {
        assertEquals(PailContents.EMPTY, new PailContents(GOAT, 1000).drain(1000).contents());
    }

    @Test
    void drainingMoreThanThereIsMovesWhatThereIs() {
        assertEquals(300, new PailContents(GOAT, 300).drain(1000).amount());
    }

    @Test
    void zeroAmountIsEmpty() {
        assertTrue(new PailContents(GOAT, 0).isEmpty());
        assertNull(new PailContents(GOAT, 0).fluid());
    }

    @Test
    void negativeRequestsMoveNothing() {
        assertEquals(0, new PailContents(GOAT, 500).drain(-5).amount());
        assertEquals(0, PailContents.EMPTY.fill(GOAT, -5, 2000).amount());
    }

    @Test
    void anOverfullPailHasNoSpaceAndTakesNothing() {
        // A capacity can shrink under a pail (a config or datapack change), so space never goes negative.
        PailContents over = new PailContents(GOAT, 2500);
        assertEquals(0, over.space(2000));
        assertEquals(0, over.fill(GOAT, 100, 2000).amount());
    }

    @Test
    void holdsOnlyItsOwnFluid() {
        assertTrue(new PailContents(GOAT, 10).holds(GOAT));
        assertEquals(false, new PailContents(GOAT, 10).holds(SHEEP));
        assertEquals(false, PailContents.EMPTY.holds(GOAT));
    }

    @Test
    void theCodecRoundTrips() {
        PailContents contents = new PailContents(GOAT, 1500);
        JsonElement json = PailContents.CODEC.encodeStart(JsonOps.INSTANCE, contents).getOrThrow();
        assertEquals(contents, PailContents.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void anUnknownFluidIdSurvivesTheCodec() {
        PailContents gone = new PailContents(Identifier.fromNamespaceAndPath("gone", "whey"), 400);
        JsonElement json = PailContents.CODEC.encodeStart(JsonOps.INSTANCE, gone).getOrThrow();
        assertEquals(gone, PailContents.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void aMissingAmountReadsAsEmpty() {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", GOAT.toString());
        assertEquals(PailContents.EMPTY, PailContents.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void theStreamCodecRoundTrips() {
        for (PailContents contents : new PailContents[] {PailContents.EMPTY, new PailContents(GOAT, 2000), new PailContents(Identifier.fromNamespaceAndPath("gone", "whey"), 7)}) {
            ByteBuf buf = Unpooled.buffer();
            PailContents.STREAM_CODEC.encode(buf, contents);
            assertEquals(contents, PailContents.STREAM_CODEC.decode(buf));
        }
    }
}
