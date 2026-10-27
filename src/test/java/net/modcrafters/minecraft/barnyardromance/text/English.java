package net.modcrafters.minecraft.barnyardromance.text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Shows a Text the way Minecraft would in English, from en_us.json, so tests can check what players read.
// A missing key or argument fails the test instead of falling back like the game does.
public final class English {
    private static final String LANG_FILE = "assets/barnyard_romance/lang/en_us.json";
    // Minecraft's own placeholders: %s, %1$s and %%.
    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:(\\d+)\\$)?([s%])");
    private static Map<String, String> lang;

    private English() {
    }

    public static String of(Text text) {
        return of(text, lang());
    }

    public static String of(Text text, Map<String, String> lang) {
        return switch (text) {
            case Text.Plain plain -> plain.value();
            case Text.Join join -> {
                StringBuilder joined = new StringBuilder();
                for (int index = 0; index < join.parts().size(); index++) {
                    if (index > 0) {
                        joined.append(join.separator());
                    }
                    joined.append(of(join.parts().get(index), lang));
                }
                yield joined.toString();
            }
            case Text.Key key -> fill(key, lang);
        };
    }

    public static synchronized Map<String, String> lang() {
        if (lang == null) {
            try (InputStream in = English.class.getClassLoader().getResourceAsStream(LANG_FILE)) {
                if (in == null) {
                    throw new AssertionError(LANG_FILE + " is missing");
                }
                lang = parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException exception) {
                throw new AssertionError("Couldn't read " + LANG_FILE, exception);
            }
        }
        return lang;
    }

    private static String fill(Text.Key key, Map<String, String> lang) {
        String format = lang.get(key.key());
        if (format == null) {
            throw new AssertionError("No lang entry for " + key.key());
        }
        Matcher matcher = PLACEHOLDER.matcher(format);
        StringBuilder out = new StringBuilder();
        int next = 0;
        while (matcher.find()) {
            String argument;
            if (matcher.group(2).equals("%")) {
                argument = "%";
            } else {
                int index = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) - 1 : next++;
                if (index >= key.args().size()) {
                    throw new AssertionError(key.key() + " needs more than " + key.args().size() + " arguments: " + format);
                }
                Object value = key.args().get(index);
                argument = value instanceof Text text ? of(text, lang) : String.valueOf(value);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(argument));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    // en_us.json is one flat object of strings, so a small reader does; Gson is not on the test compile classpath.
    static Map<String, String> parse(String json) {
        Map<String, String> entries = new LinkedHashMap<>();
        Matcher entry = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        while (entry.find()) {
            entries.put(unescape(entry.group(1)), unescape(entry.group(2)));
        }
        return entries;
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (c != '\\') {
                out.append(c);
                continue;
            }
            char escaped = value.charAt(++index);
            switch (escaped) {
                case 'n' -> out.append('\n');
                case 't' -> out.append('\t');
                case 'u' -> {
                    out.append((char) Integer.parseInt(value.substring(index + 1, index + 5), 16));
                    index += 4;
                }
                default -> out.append(escaped);
            }
        }
        return out.toString();
    }
}
