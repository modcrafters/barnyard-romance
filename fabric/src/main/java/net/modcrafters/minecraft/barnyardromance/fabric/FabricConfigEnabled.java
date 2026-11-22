package net.modcrafters.minecraft.barnyardromance.fabric;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.ConfigEnabled;
import org.jspecify.annotations.Nullable;

// barnyard_romance:config_enabled in a file's fabric:load_conditions.
public record FabricConfigEnabled(String key) implements ResourceCondition {
    private static final ResourceConditionType<FabricConfigEnabled> TYPE = ResourceConditionType.create(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, ConfigEnabled.ID),
            ConfigEnabled.KEY.xmap(FabricConfigEnabled::new, FabricConfigEnabled::key)
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
        return ConfigEnabled.test(key);
    }
}
