package net.modcrafters.minecraft.barnyardromance.look;

import com.google.gson.JsonElement;
import net.modcrafters.minecraft.barnyardromance.Constants;
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Loads assets/<ns>/barnyard_horns/*.json from the client's resource packs, keyed by entity id.
public final class HornShapes {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "horns");

    private static volatile Map<String, LoadedHorns> byEntity = Map.of();

    private HornShapes() {
    }

    public static LoadedHorns get(String entityId) {
        return byEntity.get(entityId);
    }

    public static PreparableReloadListener listener() {
        return new Listener();
    }

    // Sorted by file id, so when two files name the same entity the last id wins on every client.
    private static void replace(Map<Identifier, JsonElement> files) {
        Map<String, LoadedHorns> loaded = new HashMap<>();
        Map<String, Identifier> source = new HashMap<>();
        new TreeMap<>(files).forEach((id, json) -> {
            LoadedHorns horns;
            try {
                horns = LoadedHorns.bake(HornJson.from(json));
            } catch (RuntimeException exception) {
                Constants.LOG.error("Skipped barnyard horn file {}: {}", id, exception.getMessage());
                return;
            }
            for (String entity : horns.file().entities()) {
                Identifier earlier = source.put(entity, id);
                if (earlier != null) {
                    Constants.LOG.warn("Barnyard horn file {} replaces {} for {}", id, earlier, entity);
                }
                loaded.put(entity, horns);
            }
        });
        byEntity = Map.copyOf(loaded);
        Constants.LOG.info("Loaded barnyard horns for {} entities", loaded.size());
    }

    //? if >=1.21.2 {
    private static final class Listener extends SimpleJsonResourceReloadListener<JsonElement> {
    //?} else
    //private static final class Listener extends SimpleJsonResourceReloadListener {
        Listener() {
            //? if >=1.21.2 {
            super(ExtraCodecs.JSON, FileToIdConverter.json("barnyard_horns"));
            //?} else
            //super(new Gson(), "barnyard_horns");
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            replace(files);
        }
    }
}
