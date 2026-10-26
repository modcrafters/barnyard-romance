package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.client.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.InspectorScreen;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.UUID;
import java.util.function.UnaryOperator;

// Opens the Barnyard Inspector on a few prepared animals in a real client and screenshots each one.
public class InspectorClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            // A new world starts at tick 0, too early for anything to have been born a month ago.
            world.getServer().runOnServer(server -> {
                ((ServerLevelData) server.overworld().getLevelData()).setGameTime(1_000_000L);
                server.forceGameTimeSynchronization();
            });
            world.getConnection().waitForClientboundPackets();
            long now = world.getServer().computeOnServer(server -> server.overworld().getGameTime());

            inspect(context, world, "inspector_worn_out_heavy_cow", Sex.FEMALE, false,
                    traits -> traits.withWeight(0.8).withFertility(0.1, now), false);
            inspect(context, world, "inspector_light_bull", Sex.MALE, false,
                    traits -> traits.withWeight(0.3), false);
            inspect(context, world, "inspector_pregnant_cow", Sex.FEMALE, false,
                    traits -> traits.withWeight(0.55).withFertility(0.75, now), true);
            inspect(context, world, "inspector_calf", Sex.FEMALE, true,
                    traits -> traits.withWeight(0.62), false);
            inspect(context, world, "inspector_strong_stallion", EntityTypes.HORSE, Sex.MALE, false,
                    traits -> traits.withWeight(0.7), false);
            inspect(context, world, "inspector_young_bull", Sex.MALE, false,
                    traits -> traits.withBornTick(now - 30_000L), false);
            inspect(context, world, "inspector_old_cow", Sex.FEMALE, false,
                    traits -> traits.withBornTick(now - 800_000L).withFertility(0.3, now), false);
            oldHearts(context, world, now);
            superfoods(context, world);
        }
    }

    // The superfood textures in the hotbar, to catch a missing texture.
    private static void superfoods(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            player.getInventory().setItem(0, new ItemStack(BarnyardItems.BREEDERS_MASH, 2));
            player.getInventory().setItem(1, new ItemStack(BarnyardItems.GOLDEN_BRAN, 2));
        });
        world.getConnection().waitForClientboundPackets();
        context.waitTicks(5);
        context.takeScreenshot("superfoods_hotbar");
    }

    // Old hearts are random and rare, so a few are spawned by hand to see how they look.
    private static void oldHearts(ClientGameTestContext context, TestSingleplayerContext world, long now) {
        world.getServer().runCommand("tp @a ~ ~ ~ -75 5");
        UUID id = world.getServer().computeOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            Animal cow = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
            cow.snapTo(player.getX() + 5.0, player.getY(), player.getZ(), 90.0F, 0.0F);
            cow.setNoAi(true);
            player.level().addFreshEntity(cow);
            Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, 0.85, 1.0, -1L).withBornTick(now - 800_000L));
            return cow.getUUID();
        });
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.level != null && entity(client.level.getEntity(id)) != null);
        for (int i = 0; i < 4; i++) {
            context.runOnClient(client -> {
                Entity cow = client.level.getEntity(id);
                client.level.addParticle(BarnyardParticles.OLD_HEART, cow.getRandomX(0.6), cow.getRandomY() + 0.5, cow.getRandomZ(0.6), 0.0, 0.0, 0.0);
            });
            context.waitTicks(4);
        }
        context.takeScreenshot("old_cow_hearts");
        world.getServer().runOnServer(server -> {
            Entity cow = server.overworld().getEntity(id);
            if (cow != null) {
                cow.discard();
            }
        });
    }

    private static void inspect(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            String name,
            Sex sex,
            boolean baby,
            UnaryOperator<AnimalTraits> change,
            boolean pregnant
    ) {
        inspect(context, world, name, EntityTypes.COW, sex, baby, change, pregnant);
    }

    private static void inspect(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            String name,
            EntityType<? extends Animal> type,
            Sex sex,
            boolean baby,
            UnaryOperator<AnimalTraits> change,
            boolean pregnant
    ) {
        UUID id = world.getServer().computeOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            ServerLevel level = player.level();
            Animal cow = type.create(level, EntitySpawnReason.COMMAND);
            AttributeInstance maxHealth = cow.getAttribute(Attributes.MAX_HEALTH);
            if (type == EntityTypes.HORSE && maxHealth != null) {
                maxHealth.setBaseValue(30.0);
                cow.setHealth(30.0F);
            }
            cow.snapTo(player.getX() + 3.0, player.getY(), player.getZ(), 90.0F, 0.0F);
            cow.setNoAi(true);
            cow.setBaby(baby);
            level.addFreshEntity(cow);
            Services.DATA.set(cow, change.apply(new AnimalTraits(sex, sex == Sex.FEMALE ? 0.85 : 1.2, 1.0, -1L)));
            if (pregnant) {
                Services.DATA.setPregnancy(cow, new Pregnancy(level.getGameTime() - 30_000L, 66_000L, "minecraft:cow", 1.25, 1, 1.0));
            }
            return cow.getUUID();
        });
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.level != null && entity(client.level.getEntity(id)) != null);
        context.waitTicks(5);
        context.setScreen(() -> new InspectorScreen((Animal) entity(context.computeOnClient(client -> client.level.getEntity(id)))));
        context.waitTicks(2);
        context.takeScreenshot(name);
        context.setScreen(() -> null);
        world.getServer().runOnServer(server -> {
            Entity cow = server.overworld().getEntity(id);
            if (cow != null) {
                cow.discard();
            }
        });
        world.getConnection().waitForClientboundPackets();
    }

    private static Entity entity(Entity entity) {
        return entity instanceof Animal animal && Services.DATA.get(animal).isPresent() ? entity : null;
    }
}
