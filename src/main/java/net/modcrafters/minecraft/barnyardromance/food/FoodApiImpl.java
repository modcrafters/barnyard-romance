package net.modcrafters.minecraft.barnyardromance.food;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodApi;

import java.util.Optional;

final class FoodApiImpl implements FoodApi {
    @Override
    public Feed offered(Animal animal, ItemStack stack) {
        return FoodData.resolve(animal, stack);
    }

    @Override
    public boolean eats(Animal animal, ItemStack stack) {
        return FoodData.eats(animal, stack);
    }

    // Something the animal's kind doesn't eat (a feeding block may serve it) heals the default, at a factor of 1.
    @Override
    public float heals(Animal animal, ItemStack stack, Feed feed) {
        return feed instanceof Feed.Eaten eaten
                ? FoodHeal.amount(eaten.effects().heal(), new MinecraftItemFacts(animal, stack).nutrition(), eaten.factor(), BarnyardConfig.current().tuning())
                : FoodHeal.amount(Optional.empty(), new MinecraftItemFacts(animal, stack).nutrition(), 1.0, BarnyardConfig.current().tuning());
    }
}
