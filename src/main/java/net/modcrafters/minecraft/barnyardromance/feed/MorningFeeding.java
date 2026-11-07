package net.modcrafters.minecraft.barnyardromance.feed;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// From sunrise to midnight a feeding block's herd walks over and eats, a few at a time and nearest first, so a big
// herd never paths all at once. Each round sends the hungry ones on (EatAtFeederGoal) while there's food, so food put
// in later in the day feeds whoever is still hungry at the next round. A pet told to sit isn't walked: it keeps its
// orders and eats where it sits if it's close enough.
public final class MorningFeeding {
    // A round a second, on the block's own tick of it.
    public static final int ROUND_TICKS = 20;
    public static final int MAX_WALKING = 4;
    // From a sitting pet to the block.
    static final double SITTING_REACH = 2.0;

    private MorningFeeding() {
    }

    // One round at this clock (the block's, see FeederBlockEntity.clock): the block's today is settled by now.
    public static void tick(FeederBlockEntity feeder, ServerLevel level, long clock) {
        if (!GameDay.afterSunrise(clock) || feeder.food() == 0) {
            return;
        }
        BlockPos pos = feeder.getBlockPos();
        long now = level.getGameTime();
        int walking = 0;
        List<Animal> hungry = new ArrayList<>();
        for (UUID id : feeder.herd()) {
            if (!(level.getEntity(id) instanceof Animal animal) || !animal.isAlive()) {
                continue;
            }
            // On its way already, here or to another block it eats at.
            if (EatAtFeederGoal.walking(animal, now)) {
                if (EatAtFeederGoal.headingTo(animal, pos)) {
                    walking++;
                }
                continue;
            }
            if (!FeederMeal.due(animal, clock)) {
                continue;
            }
            if (animal instanceof TamableAnimal pet && pet.isOrderedToSit()) {
                if (EatAtFeederGoal.within(animal, pos, SITTING_REACH) && FeederMeal.eat(animal, level, feeder, clock) && feeder.food() == 0) {
                    return;
                }
                continue;
            }
            if (EatAtFeederGoal.walks(animal) && !animal.isPanicking() && !EatAtFeederGoal.resting(animal, now)) {
                hungry.add(animal);
            }
        }
        // No more walking than there is food for, either.
        int room = Math.min(MAX_WALKING, feeder.food()) - walking;
        if (room <= 0 || hungry.isEmpty()) {
            return;
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        hungry.sort(Comparator.comparingDouble(animal -> animal.distanceToSqr(centre)));
        for (int i = 0; i < Math.min(room, hungry.size()); i++) {
            EatAtFeederGoal.send(hungry.get(i), pos, now);
            net.modcrafters.minecraft.barnyardromance.Constants.LOG.info("DBG send {} to {} at {}", hungry.get(i).getId(), pos, now);
        }
    }
}
