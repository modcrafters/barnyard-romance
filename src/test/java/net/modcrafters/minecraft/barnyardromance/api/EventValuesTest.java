package net.modcrafters.minecraft.barnyardromance.api;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// "Nothing" has one meaning, cancel(); an empty stack is refused rather than quietly meaning the same. The events start
// with no stack here, as a real one needs the game's registries.
class EventValuesTest {
    @Test
    void emptyResultIsRefused() {
        Milking milking = new Milking(null, null, null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> milking.setResult(ItemStack.EMPTY));

        assertEquals("use cancel() to give nothing", thrown.getMessage());
    }

    @Test
    void emptyDeathDropIsRefused() {
        DeathDrop drop = new DeathDrop(null, null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> drop.setStack(ItemStack.EMPTY));

        assertEquals("use cancel() to give nothing", thrown.getMessage());
    }

    @Test
    void emptyShearYieldIsRefused() {
        ShearYield yield = new ShearYield(null, null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> yield.setStack(ItemStack.EMPTY));

        assertEquals("use cancel() to give nothing", thrown.getMessage());
    }
}
