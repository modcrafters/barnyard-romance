package net.modcrafters.minecraft.barnyardromance.herd;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.modcrafters.minecraft.barnyardromance.eggs.Laying;

// What a herd book record keeps for the full stat sheet beyond the animal's traits: its health, whether it only breeds
// once tamed, and the eggs it laid on the day it was last seen. A maxHealth of 0 means nothing was recorded (a record
// from before the sheet), so the book shows no Health row.
public record RecordSheet(float health, float maxHealth, boolean needsTaming, Laying laying) {
    public static final RecordSheet NONE = new RecordSheet(0.0F, 0.0F, false, Laying.NONE);
    public static final Codec<RecordSheet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("health", 0.0F).forGetter(RecordSheet::health),
            Codec.FLOAT.optionalFieldOf("maxHealth", 0.0F).forGetter(RecordSheet::maxHealth),
            Codec.BOOL.optionalFieldOf("needsTaming", false).forGetter(RecordSheet::needsTaming),
            Laying.CODEC.optionalFieldOf("laying", Laying.NONE).forGetter(RecordSheet::laying)
    ).apply(instance, RecordSheet::new));
}
