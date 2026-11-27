package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.core.animal.SizeRanges;
import net.modcrafters.minecraft.barnyardromance.core.data.ItemData;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.stats.api.StatsApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.milk.api.MilkApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.20.5 {
import net.minecraft.world.item.component.ItemLore;
//?}
//? if <1.20.5 {
/*import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HerdStick extends Item {
    public HerdStick(Properties properties) {
        super(properties);
    }

    //? if <1.20.5 {
    /*@Override
    public Component getName(ItemStack stack) {
        HerdTool tool = ItemData.get(stack, HerdComponents.HERD_TOOL);
        return tool == null ? super.getName(stack) : TextComponents.of(tool.label());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (ItemData.has(stack, HerdComponents.HERD_TOOL)) {
            lines.add(Component.translatable("item.barnyard_romance.lore.use_on_animal").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
    *///?}

    public static ItemStack create(HerdTool tool) {
        ItemStack stack = new ItemStack(HerdItems.HERD_STICK);
        ItemData.set(stack, HerdComponents.HERD_TOOL, tool);
        // 1.20.1 has no name or lore components: the stick shows them from its tool (getName, appendHoverText).
        //? if >=1.20.5 {
        stack.set(DataComponents.ITEM_NAME, TextComponents.of(tool.label()));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.barnyard_romance.lore.use_on_animal"))));
        //?}
        return stack;
    }

    // The sticks the creative tab shows, in its order: there only while the config's herdSticks is on.
    public static List<ItemStack> tabStacks(boolean herdSticks) {
        List<ItemStack> sticks = new ArrayList<>();
        if (herdSticks) {
            sticks.add(create(HerdTool.info()));
            sticks.add(create(HerdTool.familiarity(1)));
            sticks.add(create(HerdTool.familiarity(-1)));
            sticks.add(create(HerdTool.pregnancy(1)));
            sticks.add(create(HerdTool.pregnancy(-1)));
        }
        return sticks;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        HerdTool tool = ItemData.get(stack, HerdComponents.HERD_TOOL);
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
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        if (tool.kind() == HerdTool.Kind.INFO) {
            String species = AnimalSex.speciesId(animal);
            Optional<AnimalTraits> traits = Services.DATA.get(animal);
            Text text = AnimalInfoText.format(
                    animal.getName().getString(),
                    species,
                    traits,
                    AnimalSex.currentScale(animal),
                    AgeController.stage(animal),
                    pregnancies.pregnancy(animal).map(pregnancy -> pregnancies.progressLine(pregnancy, animal.level().getGameTime(), true)),
                    traits.map(value -> SizeRanges.rangeOf(value, species)).orElseGet(() -> BarnyardConfig.current().sizeFor(species)),
                    animal.level().getGameTime(),
                    traits.flatMap(value -> FeaturesRepository.get(MilkApi.class)
                                    .status(animal, value, animal.level().getGameTime(), animal.level().getOverworldClockTime()))
                            .map(status -> FeaturesRepository.get(StatsApi.class).milkText(status))
                            .orElse(Text.EMPTY)
            );
            return new Outcome(true, text);
        }
        if (tool.kind() == HerdTool.Kind.FAMILIARITY) {
            // Only a tracked animal: an excluded species is left alone, and fires no familiarity or taming events.
            AnimalTraits traits = Tracked.traits(animal).orElse(null);
            if (traits == null) {
                return new Outcome(false, Text.of("message.barnyard_romance.herd_stick.no_familiarity"));
            }
            FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
            double before = familiarityApi.familiarity(traits);
            double next = tool.sets()
                    ? DebugSteps.familiarityAt(tool.percent())
                    : DebugSteps.familiarity(before, tool.direction());
            AnimalTraits updated = familiarityApi.withFamiliaritySetTo(animal, traits, next);
            Services.DATA.set(animal, updated);
            double familiarity = familiarityApi.familiarity(updated);
            familiarityApi.tameIfFamiliar(animal, player, familiarity);
            return new Outcome(true, Text.of("message.barnyard_romance.herd_stick.familiarity", Text.percent(before),
                    Text.percent(familiarity)));
        }
        Pregnancy current = pregnancies.pregnancy(animal).orElse(null);
        if (current == null) {
            return new Outcome(false, Text.of("message.barnyard_romance.herd_stick.not_pregnant"));
        }
        long now = animal.level().getGameTime();
        Pregnancy next = tool.sets()
                ? DebugSteps.pregnancyAt(current, now, tool.percent())
                : DebugSteps.pregnancy(current, now, tool.direction());
        pregnancies.setPregnancy(animal, next);
        return new Outcome(true, Text.of("message.barnyard_romance.herd_stick.pregnancy", progress(current, now), progress(next, now)));
    }

    private static Text progress(Pregnancy pregnancy, long now) {
        PregnancyApi pregnancies = FeaturesRepository.get(PregnancyApi.class);
        int percent = (int) Math.round(pregnancies.progress(pregnancy, now) * 100.0);
        long remaining = pregnancies.ticksUntilDue(pregnancy, now);
        if (remaining <= 0L) {
            return Text.of("message.barnyard_romance.herd_stick.progress_due", percent);
        }
        return Text.of("message.barnyard_romance.herd_stick.progress", percent, pregnancies.dueClock(remaining));
    }
}
