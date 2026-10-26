package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.command.AnimalInfoText;
import net.modcrafters.minecraft.barnyardromance.command.DebugSteps;
import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.modcrafters.minecraft.barnyardromance.command.HerdToolRules;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorSheet;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class HerdStick extends Item {
    public HerdStick(Properties properties) {
        super(properties);
    }

    public static ItemStack create(HerdTool tool) {
        ItemStack stack = new ItemStack(BarnyardItems.HERD_STICK);
        stack.set(BarnyardComponents.HERD_TOOL, tool);
        stack.set(DataComponents.ITEM_NAME, Component.literal(tool.label()));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Use on an animal"))));
        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        HerdTool tool = stack.get(BarnyardComponents.HERD_TOOL);
        if (tool == null) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!HerdToolRules.allowed(tool, player.isCreative())) {
            player.sendSystemMessage(Component.literal(HerdToolRules.CREATIVE_ONLY));
            return InteractionResult.CONSUME;
        }
        Outcome outcome = apply(animal, tool, player);
        if (outcome.message != null) {
            player.sendSystemMessage(Component.literal(outcome.message));
        }
        if (outcome.applied && HerdToolRules.consumed(tool, player.isCreative())) {
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private record Outcome(boolean applied, String message) {
    }

    private static Outcome apply(Animal animal, HerdTool tool, Player player) {
        if (tool.kind() == HerdTool.Kind.INFO) {
            String text = AnimalInfoText.format(
                    animal.getName().getString(),
                    AnimalSex.speciesId(animal),
                    Services.DATA.get(animal),
                    AnimalSex.currentScale(animal),
                    AgeController.stage(animal),
                    Services.DATA.getPregnancy(animal).map(pregnancy -> PregnancyText.line(pregnancy, animal.level().getGameTime(), true)),
                    BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal)),
                    animal.level().getGameTime(),
                    Services.DATA.get(animal)
                            .flatMap(traits -> MilkActions.status(animal, traits, animal.level().getGameTime(), animal.level().getOverworldClockTime()))
                            .map(InspectorSheet::milkText)
                            .orElse("")
            );
            return new Outcome(true, text);
        }
        if (tool.kind() == HerdTool.Kind.FAMILIARITY) {
            AnimalTraits traits = Services.DATA.get(animal).orElse(null);
            if (traits == null) {
                return new Outcome(false, "This animal has no familiarity yet.");
            }
            double next = tool.sets()
                    ? DebugSteps.familiarityAt(tool.percent())
                    : DebugSteps.familiarity(traits.familiarity(), tool.direction());
            Services.DATA.set(animal, traits.fed(next, traits.lastFedTick()));
            FamiliarityActions.tameIfFamiliar(animal, player, next);
            return new Outcome(true, "Familiarity " + Familiarity.percent(traits.familiarity()) + " → " + Familiarity.percent(next) + ".");
        }
        Pregnancy current = Services.DATA.getPregnancy(animal).orElse(null);
        if (current == null) {
            return new Outcome(false, "This animal is not pregnant.");
        }
        long now = animal.level().getGameTime();
        Pregnancy next = tool.sets()
                ? DebugSteps.pregnancyAt(current, now, tool.percent())
                : DebugSteps.pregnancy(current, now, tool.direction());
        Services.DATA.setPregnancy(animal, next);
        return new Outcome(true, "Pregnancy " + progress(current, now) + " → " + progress(next, now) + ".");
    }

    private static String progress(Pregnancy pregnancy, long now) {
        int percent = (int) Math.round(PregnancyClock.progress(now, pregnancy) * 100.0);
        String time = PregnancyText.clock(PregnancyClock.remainingTicks(now, pregnancy));
        return percent + "%, " + ("due".equals(time) ? "due" : time + " left");
    }
}
