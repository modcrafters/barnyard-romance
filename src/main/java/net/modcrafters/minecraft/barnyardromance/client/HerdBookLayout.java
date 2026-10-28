package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.text.Text;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// How the herd book is laid out: species bookmarks, contents pages grouped Females, Males, Kids, and the order
// the arrows step through. Names come in already translated, so this has no Minecraft types and is unit tested.
public final class HerdBookLayout {
    public static final int LINES = 12;
    // The height the bookmarks share down the book's right edge.
    public static final int TAB_SPACE = 176;
    public static final int MAX_TAB = 24;
    // A tab still fits the 8-pixel font; past that, the tabs go on in another column.
    public static final int MIN_TAB = 9;

    public record Row(UUID id, String species, String speciesName, String name, boolean female, boolean kid, boolean alive) {
    }

    public enum Group {
        FEMALES,
        MALES,
        KIDS
    }

    public sealed interface Line permits Header, Animal {
    }

    public record Header(Group group) implements Line {
    }

    public record Animal(UUID id) implements Line {
    }

    private HerdBookLayout() {
    }

    // Species with at least one living animal, sorted by their translated names.
    public static List<String> bookmarks(List<Row> rows) {
        return rows.stream()
                .filter(Row::alive)
                .sorted(Comparator.comparing(Row::speciesName).thenComparing(Row::species))
                .map(Row::species)
                .distinct()
                .toList();
    }

    public static int tabColumns(int count) {
        int perColumn = TAB_SPACE / MIN_TAB;
        return Math.max(1, (count + perColumn - 1) / perColumn);
    }

    public static int tabHeight(int count) {
        int columns = tabColumns(count);
        int rows = Math.max(1, (count + columns - 1) / columns);
        return Math.max(MIN_TAB, Math.min(MAX_TAB, TAB_SPACE / rows));
    }

    // Young are listed one per line; when they don't all fit, the last line says how many more there are.
    public static int youngShown(int count, int linesLeft) {
        if (count <= linesLeft) {
            return count;
        }
        return Math.max(0, linesLeft - 1);
    }

    public static List<List<Line>> contents(List<Row> rows, String species) {
        List<Line> lines = new ArrayList<>();
        for (Group group : Group.values()) {
            List<Row> members = rows.stream()
                    .filter(row -> row.alive() && row.species().equals(species) && group(row) == group)
                    .sorted(Comparator.comparing(Row::name).thenComparing(Row::id))
                    .toList();
            if (members.isEmpty()) {
                continue;
            }
            lines.add(new Header(group));
            members.forEach(row -> lines.add(new Animal(row.id())));
        }
        List<List<Line>> pages = new ArrayList<>();
        List<Line> page = new ArrayList<>();
        for (Line line : lines) {
            // A header goes to the next page rather than end this one with nothing under it.
            boolean full = page.size() == LINES || line instanceof Header && page.size() == LINES - 1;
            if (full) {
                pages.add(page);
                page = new ArrayList<>();
            }
            page.add(line);
        }
        pages.add(page);
        return pages;
    }

    public static List<UUID> order(List<List<Line>> pages) {
        List<UUID> order = new ArrayList<>();
        for (List<Line> page : pages) {
            for (Line line : page) {
                if (line instanceof Animal animal) {
                    order.add(animal.id());
                }
            }
        }
        return order;
    }

    // The contents page an animal is listed on, or 0 when it isn't listed.
    public static int pageOf(List<List<Line>> pages, UUID id) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).contains(new Animal(id))) {
                return i;
            }
        }
        return 0;
    }

    public static @Nullable UUID previous(List<UUID> order, UUID current) {
        int index = order.indexOf(current);
        return index > 0 ? order.get(index - 1) : null;
    }

    public static @Nullable UUID next(List<UUID> order, UUID current) {
        int index = order.indexOf(current);
        return index >= 0 && index + 1 < order.size() ? order.get(index + 1) : null;
    }

    // A custom name, else "<mother>'s young" when the mother in the book has a name, else the species.
    public static Text displayName(String name, String speciesName, String motherName) {
        if (!name.isEmpty()) {
            return Text.plain(name);
        }
        if (!motherName.isEmpty()) {
            return Text.of("gui.barnyard_romance.herd_book.young_of", motherName);
        }
        return Text.plain(speciesName);
    }

    private static Group group(Row row) {
        if (row.kid()) {
            return Group.KIDS;
        }
        return row.female() ? Group.FEMALES : Group.MALES;
    }
}
