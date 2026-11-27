package net.modcrafters.minecraft.barnyardromance.forge;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.modcrafters.minecraft.barnyardromance.advancement.SpeciesTracked;
import net.modcrafters.minecraft.barnyardromance.core.Constants;

import java.util.List;

// barnyard_romance:species_tracked in a ladder's Forge conditions, read from JSON with the same "species" field as the
// codec the other loaders use.
public record SpeciesTrackedCondition(List<String> species) implements ICondition {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, SpeciesTracked.ID);

    static void register() {
        CraftingHelper.register(Serializer.INSTANCE);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return SpeciesTracked.test(species);
    }

    public static final class Serializer implements IConditionSerializer<SpeciesTrackedCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, SpeciesTrackedCondition value) {
            SpeciesTracked.SPECIES.codec().encodeStart(JsonOps.INSTANCE, value.species()).result().ifPresent(written ->
                    written.getAsJsonObject().entrySet().forEach(field -> json.add(field.getKey(), field.getValue())));
        }

        @Override
        public SpeciesTrackedCondition read(JsonObject json) {
            return new SpeciesTrackedCondition(SpeciesTracked.SPECIES.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {}));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
