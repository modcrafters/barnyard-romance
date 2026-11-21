package net.modcrafters.minecraft.barnyardromance.shear;

import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearApi;
import net.modcrafters.minecraft.barnyardromance.shear.api.ShearState;

final class ShearApiImpl implements ShearApi {
    @Override
    public ShearState shearState(boolean shearable, boolean woolly, boolean baby, AnimalTraits traits, long clock) {
        return ShearRule.state(shearable, woolly, baby, traits, clock);
    }

    @Override
    public boolean tracksShearing(Animal animal) {
        return ShearActions.tracksShearing(animal);
    }

    @Override
    public boolean readyForShearing(Animal animal) {
        return animal instanceof Shearable shearable && shearable.readyForShearing();
    }
}
