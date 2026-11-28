package net.modcrafters.minecraft.barnyardromance.feed;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeederTooltipTest {
    private static List<Component> names(String... names) {
        return Stream.of(names).<Component>map(Component::literal).toList();
    }

    // The first line is "Feeds: %s"; this is what fills the %s.
    private static String firstRow(List<Component> lines) {
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, lines.get(0).getContents());
        assertEquals(FeederTooltip.KEY, contents.getKey());
        assertEquals(1, contents.getArgs().length);
        return ((Component) contents.getArgs()[0]).getString();
    }

    private static void assertGrey(List<Component> lines) {
        for (Component line : lines) {
            assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY), line.getStyle().getColor());
        }
    }

    @Test
    void noNamesNoLines() {
        assertTrue(FeederTooltip.lines(List.of()).isEmpty());
    }

    @Test
    void oneName() {
        List<Component> lines = FeederTooltip.lines(names("A"));
        assertEquals(1, lines.size());
        assertEquals("A", firstRow(lines));
        assertGrey(lines);
    }

    @Test
    void fourNamesOneLine() {
        List<Component> lines = FeederTooltip.lines(names("A", "B", "C", "D"));
        assertEquals(1, lines.size());
        assertEquals("A, B, C, D", firstRow(lines));
        assertGrey(lines);
    }

    @Test
    void fiveNamesBreakAfterFour() {
        List<Component> lines = FeederTooltip.lines(names("A", "B", "C", "D", "E"));
        assertEquals(2, lines.size());
        assertEquals("A, B, C, D,", firstRow(lines));
        assertEquals("E", lines.get(1).getString());
        assertGrey(lines);
    }

    @Test
    void eightNamesTwoFullLines() {
        List<Component> lines = FeederTooltip.lines(names("A", "B", "C", "D", "E", "F", "G", "H"));
        assertEquals(2, lines.size());
        assertEquals("A, B, C, D,", firstRow(lines));
        assertEquals("E, F, G, H", lines.get(1).getString());
        assertGrey(lines);
    }
}
