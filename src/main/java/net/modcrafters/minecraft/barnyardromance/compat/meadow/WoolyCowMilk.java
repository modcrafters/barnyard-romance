package net.modcrafters.minecraft.barnyardromance.compat.meadow;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.config.MilkEntry;
import net.modcrafters.minecraft.barnyardromance.core.config.MilkResult;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

// A wooly cow's wooden milk depends on its variant: the warped cow gives warped milk, the others plain wooden milk. The
// variant is the mod's enum, so it is read by name (getVariant, then getBucket on that) and the mod isn't compiled
// against. If the mod renamed either, this logs once and the entry's own result is used, as for a config entry.
// Into a pail, the plain cows give cow milk and the warped cow nothing: warped milk is only Meadow's wooden-bucket item,
// with no fluid behind it.
public final class WoolyCowMilk implements MilkResult {
    static final String WOODEN_BUCKET = "meadow:wooden_bucket";
    static final String PAILS = "#barnyard_romance:pails";
    private static final String PLAIN_WOODEN_MILK = "meadow:wooden_milk_bucket";

    private final AtomicBoolean warned = new AtomicBoolean();
    // The methods of the class last seen; a wooly cow is one class, so this is looked up once.
    private volatile Lookup lookup;

    private record Lookup(Class<?> type, Method variant, Method bucket) {
    }

    @Override
    public Optional<Item> resultFor(Animal animal, MilkEntry entry) {
        if (WOODEN_BUCKET.equals(entry.bucket())) {
            Optional<Item> bucket = variantBucket(animal);
            if (bucket.isPresent()) {
                return bucket;
            }
        }
        if (PAILS.equals(entry.bucket()) && variantBucket(animal).filter(WoolyCowMilk::isWarped).isPresent()) {
            return Optional.empty();
        }
        return own(entry);
    }

    // Any wooden milk but the plain one is the warped cow's.
    private static boolean isWarped(Item woodenMilk) {
        return !PLAIN_WOODEN_MILK.equals(BuiltInRegistries.ITEM.getKey(woodenMilk).toString());
    }

    private static Optional<Item> own(MilkEntry entry) {
        Identifier id = Identifier.tryParse(entry.result());
        return id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id).filter(item -> item != Items.AIR);
    }

    private Optional<Item> variantBucket(Animal animal) {
        try {
            Lookup found = lookup;
            if (found == null || found.type() != animal.getClass()) {
                Method variant = animal.getClass().getMethod("getVariant");
                Method bucket = variant.getReturnType().getMethod("getBucket");
                if (!Item.class.isAssignableFrom(bucket.getReturnType())) {
                    throw new NoSuchMethodException(bucket + " does not return an item");
                }
                found = new Lookup(animal.getClass(), variant, bucket);
                lookup = found;
            }
            Object variant = found.variant().invoke(animal);
            return Optional.ofNullable((Item) found.bucket().invoke(variant)).filter(item -> item != Items.AIR);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (warned.compareAndSet(false, true)) {
                Constants.LOG.warn("Can't read a wooly cow's milk bucket from its variant, so the wooden bucket gives plain wooden milk",
                        exception);
            }
            return Optional.empty();
        }
    }
}
