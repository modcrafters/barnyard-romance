package net.modcrafters.minecraft.barnyardromance.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

/**
 * Pails: reading what one holds and moving fluid in and out of it, by the same rules as the pail's screen and the
 * fluid adapters of each loader. Amounts are millilitres. A pail holds one fluid at a time. The fluids a pail takes
 * are the ones in the {@code barnyard_romance:pail_fluids} fluid tag, so a datapack or another mod adds one by
 * adding it to that tag.
 *
 * <p>Pails stack to one. A stack of more than one pail takes and gives nothing, so a caller moving fluid splits one
 * off first.
 */
public interface BarnyardPails {
    /**
     * Whether the stack is one of Barnyard Romance's pails.
     */
    boolean isPail(ItemStack stack);

    /**
     * How many millilitres the pail holds when full, or 0 when the stack isn't a pail.
     */
    int capacity(ItemStack stack);

    /**
     * The fluid in the pail. Empty when the pail is empty, when the stack isn't a pail, and when the pail holds a
     * fluid that isn't registered any more (its mod was removed); {@link #amount} still reports that fluid's
     * amount, and {@link #drain} still empties it.
     */
    Optional<Fluid> fluid(ItemStack stack);

    /**
     * How many millilitres the pail holds, 0 when it is empty or the stack isn't a pail.
     */
    int amount(ItemStack stack);

    /**
     * Puts fluid into the pail.
     *
     * @param pail        the pail, changed in place unless {@code simulate}
     * @param fluid       the fluid to put in; a fluid outside the {@code barnyard_romance:pail_fluids} tag, or one
     *                    other than what the pail already holds, is refused
     * @param millilitres the most to put in
     * @param simulate    true to only find out how much would go in
     * @return the millilitres taken, 0 when nothing went in, the pail isn't one, or the stack holds more than one
     */
    int fill(ItemStack pail, Fluid fluid, int millilitres, boolean simulate);

    /**
     * Takes fluid out of the pail, whatever it holds. That includes a fluid that has left the
     * {@code barnyard_romance:pail_fluids} tag or isn't registered any more, so such a pail can always be emptied.
     *
     * @param pail        the pail, changed in place unless {@code simulate}
     * @param millilitres the most to take out
     * @param simulate    true to only find out how much would come out
     * @return the millilitres taken out, 0 when nothing came out, the pail isn't one, or the stack holds more than
     *         one
     */
    int drain(ItemStack pail, int millilitres, boolean simulate);
}
