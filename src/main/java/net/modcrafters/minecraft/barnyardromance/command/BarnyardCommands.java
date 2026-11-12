package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.herd.HerdToolRules;
import net.modcrafters.minecraft.barnyardromance.herd.HerdTool;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.food.FoodScanRunner;
import net.modcrafters.minecraft.barnyardromance.herd.HerdStick;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//? if >=26.1
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

public final class BarnyardCommands {
    private BarnyardCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // The stick commands exist only while herdSticks is on, read when the server starts.
        register(dispatcher, BarnyardConfig.current().herdSticks());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean herdSticks) {
        LiteralArgumentBuilder<CommandSourceStack> barnyard = Commands.literal("barnyard")
                //? if >=1.21.11 {
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                //?} else
                //.requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("foods").then(Commands.literal("scan").executes(context -> scan(context.getSource()))));
        if (herdSticks) {
            barnyard.then(Commands.literal("familiarity")
                        .then(Commands.literal("up").executes(context -> give(context.getSource(), HerdTool.familiarity(1))))
                        .then(Commands.literal("down").executes(context -> give(context.getSource(), HerdTool.familiarity(-1))))
                        .then(Commands.literal("set").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(context -> give(context.getSource(), HerdTool.familiaritySet(IntegerArgumentType.getInteger(context, "percent")))))))
                .then(Commands.literal("pregnancy")
                        .then(Commands.literal("up").executes(context -> give(context.getSource(), HerdTool.pregnancy(1))))
                        .then(Commands.literal("down").executes(context -> give(context.getSource(), HerdTool.pregnancy(-1))))
                        .then(Commands.literal("set").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(context -> give(context.getSource(), HerdTool.pregnancySet(IntegerArgumentType.getInteger(context, "percent")))))));
        }
        dispatcher.register(barnyard);
    }

    private static int scan(CommandSourceStack source) {
        try {
            FoodScanRunner.ScanSummary summary = FoodScanRunner.run(source.getServer(), FoodScanRunner.liveGroups(), FoodScanRunner.defaultFolder(source.getServer()));
            source.sendSuccess(() -> TextComponents.of(FoodScanText.summary(summary.recipes(), summary.items(), summary.tags(), summary.folder().toString(), summary.skippedByType())), false);
            return summary.items();
        } catch (IOException | RuntimeException exception) {
            Constants.LOG.error("The food scan failed", exception);
            source.sendFailure(Component.translatable("commands.barnyard_romance.foods.scan.failed", String.valueOf(exception.getMessage())));
            return 0;
        }
    }

    private static int give(CommandSourceStack source, HerdTool tool) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!HerdToolRules.allowed(tool, player.isCreative())) {
            source.sendFailure(TextComponents.of(HerdToolRules.CREATIVE_ONLY));
            return 0;
        }
        give(player, HerdStick.create(tool));
        source.sendSuccess(() -> Component.translatable("commands.barnyard_romance.herd_stick.given", TextComponents.of(tool.label())), false);
        return 1;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            //? if >=26.1 {
            player.drop(stack, false, Prediction.SERVER_ONLY);
            //?} else
            //player.drop(stack, false);
        }
    }
}
