package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.rivalry.api.RivalryApi;

final class RivalryApiImpl implements RivalryApi {
    @Override
    public boolean chosen(Animal female, Animal male) {
        return Courtship.chosen(female, male);
    }

    @Override
    public int restAfterMating(Animal animal, int rest) {
        return Courtship.restAfterMating(animal, rest);
    }

    @Override
    public boolean killedByRival(Animal animal) {
        return Fights.killedByRival(animal);
    }
}
