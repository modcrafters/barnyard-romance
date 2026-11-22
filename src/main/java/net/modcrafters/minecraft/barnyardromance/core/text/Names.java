package net.modcrafters.minecraft.barnyardromance.core.text;

import net.modcrafters.minecraft.barnyardromance.api.AgeStage;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// The shown names of the public API's values and of entity types; the API itself carries no text, so its language keys
// stay here.
public final class Names {
    private Names() {
    }

    public static Text of(Sex sex) {
        return sex == Sex.FEMALE ? Text.of("gui.barnyard_romance.sex.female") : Text.of("gui.barnyard_romance.sex.male");
    }

    public static Text of(AgeStage stage) {
        return Text.of(switch (stage) {
            case BABY -> "gui.barnyard_romance.age.baby";
            case YOUNG -> "gui.barnyard_romance.age.young";
            case ADULT -> "gui.barnyard_romance.age.adult";
            case OLD -> "gui.barnyard_romance.age.old";
        });
    }

    // An entity type's translatable name by its id; empty when it isn't registered, so a line can be left out.
    public static Text entity(String entityId) {
        Identifier id = Identifier.tryParse(entityId);
        return id == null ? Text.EMPTY : BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(type -> Text.of(type.getDescriptionId()))
                .orElse(Text.EMPTY);
    }

    // The animal's mod id when another loaded entity type has the same name, as ducks from two mods do; else empty.
    public static Optional<String> sharedNameMod(String entityId) {
        Map<String, String> names = new HashMap<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            names.put(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(), type.getDescription().getString());
        }
        return sharedNameMod(entityId, names);
    }

    // The same, given every entity type's shown name by its id.
    static Optional<String> sharedNameMod(String entityId, Map<String, String> namesById) {
        String name = namesById.get(entityId);
        boolean shared = name != null && namesById.entrySet().stream()
                .anyMatch(other -> !other.getKey().equals(entityId) && other.getValue().equals(name));
        return shared ? Optional.of(modId(entityId)) : Optional.empty();
    }

    // The namespace of an id such as "naturalist:duck"; an id without one is Minecraft's.
    public static String modId(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }
}
