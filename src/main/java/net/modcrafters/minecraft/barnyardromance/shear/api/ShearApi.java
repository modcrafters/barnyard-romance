package net.modcrafters.minecraft.barnyardromance.shear.api;

import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;

// What other features ask about shearing.
public interface ShearApi {
    // Whether the herd book and the card show the animal as ready to shear. shearable is whether its kind is sheared at
    // all; woolly is false once a sheep is sheared, until it eats grass. clock is an overworld clock time.
    ShearState shearState(boolean shearable, boolean woolly, boolean baby, AnimalTraits traits, long clock);
}
