package net.modcrafters.minecraft.barnyardromance.food;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

// The held item as the food rules see it.
public record MinecraftItemFacts(Animal animal, ItemStack stack) implements ItemFacts {
    @Override
    public boolean matches(ItemMatch match) {
        return matches(stack, match);
    }

    // Each match is checked whenever an animal looks at what a player holds, so its id or tag is worked out once.
    // Tag contents are still read from the stack each time, so a datapack reload changes what a tag holds.
    private static final Map<ItemMatch, Predicate<ItemStack>> MATCHERS = new ConcurrentHashMap<>();

    public static boolean matches(ItemStack stack, ItemMatch match) {
        return MATCHERS.computeIfAbsent(match, MinecraftItemFacts::matcher).test(stack);
    }

    private static Predicate<ItemStack> matcher(ItemMatch match) {
        Identifier id = Identifier.parse(match.id());
        if (match.tag()) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
            return stack -> stack.is(tag);
        }
        return stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id);
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
