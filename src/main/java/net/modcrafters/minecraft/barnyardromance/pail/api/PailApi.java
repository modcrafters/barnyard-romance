package net.modcrafters.minecraft.barnyardromance.pail.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// What other features ask of pails. A milking into a pail adds one bucket's worth, 1000 ml, of the milk bucket's fluid.
public interface PailApi {
    // Whether the stack is a pail, which fills where it is rather than turning into a milk bucket.
    boolean isPail(ItemStack stack);

    // Whether a milking that gives this milk bucket fits in the pail: the bucket holds a pail fluid, the pail holds
    // nothing else, and it has 1000 ml free.
    boolean fits(ItemStack pail, Item result);

    // What to tell the player when the milking doesn't fit: the pail is full, or it holds another milk. Empty when it
    // fits, or when the milk has no fluid a pail takes, which says nothing, as a bucket a species doesn't use.
    Optional<Component> refusal(ItemStack pail, Item result);

    // Pours one milking of this milk bucket's fluid into the pail, in place. False, with nothing changed, if it doesn't fit.
    boolean fill(ItemStack pail, Item result);
}
