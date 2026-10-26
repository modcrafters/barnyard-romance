package net.modcrafters.minecraft.barnyardromance.food;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

// The held item as the food rules see it.
public record MinecraftItemFacts(Animal animal, ItemStack stack) implements ItemFacts {
    @Override
    public boolean matches(ItemMatch match) {
        Identifier id = Identifier.parse(match.id());
        if (match.tag()) {
            return stack.is(TagKey.create(Registries.ITEM, id));
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id);
    }

    @Override
    public boolean vanillaFood() {
        return animal.isFood(stack);
    }

    @Override
    public OptionalInt nutrition() {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? OptionalInt.empty() : OptionalInt.of(food.nutrition());
    }
}
