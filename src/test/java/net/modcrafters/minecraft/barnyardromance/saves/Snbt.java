package net.modcrafters.minecraft.barnyardromance.saves;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
//? if >=1.21.5 {
import net.minecraft.nbt.NbtOps;
//?} else
//import com.mojang.brigadier.StringReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

// Reads the save fixtures under src/test/resources/saves: SNBT text, the way /data prints NBT.
final class Snbt {
    private Snbt() {
    }

    static Tag parse(String text) {
        try {
            //? if >=1.21.5 {
            return TagParser.create(NbtOps.INSTANCE).parseFully(text);
            //?} else {
            /*StringReader reader = new StringReader(text);
            Tag tag = new TagParser(reader).readValue();
            reader.skipWhitespace();
            if (reader.canRead()) {
                throw TagParser.ERROR_TRAILING_DATA.createWithContext(reader);
            }
            return tag;
            *///?}
        } catch (CommandSyntaxException exception) {
            throw new IllegalArgumentException("not valid SNBT: " + text, exception);
        }
    }

    // The fixture saves/<name>.snbt.
    static Tag fixture(String name) {
        String path = "saves/" + name + ".snbt";
        try (InputStream in = Snbt.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("missing fixture " + path);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
