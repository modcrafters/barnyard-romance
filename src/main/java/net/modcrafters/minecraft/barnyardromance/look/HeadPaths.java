package net.modcrafters.minecraft.barnyardromance.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

// One assets/<ns>/barnyard_heads/*.json file: where a model's head is, for the herd book's close-ups. The first path
// a model has is framed. A file without entities is for every animal.
public record HeadPaths(List<String> entities, List<List<String>> head) {
    public HeadPaths {
        entities = List.copyOf(entities);
        head = head.stream().map(List::copyOf).toList();
    }

    public static HeadPaths from(JsonElement element) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("head file must be an object");
        }
        JsonObject object = element.getAsJsonObject();
        List<String> entities = new ArrayList<>();
        if (object.has("entities")) {
            for (JsonElement entry : list(object.get("entities"), "entities")) {
                entities.add(string(entry, "entities"));
            }
        }
        if (!object.has("head")) {
            throw new IllegalArgumentException("missing head");
        }
        List<List<String>> head = new ArrayList<>();
        for (JsonElement entry : list(object.get("head"), "head")) {
            head.add(HornJson.path(string(entry, "head")));
        }
        if (head.isEmpty()) {
            throw new IllegalArgumentException("head must name at least one part");
        }
        return new HeadPaths(entities, head);
    }

    // The paths to try for one animal: from the files that name it, then from the files for every animal, each in
    // the order given.
    public static List<List<String>> candidates(String entityId, List<HeadPaths> files) {
        List<List<String>> named = new ArrayList<>();
        List<List<String>> general = new ArrayList<>();
        for (HeadPaths file : files) {
            if (file.entities().isEmpty()) {
                general.addAll(file.head());
            } else if (file.entities().contains(entityId)) {
                named.addAll(file.head());
            }
        }
        named.addAll(general);
        return named;
    }

    private static JsonArray list(JsonElement element, String name) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException(name + " must be a list");
        }
        return element.getAsJsonArray();
    }

    private static String string(JsonElement element, String name) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(name + " entries must be strings");
        }
        return element.getAsString();
    }
}
