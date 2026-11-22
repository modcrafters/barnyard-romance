package net.modcrafters.minecraft.barnyardromance.neoforge;

import com.mojang.serialization.MapCodec;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.ConfigEnabled;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// barnyard_romance:config_enabled in a file's neoforge:conditions.
public record ConfigEnabledCondition(String key) implements ICondition {
    public static final MapCodec<ConfigEnabledCondition> CODEC = ConfigEnabled.KEY.xmap(ConfigEnabledCondition::new, ConfigEnabledCondition::key);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
            NeoForgeRegistries.CONDITION_SERIALIZERS,
            Constants.MOD_ID
    );

    static void register(IEventBus modBus) {
        CONDITIONS.register(ConfigEnabled.ID, () -> CODEC);
        CONDITIONS.register(modBus);
    }

    @Override
    public boolean test(IContext context) {
        return ConfigEnabled.test(key);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
