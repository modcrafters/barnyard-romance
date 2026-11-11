package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;
import net.modcrafters.minecraft.barnyardromance.herd.api.HerdApi;

import java.util.List;

public final class HerdFeature implements Feature {
    @Override
    public String name() {
        return "herd";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(HerdApi.class, HerdBooks::listed);
        registrar.listen(AnimalEvent.Born.class, HerdFeature::born);
        registrar.listen(AnimalEvent.Hatched.class, HerdFeature::hatched);
    }

    // Young join their mother's books. A herd book problem mustn't count as a failed birth.
    private static void born(AnimalEvent.Born event) {
        try {
            HerdBooks.born(event.mother(), event.young());
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to add {} young to herd books", AnimalSex.speciesId(event.mother()), exception);
        }
    }

    // Only a nesting box chick joins its mother's books; the chick is out by now, so a herd book problem mustn't count
    // as a failed hatch.
    private static void hatched(AnimalEvent.Hatched event) {
        Lineage parents = event.parents();
        if (event.hatch() != AnimalEvent.Hatch.NEST || parents == null || parents.mother() == null) {
            return;
        }
        Animal chick = event.chick();
        try {
            HerdBooks.born(event.level(), parents.mother(), List.of(chick));
        } catch (RuntimeException exception) {
            Constants.LOG.warn("Failed to add a hatched {} to herd books", AnimalSex.speciesId(chick), exception);
        }
    }
}
