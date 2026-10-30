package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Optional;
//? if >=1.21.5
import java.util.function.Consumer;

// An egg laid in a nesting box while a fed rooster was near. It remembers both parents for the chick (EggData), so
// no two are alike and it never stacks. Thrown, it sometimes hatches like a vanilla egg, into a poor chick.
public class FertilizedEggItem extends EggItem {
    public FertilizedEggItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(EggData data) {
        ItemStack stack = new ItemStack(BarnyardItems.FERTILIZED_EGG);
        stack.set(BarnyardComponents.EGG_DATA, data);
        return stack;
    }

    public static Optional<EggData> data(ItemStack stack) {
        return Optional.ofNullable(stack.get(BarnyardComponents.EGG_DATA));
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        data(stack).ifPresent(data -> lines(data).forEach(line -> builder.accept(TextComponents.of(line).copy().withStyle(ChatFormatting.GRAY))));
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        data(stack).ifPresent(data -> lines(data).forEach(line -> lines.add(TextComponents.of(line).copy().withStyle(ChatFormatting.GRAY))));
    }
    *///?}

    // In a box the window shows the countdown under the egg, so only a paused egg says how long it has left.
    private static List<Text> lines(EggData data) {
        SizeSettings size = BarnyardConfig.current().sizeFor(data.species());
        Text parents = Text.of(
                "item.barnyard_romance.fertilized_egg.parents",
                SizeClass.of(Sex.FEMALE, data.motherScale(), size).name(),
                SizeClass.of(Sex.MALE, data.fatherScale(), size).name()
        );
        if (data.inBox()) {
            return List.of(parents);
        }
        return List.of(Text.of("item.barnyard_romance.fertilized_egg.time", Growth.clock(data.remainingTicks())), parents);
    }
}
