package net.modcrafters.minecraft.barnyardromance.advancement;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.advancement.api.AdvancementsApi;
import net.modcrafters.minecraft.barnyardromance.core.Feature;
import net.modcrafters.minecraft.barnyardromance.core.FeatureRegistrar;
import net.modcrafters.minecraft.barnyardromance.core.event.AnimalEvent;

public final class AdvancementFeature implements Feature {
    @Override
    public String name() {
        return "advancement";
    }

    @Override
    public void register(FeatureRegistrar registrar) {
        registrar.provide(AdvancementsApi.class, BarnyardAdvancements::breeder);
        registrar.listen(AnimalEvent.Conceived.class, event -> BarnyardAdvancements.conceived(event.mother(), event.breeder()));
        registrar.listen(AnimalEvent.Born.class, AdvancementFeature::born);
        registrar.listen(AnimalEvent.Hatched.class, AdvancementFeature::hatched);
        registrar.listen(AnimalEvent.Befriended.class, event -> BarnyardAdvancements.befriended(event.animal(), event.player()));
        registrar.listen(AnimalEvent.Milked.class, event -> BarnyardAdvancements.milked(event.animal(), event.player()));
        registrar.listen(AnimalEvent.Sheared.class, event -> BarnyardAdvancements.sheared(event.animal(), event.shearer()));
        registrar.listen(AnimalEvent.AddedToHerdBook.class, event -> BarnyardAdvancements.registered(event.animal(), event.player()));
        registrar.listen(AnimalEvent.Died.class, event -> BarnyardAdvancements.died(event.animal(), event.source()));
        registrar.listen(AnimalEvent.FeederMeal.class, event -> BarnyardAdvancements.feederMeal(event.animal()));
        registrar.listen(AnimalEvent.FightStarted.class, event -> BarnyardAdvancements.fightStarted(event.a(), event.b()));
        registrar.listen(AnimalEvent.FamiliarityFell.class,
                event -> BarnyardAdvancements.familiarityFell(event.animal(), event.before(), event.after()));
    }

    // Vanilla breeding conceives and births at once. A pregnancy's birth also counts each young for Two by Two, which
    // vanilla counted at mating for a vanilla birth.
    private static void born(AnimalEvent.Born event) {
        Animal mother = event.mother();
        if (event.birth() == AnimalEvent.Birth.VANILLA) {
            BarnyardAdvancements.conceived(mother, event.breeder());
            BarnyardAdvancements.born(mother, event.young(), event.breeder());
            return;
        }
        BarnyardAdvancements.born(mother, event.young(), event.breeder());
        if (mother.level() instanceof ServerLevel level) {
            for (Animal baby : event.young()) {
                BarnyardAdvancements.twoByTwo(level, event.breeder(), mother, baby);
            }
        }
    }

    private static void hatched(AnimalEvent.Hatched event) {
        if (event.hatch() == AnimalEvent.Hatch.NATURALIST_EGG) {
            BarnyardAdvancements.hatchedFromNaturalistEgg(event.level(), event.chick());
        } else {
            BarnyardAdvancements.hatched(event.level(), event.chick(), event.parents());
        }
    }
}
