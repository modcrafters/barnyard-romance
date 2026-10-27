package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.food.FoodScanRunner;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardInspector;
import net.modcrafters.minecraft.barnyardromance.item.HerdStick;
import com.mojang.brigadier.CommandDispatcher;
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
        dispatcher.register(Commands.literal("barnyard")
                //? if >=1.21.11 {
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                //?} else
                //.requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("info").executes(context -> inspector(context.getSource())))
                .then(Commands.literal("foods").then(Commands.literal("scan").executes(context -> scan(context.getSource()))))
                .then(Commands.literal("familiarity")
                        .then(Commands.literal("up").executes(context -> give(context.getSource(), HerdTool.familiarity(1))))
                        .then(Commands.literal("down").executes(context -> give(context.getSource(), HerdTool.familiarity(-1))))
                        .then(Commands.literal("set").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(context -> give(context.getSource(), HerdTool.familiaritySet(IntegerArgumentType.getInteger(context, "percent")))))))
                .then(Commands.literal("pregnancy")
                        .then(Commands.literal("up").executes(context -> give(context.getSource(), HerdTool.pregnancy(1))))
                        .then(Commands.literal("down").executes(context -> give(context.getSource(), HerdTool.pregnancy(-1))))
                        .then(Commands.literal("set").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(context -> give(context.getSource(), HerdTool.pregnancySet(IntegerArgumentType.getInteger(context, "percent"))))))));
    }

    private static int scan(CommandSourceStack source) {
        try {
            FoodScanRunner.ScanSummary summary = FoodScanRunner.run(source.getServer(), FoodScanRunner.liveGroups(), FoodScanRunner.defaultFolder(source.getServer()));
            source.sendSuccess(() -> Component.literal(FoodScanText.summary(summary.recipes(), summary.items(), summary.tags(), summary.folder().toString(), summary.skippedByType())), false);
            return summary.items();
        } catch (IOException | RuntimeException exception) {
            Constants.LOG.error("The food scan failed", exception);
            source.sendFailure(Component.literal("The food scan failed: " + exception.getMessage()));
            return 0;
        }
    }

    private static int inspector(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        give(player, BarnyardInspector.create());
        source.sendSuccess(() -> Component.literal("Use the Barnyard Inspector on an animal."), false);
        return 1;
    }

    private static int give(CommandSourceStack source, HerdTool tool) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!HerdToolRules.allowed(tool, player.isCreative())) {
            source.sendFailure(Component.literal(HerdToolRules.CREATIVE_ONLY));
            return 0;
        }
        give(player, HerdStick.create(tool));
        source.sendSuccess(() -> Component.literal("Use the " + tool.label() + " stick on an animal."), false);
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
