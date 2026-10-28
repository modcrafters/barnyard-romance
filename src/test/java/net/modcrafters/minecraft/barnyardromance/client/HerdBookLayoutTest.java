package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.text.English;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HerdBookLayoutTest {
    private static UUID id(int n) {
        return new UUID(0L, n);
    }

    private static HerdBookLayout.Row cow(int n, String name, boolean female, boolean kid) {
        return new HerdBookLayout.Row(id(n), "minecraft:cow", "Cow", name, female, kid, true);
    }

    private static List<HerdBookLayout.Line> flat(List<List<HerdBookLayout.Line>> pages) {
        return pages.stream().flatMap(List::stream).toList();
    }

    @Test
    void groupsGoFemalesMalesKids() {
        List<HerdBookLayout.Row> rows = List.of(cow(1, "Calf", true, true), cow(2, "Tom", false, false), cow(3, "Daisy", true, false));

        List<HerdBookLayout.Line> lines = flat(HerdBookLayout.contents(rows, "minecraft:cow"));

        assertEquals(List.of(
                new HerdBookLayout.Header(HerdBookLayout.Group.FEMALES), new HerdBookLayout.Animal(id(3)),
                new HerdBookLayout.Header(HerdBookLayout.Group.MALES), new HerdBookLayout.Animal(id(2)),
                new HerdBookLayout.Header(HerdBookLayout.Group.KIDS), new HerdBookLayout.Animal(id(1))
        ), lines);
    }

    @Test
    void rowsAreSortedByNameThenId() {
        List<HerdBookLayout.Row> rows = List.of(cow(2, "Daisy", true, false), cow(1, "Daisy", true, false), cow(3, "Bluebell", true, false));

        assertEquals(List.of(id(3), id(1), id(2)), HerdBookLayout.order(HerdBookLayout.contents(rows, "minecraft:cow")));
    }

    @Test
    void deadRowsAreNotListed() {
        List<HerdBookLayout.Row> rows = List.of(cow(1, "Daisy", true, false),
                new HerdBookLayout.Row(id(2), "minecraft:cow", "Cow", "Old Bess", true, false, false));

        assertEquals(List.of(id(1)), HerdBookLayout.order(HerdBookLayout.contents(rows, "minecraft:cow")));
    }

    @Test
    void twelveLinesPerPage() {
        List<HerdBookLayout.Row> rows = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            rows.add(cow(i, "Cow " + (char) ('a' + i), true, false));
        }

        List<List<HerdBookLayout.Line>> pages = HerdBookLayout.contents(rows, "minecraft:cow");

        assertEquals(2, pages.size());
        assertEquals(HerdBookLayout.LINES, pages.getFirst().size());
        assertEquals(21 - HerdBookLayout.LINES, pages.get(1).size());
    }

    @Test
    void aHeaderNeverEndsAPage() {
        List<HerdBookLayout.Row> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rows.add(cow(i, "Cow " + (char) ('a' + i), true, false));
        }
        rows.add(cow(50, "Tom", false, false));

        List<List<HerdBookLayout.Line>> pages = HerdBookLayout.contents(rows, "minecraft:cow");

        // Header + 10 females fill 11 lines; the males' header would be line 12, so it starts page 2.
        assertEquals(11, pages.getFirst().size());
        assertEquals(new HerdBookLayout.Header(HerdBookLayout.Group.MALES), pages.get(1).getFirst());
        assertEquals(1, HerdBookLayout.pageOf(pages, id(50)));
    }

    @Test
    void previousAndNextStopAtTheEnds() {
        List<UUID> order = List.of(id(1), id(2), id(3));

        assertNull(HerdBookLayout.previous(order, id(1)));
        assertEquals(id(1), HerdBookLayout.previous(order, id(2)));
        assertEquals(id(3), HerdBookLayout.next(order, id(2)));
        assertNull(HerdBookLayout.next(order, id(3)));
        assertNull(HerdBookLayout.next(order, id(9)));
    }

    @Test
    void orderSkipsOtherSpecies() {
        List<HerdBookLayout.Row> rows = List.of(cow(1, "Daisy", true, false),
                new HerdBookLayout.Row(id(2), "minecraft:pig", "Pig", "Babe", true, false, true));

        assertEquals(List.of(id(1)), HerdBookLayout.order(HerdBookLayout.contents(rows, "minecraft:cow")));
    }

    @Test
    void bookmarksOnlyForSpeciesWithLivingRows() {
        List<HerdBookLayout.Row> rows = List.of(
                new HerdBookLayout.Row(id(1), "minecraft:sheep", "Sheep", "Dolly", true, false, true),
                new HerdBookLayout.Row(id(2), "minecraft:pig", "Pig", "Old Babe", true, false, false),
                cow(3, "Daisy", true, false));

        assertEquals(List.of("minecraft:cow", "minecraft:sheep"), HerdBookLayout.bookmarks(rows));
    }

    @Test
    void bookmarksShrinkToFit() {
        assertEquals(24, HerdBookLayout.tabHeight(3));
        assertEquals(11, HerdBookLayout.tabHeight(16));
        assertTrue(HerdBookLayout.tabHeight(16) * 16 <= HerdBookLayout.TAB_SPACE);
        assertEquals(11, HerdBookLayout.tabHeight(30));
    }

    @Test
    void youngOfNamedMother() {
        assertEquals("Daisy's young", English.of(HerdBookLayout.displayName("", "Cow", "Daisy")));
    }

    @Test
    void speciesNameWhenNothingElse() {
        assertEquals("Cow", English.of(HerdBookLayout.displayName("", "Cow", "")));
        assertEquals("Bluebell", English.of(HerdBookLayout.displayName("Bluebell", "Cow", "Daisy")));
    }
}
