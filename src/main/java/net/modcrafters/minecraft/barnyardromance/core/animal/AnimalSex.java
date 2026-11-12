package net.modcrafters.minecraft.barnyardromance.core.animal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimalSex {
    private static final Map<EntityType<?>, String> SPECIES_IDS = new ConcurrentHashMap<>();

    private AnimalSex() {
    }

    // Asked several times a tick for every animal, so each type's id is made into a string once.
    public static String speciesId(Animal animal) {
        return SPECIES_IDS.computeIfAbsent(animal.getType(), type -> {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            return id == null ? "unknown" : id.toString();
        });
    }

    public static double currentScale(Animal animal) {
        AttributeInstance attribute = animal.getAttribute(Attributes.SCALE);
        return attribute == null ? 1.0 : attribute.getBaseValue();
    }

    // Only the animals familiarity tames (wolves, cats, horses and the rest of the horse family) count: a parrot tamed
    // with seeds, or another mod's pet, can't be fed back up, so it never turns grumpy.
    public static boolean tamed(Animal animal) {
        if (animal instanceof Wolf || animal instanceof Cat) {
            return ((TamableAnimal) animal).isTame();
        }
        return animal instanceof AbstractHorse horse && horse.isTamed();
    }

    // These only breed when tamed; ocelots trust instead, which never blocks breeding.
    public static boolean needsTaming(Animal animal) {
        return animal instanceof AbstractHorse || animal instanceof Wolf || animal instanceof Cat;
    }
}
