package net.modcrafters.minecraft.barnyardromance.familiarity;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

// What a familiar animal's hungry days cost it in health. The damage has no attacker, so nothing panics or turns on
// anyone, and it gets through armor and the hurt cooldown like old age does.
public final class HungerDamage {
    public static final ResourceKey<DamageType> HUNGER = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "hunger")
    );

    private HungerDamage() {
    }

    public static void hurt(Animal animal, ServerLevel level, float amount) {
        if (amount <= 0f) {
            return;
        }
        Optional<Holder.Reference<DamageType>> type = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(HUNGER);
        if (type.isEmpty()) {
            return;
        }
        // Any damage stands a sitting wolf up; being hungry shouldn't undo an order to stay.
        boolean sitting = animal instanceof TamableAnimal pet && pet.isOrderedToSit();
        //? if >=1.21.2 {
        animal.hurtServer(level, new DamageSource(type.get()), amount);
        //?} else
        //animal.hurt(new DamageSource(type.get()), amount);
        if (sitting && animal.isAlive()) {
            TamableAnimal pet = (TamableAnimal) animal;
            pet.setOrderedToSit(true);
            pet.setInSittingPose(true);
        }
    }
}
