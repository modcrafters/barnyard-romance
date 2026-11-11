package net.modcrafters.minecraft.barnyardromance.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.modcrafters.minecraft.barnyardromance.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

class VanillaRecipeTest {
    private static final String CAKE = "data/minecraft/recipe/cake.json";
    private static final String LANG = "assets/" + Constants.MOD_ID + "/lang/en_us.json";

    // Only one mod's copy of a vanilla recipe survives, and which one depends on load order.
    // Naturalist replaces cake too, so the mod adds its own recipe beside vanilla's.
    @Test
    void doesNotReplaceVanillaCake() throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        // Minecraft's own jar is on the classpath too, so only the mod's resources count: the folder that holds its
        // lang file (src/main/resources and the versions/<mc> overrides, processed together).
        URL lang = loader.getResource(LANG);
        assertNotNull(lang, "the lang file is missing");
        String modRoot = lang.toString().substring(0, lang.toString().length() - LANG.length());

        List<URL> shipped = Collections.list(loader.getResources(CAKE)).stream()
                .filter(url -> url.toString().startsWith(modRoot))
                .toList();

        assertEquals(List.of(), shipped);
    }
}
