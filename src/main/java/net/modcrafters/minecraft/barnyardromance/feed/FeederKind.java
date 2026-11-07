package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

// The five feeding blocks. Each serves the animals in its entity tag, barnyard_romance:feeds_from/<id>, so a pack can
// move animals between them.
public enum FeederKind {
    BIRD_FEEDER("bird_feeder"),
    TROUGH("trough"),
    FEEDER("feeder"),
    MANGER("manger"),
    PET_BOWL("pet_bowl");

    private final String id;
    // Made on first use, so the enum loads without the game's registries.
    private TagKey<EntityType<?>> animals;

    FeederKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    String tagPath() {
        return "feeds_from/" + id;
    }

    public TagKey<EntityType<?>> animals() {
        if (animals == null) {
            animals = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, tagPath()));
        }
        return animals;
    }

    // The first block whose tag holds the animal's species.
    public static Optional<FeederKind> of(Animal animal) {
        for (FeederKind kind : values()) {
            if (animal.getType().builtInRegistryHolder().is(kind.animals())) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
