package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClasses;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.config.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.core.data.ItemData;
import net.modcrafters.minecraft.barnyardromance.core.text.Names;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//? if >=1.21.5
import java.util.function.Consumer;

// An egg laid in a nesting box while a fed rooster was near. While it stays in the box it remembers both parents for
// the chick (EggData), so no two are alike and it never stacks. Taken out it goes wild (WildEggs): it forgets them and
// stacks with other wild eggs of its kind. Thrown, either sometimes hatches like a vanilla egg, into a poor chick.
public class FertilizedEggItem extends EggItem {
    public FertilizedEggItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(EggData data) {
        ItemStack stack = new ItemStack(EggsItems.FERTILIZED_EGG);
        ItemData.set(stack, EggsComponents.EGG_DATA, data);
        return stack;
    }

    public static Optional<EggData> data(ItemStack stack) {
        return Optional.ofNullable(ItemData.get(stack, EggsComponents.EGG_DATA));
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        lines(stack).forEach(line -> builder.accept(TextComponents.of(line).copy().withStyle(ChatFormatting.GRAY)));
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines(stack).forEach(line -> lines.add(TextComponents.of(line).copy().withStyle(ChatFormatting.GRAY)));
    }
    *///?}

    // Whose egg it is, with its mod when another animal has the same name. Then for a box egg its time, its parents and
    // what taking it out does; for a wild egg its time while in a box and that its parents are unknown.
    public static List<Text> lines(ItemStack stack) {
        EggData data = ItemData.get(stack, EggsComponents.EGG_DATA);
        WildEgg wild = ItemData.get(stack, EggsComponents.WILD_EGG);
        if (data == null && wild == null) {
            return List.of();
        }
        List<Text> lines = new ArrayList<>();
        String speciesId = data != null ? data.species() : wild.species();
        Text species = Names.entity(speciesId);
        if (!species.isEmpty()) {
            lines.add(Names.sharedNameMod(speciesId)
                    .map(mod -> Text.of("item.barnyard_romance.fertilized_egg.species_from", species, mod))
                    .orElse(Text.of("item.barnyard_romance.fertilized_egg.species", species)));
        }
        if (data != null) {
            SizeSettings size = SizeRanges.orCurrent(data.sizeRange(), data.species());
            lines.add(Text.of("item.barnyard_romance.fertilized_egg.time", Growth.clock(data.remainingTicks())));
            lines.add(Text.of(
                    "item.barnyard_romance.fertilized_egg.parents",
                    SizeClasses.of(Sex.FEMALE, data.motherScale(), size).name(),
                    SizeClasses.of(Sex.MALE, data.fatherScale(), size).name()
            ));
            lines.add(Text.of("item.barnyard_romance.fertilized_egg.taking_out"));
            return lines;
        }
        WildEggs.remainingTicks(stack).ifPresent(ticks -> lines.add(Text.of("item.barnyard_romance.fertilized_egg.time", Growth.clock(ticks))));
        lines.add(Text.of("item.barnyard_romance.fertilized_egg.parents_unknown"));
        return lines;
    }

    //? if <1.20.5 {
    /*// 1.20.1 has no name or stack size components. Forge asks the item for a stack's size (IForgeItem), so this
    // overrides that, and a wild egg stacks as it does on the newer versions.
    @Override
    public Component getName(ItemStack stack) {
        return WildEggs.wild(stack) ? Component.translatable("item.barnyard_romance.fertilized_egg.wild") : super.getName(stack);
    }

    public int getMaxStackSize(ItemStack stack) {
        return WildEggs.wild(stack) ? WildEggs.STACK : getMaxStackSize();
    }
    *///?}
}
