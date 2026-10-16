package net.modcrafters.minecraft.barnyardromance.command;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class BarnyardCommands {
    private static final double REACH = 8.0;

    private BarnyardCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("barnyard")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("info").executes(context -> info(context.getSource()))));
    }

    private static int info(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Animal animal = lookAtAnimal(player);
        if (animal == null) {
            source.sendFailure(Component.literal("You are not looking at an animal."));
            return 0;
        }
        String text = AnimalInfoText.format(
                animal.getName().getString(),
                AnimalSex.speciesId(animal),
                Services.DATA.get(animal),
                AnimalSex.currentScale(animal),
                animal.isBaby(),
                Services.DATA.getPregnancy(animal).map(pregnancy -> PregnancyText.line(pregnancy, animal.level().getGameTime(), true)),
                BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal))
        );
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static Animal lookAtAnimal(ServerPlayer player) {
        Vec3 from = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 to = from.add(look.scale(REACH));
        AABB box = player.getBoundingBox().expandTowards(look.scale(REACH)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                from,
                to,
                box,
                entity -> entity instanceof Animal && !entity.isSpectator(),
                REACH * REACH
        );
        if (hit != null && hit.getEntity() instanceof Animal animal) {
            return animal;
        }
        return null;
    }
}
