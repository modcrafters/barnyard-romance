package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FoodScanText {
    private FoodScanText() {
    }

    public static Text summary(int recipes, int items, int tags, String folder, Map<String, Integer> skipped) {
        Text text = tags == 1
                ? Text.of("commands.barnyard_romance.foods.scan.done_one_tag", recipes, items, tags, folder)
                : Text.of("commands.barnyard_romance.foods.scan.done", recipes, items, tags, folder);
        if (skipped.isEmpty()) {
            return text;
        }
        List<Text> unread = new ArrayList<>();
        skipped.forEach((type, count) -> unread.add(Text.of("commands.barnyard_romance.foods.scan.recipe_type", type, count)));
        return Text.join(" ", List.of(text, Text.of("commands.barnyard_romance.foods.scan.unread", Text.join(", ", unread))));
    }
}
