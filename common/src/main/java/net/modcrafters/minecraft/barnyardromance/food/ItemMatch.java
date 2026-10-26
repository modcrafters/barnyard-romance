package net.modcrafters.minecraft.barnyardromance.food;

import java.util.regex.Pattern;

// One "items" entry: "#ns:path" is an item tag, "ns:path" an item. A bare path means minecraft.
public record ItemMatch(boolean tag, String id) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    public static ItemMatch parse(String text) {
        boolean tag = text.startsWith("#");
        return new ItemMatch(tag, id(tag ? text.substring(1) : text));
    }

    // Namespaced ids, as Minecraft writes them; throws on anything Minecraft would reject.
    public static String id(String text) {
        String full = text.indexOf(':') < 0 ? "minecraft:" + text : text;
        if (!ID.matcher(full).matches()) {
            throw new IllegalArgumentException("Not a valid id: " + text);
        }
        return full;
    }

    @Override
    public String toString() {
        return (tag ? "#" : "") + id;
    }
}
