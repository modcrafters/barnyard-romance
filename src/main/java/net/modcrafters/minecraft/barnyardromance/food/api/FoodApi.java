package net.modcrafters.minecraft.barnyardromance.food.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// What other features ask about food: the diets and food files from the loaded datapacks.
public interface FoodApi {
    // What happens when the animal is offered the item.
    Feed offered(Animal animal, ItemStack stack);

    // Whether the animal eats the item.
    boolean eats(Animal animal, ItemStack stack);

    // How much a meal of the item heals the animal, as the food's offer worked it out: a food file's heal, else part of
    // what a player would get from it, else the default.
    float heals(Animal animal, ItemStack stack, Feed feed);

    // What the species eats, best first, as "#tag" and item ids, for the herd book; superfoods are left out. Empty for
    // a species that isn't tracked.
    List<String> menu(ServerLevel level, String speciesId);
}
