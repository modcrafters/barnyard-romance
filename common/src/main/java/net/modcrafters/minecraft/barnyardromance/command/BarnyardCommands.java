package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.item.BarnyardInspector;
import net.modcrafters.minecraft.barnyardromance.item.HerdStick;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

public final class BarnyardCommands {
    private BarnyardCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("barnyard")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("info").executes(context -> inspector(context.getSource())))
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
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }
}
