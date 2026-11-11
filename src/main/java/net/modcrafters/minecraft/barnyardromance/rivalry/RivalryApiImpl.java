package net.modcrafters.minecraft.barnyardromance.rivalry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.rivalry.api.RivalryApi;
import net.modcrafters.minecraft.barnyardromance.rivalry.api.Walk;

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

    @Override
    public boolean canWalkTo(Animal from, BlockPos to, int range) {
        return Reach.canWalkTo(from, to, range);
    }

    @Override
    public Walk walkTo(Animal from, BlockPos to, int range) {
        return Reach.walkTo(from, to, range);
    }

    @Override
    public boolean walkBlocked(Animal from, Animal to, int range) {
        return Reach.blocked(from, to, range);
    }
}
