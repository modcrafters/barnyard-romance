package net.modcrafters.minecraft.barnyardromance.look;

import com.google.gson.JsonElement;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
//? if >=1.21.2 {
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.util.ExtraCodecs;
//?} else
//import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Loads assets/<ns>/barnyard_heads/*.json from the client's resource packs: where each model's head is, for the herd
// book's close-ups (26.3 only, but loaded on every version).
public final class HeadShapes {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "heads");

    private static volatile List<HeadPaths> files = List.of();

    private HeadShapes() {
    }

    // The head paths to try for this animal, in order; vanilla's "head" when no file loaded.
    public static List<List<String>> candidates(String entityId) {
        List<List<String>> candidates = HeadPaths.candidates(entityId, files);
        return candidates.isEmpty() ? List.of(List.of("head")) : candidates;
    }

    public static PreparableReloadListener listener() {
        return new Listener();
    }

    // Sorted by file id, so every client tries the paths in the same order.
    private static void replace(Map<Identifier, JsonElement> found) {
        List<HeadPaths> loaded = new ArrayList<>();
        new TreeMap<>(found).forEach((id, json) -> {
            try {
                loaded.add(HeadPaths.from(json));
            } catch (RuntimeException exception) {
                Constants.LOG.error("Skipped barnyard head file {}: {}", id, exception.getMessage());
            }
        });
        files = List.copyOf(loaded);
        Constants.LOG.info("Loaded {} barnyard head files", loaded.size());
    }

    //? if >=1.21.2 {
    private static final class Listener extends SimpleJsonResourceReloadListener<JsonElement> {
    //?} else
    //private static final class Listener extends SimpleJsonResourceReloadListener {
        Listener() {
            //? if >=1.21.2 {
            super(ExtraCodecs.JSON, FileToIdConverter.json("barnyard_heads"));
            //?} else
            //super(new Gson(), "barnyard_heads");
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> found, ResourceManager manager, ProfilerFiller profiler) {
            replace(found);
        }
    }
}
