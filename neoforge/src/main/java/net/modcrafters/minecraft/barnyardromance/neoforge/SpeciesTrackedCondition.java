package net.modcrafters.minecraft.barnyardromance.neoforge;

import com.mojang.serialization.MapCodec;
import net.modcrafters.minecraft.barnyardromance.advancement.SpeciesTracked;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

// barnyard_romance:species_tracked in a ladder's neoforge:conditions.
public record SpeciesTrackedCondition(List<String> species) implements ICondition {
    public static final MapCodec<SpeciesTrackedCondition> CODEC = SpeciesTracked.SPECIES.xmap(SpeciesTrackedCondition::new, SpeciesTrackedCondition::species);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
            NeoForgeRegistries.CONDITION_SERIALIZERS,
            Constants.MOD_ID
    );

    static void register(IEventBus modBus) {
        CONDITIONS.register(SpeciesTracked.ID, () -> CODEC);
        CONDITIONS.register(modBus);
    }

    @Override
    public boolean test(IContext context) {
        return SpeciesTracked.test(species);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
