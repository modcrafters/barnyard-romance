package net.modcrafters.minecraft.barnyardromance.food;

// What happens when an animal is offered an item.
public sealed interface Feed {
    Feed REFUSED = new Refused();
    Feed NOT_FOOD = new NotFood();

    // The animal turns away and the item is kept.
    record Refused() implements Feed {
    }

    record NotFood() implements Feed {
    }

    // `factor` scales the default effects; a food file's own effects use 1.
    record Eaten(FoodEffects effects, double factor, boolean superfood) implements Feed {
    }
}
