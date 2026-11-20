package net.modcrafters.minecraft.barnyardromance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;

import java.util.List;

// The load condition of the breeding ladders, barnyard_romance:species_tracked, which both loaders register under their
// own condition types. A ladder loads when any species it lists is registered and tracked. The config is read at
// startup, but entity tags aren't bound while conditions run, so the check leaves the tags out
// (speciesEnabledIgnoringTags): a pack that excludes an animal by tag still keeps its ladder.
public final class SpeciesTracked {
    public static final String ID = "species_tracked";
    // The loaders' condition types read the species list from this field.
    public static final MapCodec<List<String>> SPECIES = Codec.STRING.listOf().fieldOf("species");

    private SpeciesTracked() {
    }

    public static boolean test(List<String> species) {
        BarnyardConfig config = BarnyardConfig.current();
        return species.stream().anyMatch(id -> {
            Identifier parsed = Identifier.tryParse(id);
            return parsed != null && BuiltInRegistries.ENTITY_TYPE.containsKey(parsed) && config.speciesEnabledIgnoringTags(id);
        });
    }
}
