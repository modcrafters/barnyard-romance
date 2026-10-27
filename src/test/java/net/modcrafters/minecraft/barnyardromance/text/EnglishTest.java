package net.modcrafters.minecraft.barnyardromance.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnglishTest {
    private static final Map<String, String> LANG = Map.of(
            "a.plain", "Ready",
            "a.one", "Needs %s familiarity",
            "a.two", "%2$s before %1$s",
            "a.percent", "%s%%",
            "a.wrap", "Resting %s"
    );

    @Test
    void plainTextIsShownAsItIs() {
        assertEquals("Bessie", English.of(Text.plain("Bessie"), LANG));
    }

    @Test
    void argumentsFillThePlaceholdersInOrder() {
        assertEquals("Needs 75% familiarity", English.of(Text.of("a.one", "75%"), LANG));
        assertEquals("second before first", English.of(Text.of("a.two", "first", "second"), LANG));
        assertEquals("40%", English.of(Text.of("a.percent", 40), LANG));
    }

    @Test
    void textArgumentsAreTranslatedToo() {
        assertEquals("Resting Ready", English.of(Text.of("a.wrap", Text.of("a.plain")), LANG));
    }

    @Test
    void joinedPartsGetTheSeparator() {
        assertEquals("Ready, 40%", English.of(Text.join(", ", List.of(Text.of("a.plain"), Text.of("a.percent", 40))), LANG));
        assertEquals("", English.of(Text.join(", ", List.of()), LANG));
    }

    @Test
    void aMissingKeyOrArgumentFails() {
        assertThrows(AssertionError.class, () -> English.of(Text.of("a.nope"), LANG));
        assertThrows(AssertionError.class, () -> English.of(Text.of("a.one"), LANG));
    }

    @Test
    void theRealLangFileLoads() {
        assertEquals("Herd Stick", English.of(Text.of("item.barnyard_romance.herd_stick")));
    }

    @Test
    void emptyMeansEmptyPlainText() {
        assertTrue(Text.EMPTY.isEmpty());
        assertTrue(Text.plain("").isEmpty());
        assertTrue(!Text.of("a.plain").isEmpty());
    }
}
