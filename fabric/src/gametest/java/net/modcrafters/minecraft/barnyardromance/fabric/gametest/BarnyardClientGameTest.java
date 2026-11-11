package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardParticles;
import net.modcrafters.minecraft.barnyardromance.client.HerdBookScreen;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.milk.MilkItems;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardTab;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.familiarity.Origin;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.pregnancy.BreedingState;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;
import static net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancySlices.BREEDING;

// Opens the herd book at a few prepared animals in a real client and screenshots each page,
// then checks the sneak card, the superfoods in the hotbar and the creative tab.
public class BarnyardClientGameTest implements FabricClientGameTest {
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

            bookPage(context, world, "herd_page_worn_out_heavy_cow", Sex.FEMALE, false,
                    traits -> traits.withWeight(0.8).with(BREEDING, BreedingState.NONE.withFertility(0.1, now)), false);
            bookPage(context, world, "herd_page_light_bull", Sex.MALE, false,
                    traits -> traits.withWeight(0.3), false);
            bookPage(context, world, "herd_page_pregnant_cow", Sex.FEMALE, false,
                    traits -> traits.withWeight(0.55).with(BREEDING, BreedingState.NONE.withFertility(0.75, now)), true);
            bookPage(context, world, "herd_page_calf", Sex.FEMALE, true,
                    traits -> traits.withWeight(0.62), false);
            bookPage(context, world, "herd_page_strong_stallion", EntityTypes.HORSE, Sex.MALE, false,
                    traits -> traits.withWeight(0.7), false);
            bookPage(context, world, "herd_page_young_bull", Sex.MALE, false,
                    traits -> traits.withBornTick(now - 30_000L), false);
            bookPage(context, world, "herd_page_old_cow", Sex.FEMALE, false,
                    traits -> traits.withBornTick(now - 800_000L).with(BREEDING, BreedingState.NONE.withFertility(0.3, now)), false);
            bookPage(context, world, "herd_page_milking_cow", Sex.FEMALE, false,
                    traits -> traits.with(LACTATION, Lactation.NONE.afterBirth(now, now + 24_000L, 72_000L)), false);
            bookPage(context, world, "herd_page_wild_caught_cow", Sex.FEMALE, false,
                    traits -> traits.with(CARE, traits.get(CARE).fed(0.4, -1L).withOrigin(Origin.WILD)), false);
            sneakCard(context, world, now);
            oldHearts(context, world, now);
            superfoods(context, world);
            creativeTab(context, world);
        }
    }

    // The superfood and milk bucket textures in the hotbar, to catch a missing texture.
    private static void superfoods(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            player.getInventory().setItem(0, new ItemStack(BarnyardItems.BREEDERS_MASH, 2));
            player.getInventory().setItem(1, new ItemStack(BarnyardItems.GOLDEN_BRAN, 2));
            player.getInventory().setItem(2, new ItemStack(MilkItems.SHEEP_MILK_BUCKET));
            player.getInventory().setItem(3, new ItemStack(MilkItems.GOAT_MILK_BUCKET));
        });
        world.getConnection().waitForClientboundPackets();
        context.waitTicks(5);
        context.takeScreenshot("superfoods_hotbar");
    }

    private static void creativeTab(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("gamemode creative @a");
        world.getConnection().waitForClientboundPackets();
        context.setScreen(() -> context.computeOnClient(client ->
                new CreativeModeInventoryScreen(client.player, client.player.connection.enabledFeatures(), true)));
        context.runOnClient(client -> ((FabricCreativeModeInventoryScreen) client.gui.screen())
                .setSelectedTab(BuiltInRegistries.CREATIVE_MODE_TAB.getValue(BarnyardTab.ID)));
        context.waitTicks(5);
        context.takeScreenshot("creative_tab");
        // The breeder's mash slot in the tab's first row, at the test window's size.
        context.getInput().setCursorPos(485.0, 157.0);
        context.waitTicks(3);
        context.takeScreenshot("breeders_mash_tooltip");
        context.setScreen(() -> null);
        world.getServer().runCommand("gamemode survival @a");
    }

    // Looking at a cow while sneaking shows the full stat card above it.
    private static void sneakCard(ClientGameTestContext context, TestSingleplayerContext world, long now) {
        world.getServer().runCommand("tp @a ~ ~ ~ -90 8");
        UUID id = world.getServer().computeOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            Animal cow = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
            cow.snapTo(player.getX() + 3.0, player.getY(), player.getZ(), 90.0F, 0.0F);
            cow.setNoAi(true);
            player.level().addFreshEntity(cow);
            Services.DATA.set(cow, AnimalTraits.of(Sex.FEMALE, 0.9).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)).withWeight(0.6).with(BREEDING, BreedingState.NONE.withFertility(0.8, now)));
            return cow.getUUID();
        });
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.level != null && entity(client.level.getEntity(id)) != null);
        context.waitTicks(10);
        context.getInput().holdKey(options -> options.keyShift);
        context.waitTicks(10);
        context.takeScreenshot("sneak_card_cow");
        context.getInput().releaseKey(options -> options.keyShift);
        context.waitTicks(2);
        world.getServer().runOnServer(server -> {
            Entity cow = server.overworld().getEntity(id);
            if (cow != null) {
                cow.discard();
            }
        });
        world.getConnection().waitForClientboundPackets();
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
            Services.DATA.set(cow, AnimalTraits.of(Sex.FEMALE, 0.85).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)).withBornTick(now - 800_000L));
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

    private static void bookPage(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            String name,
            Sex sex,
            boolean baby,
            UnaryOperator<AnimalTraits> change,
            boolean pregnant
    ) {
        bookPage(context, world, name, EntityTypes.COW, sex, baby, change, pregnant);
    }

    private static void bookPage(
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
            Services.DATA.set(cow, change.apply(AnimalTraits.of(sex, sex == Sex.FEMALE ? 0.85 : 1.2).with(CARE, FamiliarityState.NONE.fed(1.0, -1L))));
            if (pregnant) {
                Services.DATA.set(cow, PregnancyFeature.PREGNANCY, new Pregnancy(level.getGameTime() - 30_000L, 66_000L, "minecraft:cow", 1.25, 1, 1.0));
            }
            // A fresh book for each animal, opened at its page the way using the book on it does.
            ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
            player.setItemInHand(InteractionHand.MAIN_HAND, book);
            HerdBooks.register(book, cow);
            HerdBooks.show(player, book, true, Optional.of(cow.getUUID()));
            return cow.getUUID();
        });
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.gui.screen() instanceof HerdBookScreen screen && id.equals(screen.shownAnimal()));
        context.waitTicks(2);
        context.takeScreenshot(name);
        if (name.endsWith("pregnant_cow") || name.endsWith("light_bull")) {
            // A long entry runs over more than one leaf: Page Down turns to the second.
            context.getInput().pressKey(InputConstants.KEY_PAGEDOWN);
            context.waitTicks(2);
            context.takeScreenshot(name + "_leaf_2");
        }
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
