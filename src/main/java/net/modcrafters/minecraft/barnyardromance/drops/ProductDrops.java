package net.modcrafters.minecraft.barnyardromance.drops;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// Sizes one dropped stack, on death or from shearing: meat by its own curve, sized_drops by vanilla × size; both × sex × age.
public final class ProductDrops {
    public static final TagKey<Item> SIZED_DROPS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sized_drops"));

    private ProductDrops() {
    }

    public static ItemStack resize(Animal animal, ItemStack stack, RandomSource random) {
        BarnyardConfig config = BarnyardConfig.current();
        String speciesId = AnimalSex.speciesId(animal);
        Optional<AnimalTraits> tracked = config.speciesEnabled(speciesId) ? Services.DATA.get(animal) : Optional.empty();
        if (tracked.isEmpty() || stack.isEmpty()) {
            return stack;
        }
        AnimalTraits traits = tracked.get();
        double sexAge = Yield.sexAge(traits.sex(), AgeController.stage(animal, traits) == AgeStage.OLD, config.tuning());
        String itemId = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (MeatDrop.isMeat(itemId)) {
            SizeSettings range = config.defaultSize();
            stack.setCount(MeatDrop.resizedCount(
                    itemId,
                    stack.getCount(),
                    traits.scale(),
                    range.smallest(),
                    range.largest(),
                    sexAge,
                    random.nextDouble(),
                    random.nextDouble()
            ));
        } else if (stack.is(SIZED_DROPS)) {
            SizeSettings size = config.sizeFor(speciesId);
            double base = traits.sex() == Sex.FEMALE ? size.femaleScale() : size.maleScale();
            double amount = stack.getCount() * Yield.size(traits.scale(), base, config.tuning().yieldScaleExponent()) * sexAge;
            stack.setCount(Yield.round(amount, random.nextDouble()));
        }
        return stack;
    }
}
