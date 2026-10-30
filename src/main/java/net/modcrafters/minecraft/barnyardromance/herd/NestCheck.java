package net.modcrafters.minecraft.barnyardromance.herd;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.modcrafters.minecraft.barnyardromance.eggs.RoosterNear;

// What was around a hen when the herd book last saw her: the best rooster in range, and whether a nesting box in
// range had room. NONE for every other animal.
public record NestCheck(RoosterNear rooster, boolean boxWithRoom) {
    public static final NestCheck NONE = new NestCheck(RoosterNear.NONE, false);
    private static final Codec<RoosterNear> ROOSTER_CODEC = Codec.STRING.xmap(
            name -> {
                try {
                    return RoosterNear.valueOf(name);
                } catch (IllegalArgumentException unknown) {
                    return RoosterNear.NONE;
                }
            },
            RoosterNear::name);
    public static final Codec<NestCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ROOSTER_CODEC.optionalFieldOf("rooster", RoosterNear.NONE).forGetter(NestCheck::rooster),
            Codec.BOOL.optionalFieldOf("boxWithRoom", false).forGetter(NestCheck::boxWithRoom)
    ).apply(instance, NestCheck::new));
}
