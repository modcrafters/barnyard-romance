package net.modcrafters.minecraft.barnyardromance.core.config;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// One way to milk a species: holding the item bucket gives the item result (both item ids), with the sound id if
// there is one. The bucket may instead be # and an item tag, such as #barnyard_romance:pails.
public record MilkEntry(String bucket, String result, @Nullable String sound) {
    static final String TAG_PREFIX = "#";

    // Whether the bucket is # and an item tag the held item is in.
    public boolean namesTagOf(ItemStack held) {
        return inTag(bucket, held);
    }

    // Whether this milk list bucket is # and an item tag the held item is in. A tag id that doesn't parse holds nothing.
    static boolean inTag(String bucket, ItemStack held) {
        if (!bucket.startsWith(TAG_PREFIX)) {
            return false;
        }
        Identifier id = Identifier.tryParse(bucket.substring(TAG_PREFIX.length()));
        return id != null && held.is(TagKey.create(Registries.ITEM, id));
    }
}
