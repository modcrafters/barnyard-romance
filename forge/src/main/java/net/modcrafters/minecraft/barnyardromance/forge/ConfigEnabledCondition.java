package net.modcrafters.minecraft.barnyardromance.forge;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.ConfigEnabled;

// barnyard_romance:config_enabled in a file's Forge conditions. Forge 1.20.1 reads conditions from JSON, so the
// serializer reads the same "key" field as the codec the other loaders use.
public record ConfigEnabledCondition(String key) implements ICondition {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, ConfigEnabled.ID);

    static void register() {
        CraftingHelper.register(Serializer.INSTANCE);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return ConfigEnabled.test(key);
    }

    public static final class Serializer implements IConditionSerializer<ConfigEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ConfigEnabledCondition value) {
            ConfigEnabled.KEY.codec().encodeStart(JsonOps.INSTANCE, value.key()).result().ifPresent(written ->
                    written.getAsJsonObject().entrySet().forEach(field -> json.add(field.getKey(), field.getValue())));
        }

        @Override
        public ConfigEnabledCondition read(JsonObject json) {
            return new ConfigEnabledCondition(ConfigEnabled.KEY.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {}));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
