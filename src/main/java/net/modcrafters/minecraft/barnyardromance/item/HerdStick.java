package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.command.AnimalInfoText;
import net.modcrafters.minecraft.barnyardromance.command.DebugSteps;
import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.modcrafters.minecraft.barnyardromance.command.HerdToolRules;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.stats.StatText;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
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
        stack.set(DataComponents.ITEM_NAME, TextComponents.of(tool.label()));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.barnyard_romance.lore.use_on_animal"))));
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
        if (!BarnyardConfig.current().herdSticks()) {
            player.sendSystemMessage(Component.translatable("message.barnyard_romance.herd_stick.switched_off"));
            return InteractionResult.CONSUME;
        }
        return use(stack, tool, player, animal);
    }

    // The stick's work, below the config gate. Game tests call this so they never depend on the config.
    public static InteractionResult use(ItemStack stack, HerdTool tool, Player player, Animal animal) {
        if (!HerdToolRules.allowed(tool, player.isCreative())) {
            player.sendSystemMessage(TextComponents.of(HerdToolRules.CREATIVE_ONLY));
            return InteractionResult.CONSUME;
        }
        Outcome outcome = apply(animal, tool, player);
        if (outcome.message != null) {
            player.sendSystemMessage(TextComponents.of(outcome.message));
        }
        if (outcome.applied && HerdToolRules.consumed(tool, player.isCreative())) {
            stack.consume(1, player);
        }
        return VersionCompat.SUCCESS_SERVER;
    }

    private record Outcome(boolean applied, Text message) {
    }

    private static Outcome apply(Animal animal, HerdTool tool, Player player) {
        if (tool.kind() == HerdTool.Kind.INFO) {
            Text text = AnimalInfoText.format(
                    animal.getName().getString(),
                    AnimalSex.speciesId(animal),
                    Services.DATA.get(animal),
                    AnimalSex.currentScale(animal),
                    AgeController.stage(animal),
                    Services.DATA.get(animal, PregnancyFeature.PREGNANCY).map(pregnancy -> PregnancyText.line(pregnancy, animal.level().getGameTime(), true)),
                    BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal)),
                    animal.level().getGameTime(),
                    Services.DATA.get(animal)
                            .flatMap(traits -> MilkActions.status(animal, traits, animal.level().getGameTime(), animal.level().getOverworldClockTime()))
                            .map(StatText::milkText)
                            .orElse(Text.EMPTY)
            );
            return new Outcome(true, text);
        }
        if (tool.kind() == HerdTool.Kind.FAMILIARITY) {
            AnimalTraits traits = Services.DATA.get(animal).orElse(null);
            if (traits == null) {
                return new Outcome(false, Text.of("message.barnyard_romance.herd_stick.no_familiarity"));
            }
            double next = tool.sets()
                    ? DebugSteps.familiarityAt(tool.percent())
                    : DebugSteps.familiarity(traits.get(FamiliaritySlices.CARE).familiarity(), tool.direction());
            FamiliarityState care = traits.get(FamiliaritySlices.CARE);
            // Going up obeys the wild-caught cap, as feeding would.
            AnimalTraits updated = next > care.familiarity()
                    ? FamiliarityCare.raise(animal, traits, next, care.lastFedTick())
                    : traits.with(FamiliaritySlices.CARE, care.withFamiliarity(next));
            Services.DATA.set(animal, updated);
            double familiarity = updated.get(FamiliaritySlices.CARE).familiarity();
            FamiliarityActions.tameIfFamiliar(animal, player, familiarity);
            return new Outcome(true, Text.of("message.barnyard_romance.herd_stick.familiarity", Familiarity.percent(care.familiarity()),
                    Familiarity.percent(familiarity)));
        }
        Pregnancy current = Services.DATA.get(animal, PregnancyFeature.PREGNANCY).orElse(null);
        if (current == null) {
            return new Outcome(false, Text.of("message.barnyard_romance.herd_stick.not_pregnant"));
        }
        long now = animal.level().getGameTime();
        Pregnancy next = tool.sets()
                ? DebugSteps.pregnancyAt(current, now, tool.percent())
                : DebugSteps.pregnancy(current, now, tool.direction());
        Services.DATA.set(animal, PregnancyFeature.PREGNANCY, next);
        return new Outcome(true, Text.of("message.barnyard_romance.herd_stick.pregnancy", progress(current, now), progress(next, now)));
    }

    private static Text progress(Pregnancy pregnancy, long now) {
        int percent = (int) Math.round(PregnancyClock.progress(now, pregnancy) * 100.0);
        long remaining = PregnancyClock.remainingTicks(now, pregnancy);
        if (remaining <= 0L) {
            return Text.of("message.barnyard_romance.herd_stick.progress_due", percent);
        }
        return Text.of("message.barnyard_romance.herd_stick.progress", percent, PregnancyText.clock(remaining));
    }
}
