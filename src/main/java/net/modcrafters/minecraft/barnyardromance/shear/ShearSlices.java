package net.modcrafters.minecraft.barnyardromance.shear;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSlice;

// Shearing's field in the animal record, saved as lastShearedClock as it always has been.
public final class ShearSlices {
    // The overworld clock time of the last shearing, or -1 for never; shearing is once a day, like feeding.
    public static final AnimalSlice<Long> LAST_SHEARED = new AnimalSlice<>("last_sheared", -1L,
            Codec.LONG.optionalFieldOf("lastShearedClock", -1L), ByteBufCodecs.VAR_LONG);

    private ShearSlices() {
    }
}
