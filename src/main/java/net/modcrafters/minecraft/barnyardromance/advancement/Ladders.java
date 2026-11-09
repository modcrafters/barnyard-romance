package net.modcrafters.minecraft.barnyardromance.advancement;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// The breeding ladders: an advancement at 10 births of a kind of livestock, and another at 100. The horse family
// shares one ladder, and trader llamas count as llamas. Naturalist's ladders only load with Naturalist.
public final class Ladders {
    public static final List<Integer> STEPS = List.of(10, 100);

    private static final Set<String> VANILLA = Set.of("cow", "mooshroom", "sheep", "pig", "goat", "horse", "llama", "camel", "rabbit", "chicken");
    private static final Set<String> NATURALIST = Set.of("deer", "boar", "duck", "turkey", "ostrich");
    private static final Map<String, String> SHARED = Map.of(
            "minecraft:donkey", "horse",
            "minecraft:mule", "horse",
            "minecraft:trader_llama", "llama"
    );

    private Ladders() {
    }

    public static Optional<String> ladderFor(String speciesId) {
        String shared = SHARED.get(speciesId);
        if (shared != null) {
            return Optional.of(shared);
        }
        int colon = speciesId.indexOf(':');
        String namespace = colon < 0 ? "minecraft" : speciesId.substring(0, colon);
        String path = speciesId.substring(colon + 1);
        Set<String> ladders = switch (namespace) {
            case "minecraft" -> VANILLA;
            case "naturalist" -> NATURALIST;
            default -> Set.of();
        };
        return ladders.contains(path) ? Optional.of(path) : Optional.empty();
    }

    // The steps a count passed on its way from before to after.
    public static List<Integer> crossed(int before, int after) {
        return STEPS.stream().filter(step -> before < step && step <= after).toList();
    }

    public static Identifier advancement(String ladder, int step) {
        return BarnyardAdvancements.id("ladder/" + ladder + "_" + step);
    }
}
