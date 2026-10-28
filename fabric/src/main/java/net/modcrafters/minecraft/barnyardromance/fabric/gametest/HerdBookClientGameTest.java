package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.client.HerdBookScreen;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.UUID;

// Opens a herd book with a small cow family in a real client and screenshots its pages.
public class HerdBookClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set day");
            world.getServer().runCommand("gamerule advance_time false");
            world.getServer().runOnServer(server -> {
                ((ServerLevelData) server.overworld().getLevelData()).setGameTime(1_000_000L);
                server.forceGameTimeSynchronization();
            });
            world.getConnection().waitForClientboundPackets();

            UUID daisy = world.getServer().computeOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                long now = level.getGameTime();
                long clock = level.getOverworldClockTime();
                ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
                player.setItemInHand(InteractionHand.MAIN_HAND, book);
                Animal bess = cow(level, player, "Old Bess", Sex.FEMALE, false, new AnimalTraits(Sex.FEMALE, 0.9, 1.0, -1L).withBornTick(now - 900_000L));
                Animal tom = cow(level, player, "Big Tom", Sex.MALE, false, new AnimalTraits(Sex.MALE, 1.3, 1.0, clock).withBornTick(now - 400_000L));
                Animal daisyCow = cow(level, player, "Daisy", Sex.FEMALE, false, new AnimalTraits(Sex.FEMALE, 1.0, 0.8, clock)
                        .withBornTick(now - 430_000L)
                        .withFertility(0.9, now)
                        .withLineage(new Lineage(bess.getUUID(), null)));
                Services.DATA.setPregnancy(daisyCow, new Pregnancy(now - 40_000L, 66_000L, "minecraft:cow", 1.3, 1, 0.8));
                Animal bluebell = cow(level, player, "Bluebell", Sex.FEMALE, false, new AnimalTraits(Sex.FEMALE, 0.85, 0.9, -1L)
                        .withBornTick(now - 300_000L)
                        .withLactation(Lactation.NONE.afterBirth(now - 1_000L, now + 24_000L, 72_000L)));
                Animal calf = cow(level, player, "", Sex.FEMALE, true, new AnimalTraits(Sex.FEMALE, 0.8, 1.0, clock)
                        .withBornTick(now - 5_000L)
                        .withLineage(new Lineage(daisyCow.getUUID(), tom.getUUID())));
                for (Animal animal : new Animal[] {bess, tom, daisyCow, bluebell, calf}) {
                    HerdBooks.register(book, animal);
                }
                // More of Daisy's young than her page has room for.
                for (int i = 0; i < 5; i++) {
                    HerdBooks.register(book, cow(level, player, "Calf " + (i + 2), Sex.MALE, true, new AnimalTraits(Sex.MALE, 0.8, 1.0, clock)
                            .withBornTick(now - 90_000L)
                            .withLineage(new Lineage(daisyCow.getUUID(), tom.getUUID()))));
                }
                // The calf drops below the threshold, and Old Bess dies but stays as Daisy's mother.
                Services.DATA.set(calf, Services.DATA.get(calf).orElseThrow().fed(0.1, clock));
                bess.discard();
                HerdBooks.show(player, player.getItemInHand(InteractionHand.MAIN_HAND));
                return daisyCow.getUUID();
            });
            world.getConnection().waitForClientboundPackets();
            context.waitFor(client -> client.gui.screen() instanceof HerdBookScreen);
            context.waitTicks(2);
            context.takeScreenshot("herd_book_contents");

            context.runOnClient(client -> ((HerdBookScreen) client.gui.screen()).showAnimal(daisy));
            context.waitTicks(2);
            context.takeScreenshot("herd_book_animal");

            context.runOnClient(client -> ((HerdBookScreen) client.gui.screen()).pressRemove());
            context.waitTicks(2);
            context.takeScreenshot("herd_book_remove_sure");

            context.runOnClient(client -> ((HerdBookScreen) client.gui.screen()).pressRemove());
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(2);
            context.takeScreenshot("herd_book_after_remove");
            context.setScreen(() -> null);
        }
    }

    private static Animal cow(ServerLevel level, ServerPlayer player, String name, Sex sex, boolean baby, AnimalTraits traits) {
        EntityType<? extends Animal> type = EntityTypes.COW;
        Animal cow = type.create(level, EntitySpawnReason.COMMAND);
        cow.snapTo(player.getX() + 4.0, player.getY(), player.getZ() + level.getRandom().nextInt(5) - 2, 90.0F, 0.0F);
        cow.setNoAi(true);
        cow.setBaby(baby);
        if (!name.isEmpty()) {
            cow.setCustomName(Component.literal(name));
        }
        level.addFreshEntity(cow);
        Services.DATA.set(cow, traits);
        return cow;
    }
}
