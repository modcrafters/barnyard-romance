package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 1.21.1 reads item models only from models/item; the items/ definitions came in 1.21.4.
class ItemModelsTest {
    // An item's name key, e.g. "item.barnyard_romance.herd_stick", but not "item.barnyard_romance.herd_stick.use".
    private static final Pattern ITEM_NAME = Pattern.compile("\"item\\." + Constants.MOD_ID + "\\.([a-z0-9_]+)\"\\s*:");

    @Test
    void everyItemHasAnItemModel() throws IOException {
        String lang;
        try (InputStream in = resource("assets/" + Constants.MOD_ID + "/lang/en_us.json")) {
            assertNotNull(in, "the lang file is missing");
            lang = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<String> items = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        Matcher matcher = ITEM_NAME.matcher(lang);
        while (matcher.find()) {
            items.add(matcher.group(1));
            String model = "assets/" + Constants.MOD_ID + "/models/item/" + matcher.group(1) + ".json";
            try (InputStream in = resource(model)) {
                if (in == null) {
                    missing.add(model);
                }
            }
        }
        assertFalse(items.isEmpty(), "no item names in the lang file");
        assertEquals(List.of(), missing);
    }

    private static InputStream resource(String path) {
        return ItemModelsTest.class.getClassLoader().getResourceAsStream(path);
    }
}
