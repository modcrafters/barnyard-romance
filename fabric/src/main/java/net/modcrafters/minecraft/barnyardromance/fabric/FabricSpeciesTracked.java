package net.modcrafters.minecraft.barnyardromance.fabric;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.modcrafters.minecraft.barnyardromance.advancement.SpeciesTracked;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import org.jspecify.annotations.Nullable;

import java.util.List;

// barnyard_romance:species_tracked in a ladder's fabric:load_conditions.
public record FabricSpeciesTracked(List<String> species) implements ResourceCondition {
    private static final ResourceConditionType<FabricSpeciesTracked> TYPE = ResourceConditionType.create(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, SpeciesTracked.ID),
            SpeciesTracked.SPECIES.xmap(FabricSpeciesTracked::new, FabricSpeciesTracked::species)
    );

    static void register() {
        ResourceConditions.register(TYPE);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
        return SpeciesTracked.test(species);
    }
}
