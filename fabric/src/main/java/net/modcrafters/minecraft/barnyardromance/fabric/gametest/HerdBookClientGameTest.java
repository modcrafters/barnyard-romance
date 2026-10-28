package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import com.mojang.blaze3d.platform.InputConstants;
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

import org.jspecify.annotations.Nullable;

import java.util.UUID;

// Opens a herd book with a small cow family in a real client, clicks through it and screenshots its pages.
public class HerdBookClientGameTest implements FabricClientGameTest {
    // Where things are on the book, in GUI pixels from its top-left corner (see HerdBookScreen).
    private static final int DAISY_X = 36 + 12;
    private static final int DAISY_Y = 2 + 30 + 2 * 9 + 4;
    private static final int BUTTON_Y = 2 + 157 + 7;
    private static final int RETURN_X = 36 + 33;
    private static final int REMOVE_X = 36 + 114 - 3;

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

            // Real clicks: the contents list goes Females, Bluebell, Daisy.
            click(context, DAISY_X, DAISY_Y);
            expectShown(context, daisy, "Clicking Daisy's row should open her page");
            context.takeScreenshot("herd_book_animal");

            click(context, RETURN_X, BUTTON_Y);
            expectShown(context, null, "The return button should go back to the contents");
            click(context, DAISY_X, DAISY_Y);
            expectShown(context, daisy, "Clicking Daisy's row again should open her page");

            click(context, REMOVE_X, BUTTON_Y);
            context.takeScreenshot("herd_book_remove_sure");

            click(context, REMOVE_X, BUTTON_Y);
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(2);
            expectShown(context, null, "Removing Daisy should close her page");
            context.takeScreenshot("herd_book_after_remove");
            context.setScreen(() -> null);
        }
    }

    private static void click(ClientGameTestContext context, int bookX, int bookY) {
        double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        int left = context.computeOnClient(client -> (client.getWindow().getGuiScaledWidth() - 192) / 2);
        context.getInput().setCursorPos((left + bookX) * scale, bookY * scale);
        context.waitTicks(1);
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(2);
    }

    private static void expectShown(ClientGameTestContext context, @Nullable UUID expected, String message) {
        UUID shown = context.computeOnClient(client -> ((HerdBookScreen) client.gui.screen()).shownAnimal());
        if (expected == null ? shown != null : !expected.equals(shown)) {
            throw new AssertionError(message + ", but the book shows " + shown);
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
