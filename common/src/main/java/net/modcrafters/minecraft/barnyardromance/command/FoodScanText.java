package net.modcrafters.minecraft.barnyardromance.command;

import java.util.Map;
import java.util.StringJoiner;

public final class FoodScanText {
    private FoodScanText() {
    }

    public static String summary(int recipes, int items, int tags, String folder, Map<String, Integer> skipped) {
        String text = "Scanned " + recipes + " recipes: added " + items + " items to " + tags + (tags == 1 ? " tag" : " tags")
                + ". Wrote " + folder + ". Copy the barnyard_romance_scan folder into your world's datapacks and run /reload.";
        if (skipped.isEmpty()) {
            return text;
        }
        StringJoiner unread = new StringJoiner(", ", " Couldn't read: ", ".");
        skipped.forEach((type, count) -> unread.add(type + " (" + count + ")"));
        return text + unread;
    }
}
