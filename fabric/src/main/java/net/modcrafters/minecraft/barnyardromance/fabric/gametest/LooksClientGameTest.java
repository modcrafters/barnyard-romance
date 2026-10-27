package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.modcrafters.minecraft.barnyardromance.config.ClientConfig;
import net.modcrafters.minecraft.barnyardromance.look.HornRoll;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

// Lines up rows of animals in front of the camera and screenshots their bellies and horns.
public class LooksClientGameTest implements FabricClientGameTest {
    // The chances in the shipped horn files, to pick UUIDs for horned and hornless females.
    private static final double CATTLE_CHANCE = 0.75;
    private static final double SHEEP_CHANCE = 0.25;
    private static final double GOAT_CHANCE = 0.8;
    private static final double SPACING = 1.7;
    // Each picked UUID is new, so two animals in one row never share one.
    private static long nextUuid = 1L;

    private record Spec(EntityType<? extends Animal> type, Sex sex, boolean baby, UUID uuid, UnaryOperator<AnimalTraits> change,
                        double pregnancy, Consumer<Animal> extra) {
        Spec(EntityType<? extends Animal> type, Sex sex) {
            this(type, sex, false, null, UnaryOperator.identity(), -1.0, animal -> {
            });
        }

        Spec asBaby() {
            return new Spec(type, sex, true, uuid, change, pregnancy, extra);
        }

        Spec horned(double chance, boolean horned) {
            return new Spec(type, sex, baby, femaleUuid(chance, horned), change, pregnancy, extra);
        }

        Spec traits(UnaryOperator<AnimalTraits> more) {
            return new Spec(type, sex, baby, uuid, more, pregnancy, extra);
        }

        Spec pregnant(double progress) {
            return new Spec(type, sex, baby, uuid, change, progress, extra);
        }

        Spec then(Consumer<Animal> more) {
            return new Spec(type, sex, baby, uuid, change, pregnancy, more);
        }
    }

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
            long now = world.getServer().computeOnServer(server -> server.overworld().getGameTime());

            row(context, world, "looks_cattle", List.of(
                    new Spec(EntityTypes.COW, Sex.MALE),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, true),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, false),
                    new Spec(EntityTypes.COW, Sex.MALE).traits(traits -> traits.withBornTick(now - traits.growthTicks() - 12_000L)),
                    new Spec(EntityTypes.COW, Sex.MALE).asBaby().traits(traits -> traits.withBornTick(now - 1_000L))
            ));
            row(context, world, "looks_cattle_variants", List.of(
                    new Spec(EntityTypes.COW, Sex.MALE).then(animal -> variant(animal, CowVariants.COLD)),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, false).then(animal -> variant(animal, CowVariants.COLD)),
                    new Spec(EntityTypes.COW, Sex.MALE).then(animal -> variant(animal, CowVariants.WARM)),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, false).then(animal -> variant(animal, CowVariants.WARM)),
                    new Spec(EntityTypes.MOOSHROOM, Sex.MALE)
            ));
            row(context, world, "looks_sheep", List.of(
                    new Spec(EntityTypes.SHEEP, Sex.MALE),
                    new Spec(EntityTypes.SHEEP, Sex.FEMALE).horned(SHEEP_CHANCE, true),
                    new Spec(EntityTypes.SHEEP, Sex.FEMALE).horned(SHEEP_CHANCE, false).pregnant(0.95),
                    new Spec(EntityTypes.SHEEP, Sex.MALE).asBaby().traits(traits -> traits.withBornTick(now - 1_000L))
            ));
            row(context, world, "looks_goats", List.of(
                    new Spec(EntityTypes.GOAT, Sex.MALE),
                    new Spec(EntityTypes.GOAT, Sex.MALE).then(animal -> ((Goat) animal).dropHorn()),
                    new Spec(EntityTypes.GOAT, Sex.FEMALE).horned(GOAT_CHANCE, true),
                    new Spec(EntityTypes.GOAT, Sex.FEMALE).horned(GOAT_CHANCE, false),
                    new Spec(EntityTypes.GOAT, Sex.MALE).asBaby().traits(traits -> traits.withBornTick(now - 1_000L))
            ));
            List<Spec> bellies = List.of(
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, true).pregnant(0.2),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, true).pregnant(0.7),
                    new Spec(EntityTypes.COW, Sex.FEMALE).horned(CATTLE_CHANCE, true).pregnant(1.0)
            );
            // Facing the camera, so the belly widens across the screen.
            row(context, world, "looks_belly", bellies, 90.0F);
            ClientConfig before = ClientConfig.current();
            context.runOnClient(client -> ClientConfig.set(new ClientConfig(false, false)));
            try {
                row(context, world, "looks_switched_off", bellies, 90.0F);
            } finally {
                context.runOnClient(client -> ClientConfig.set(before));
            }
        }
    }

    private static void row(ClientGameTestContext context, TestSingleplayerContext world, String name, List<Spec> specs) {
        row(context, world, name, specs, 135.0F);
    }

    private static void row(ClientGameTestContext context, TestSingleplayerContext world, String name, List<Spec> specs, float yaw) {
        // Facing east and a little down, with the animals in a line across the view and turned towards the camera.
        world.getServer().runCommand("tp @a 0.5 ~ 0.5 -90 12");
        world.getConnection().waitForClientboundPackets();
        List<UUID> ids = world.getServer().computeOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            ServerLevel level = player.level();
            List<UUID> spawned = new ArrayList<>();
            for (int index = 0; index < specs.size(); index++) {
                Spec spec = specs.get(index);
                Animal animal = spec.type().create(level, EntitySpawnReason.COMMAND);
                if (spec.uuid() != null) {
                    animal.setUUID(spec.uuid());
                }
                double z = player.getZ() + (index - (specs.size() - 1) / 2.0) * SPACING;
                animal.snapTo(player.getX() + 4.0, player.getY(), z, yaw, 0.0F);
                animal.setYHeadRot(yaw);
                animal.setYBodyRot(yaw);
                animal.setNoAi(true);
                animal.setBaby(spec.baby());
                level.addFreshEntity(animal);
                Services.DATA.set(animal, spec.change().apply(new AnimalTraits(spec.sex(), 1.0, 1.0, -1L)));
                // Joining rolled a random size; every animal gets the same one so they compare fairly.
                AttributeInstance scale = animal.getAttribute(Attributes.SCALE);
                if (scale != null) {
                    scale.setBaseValue(1.0);
                }
                if (spec.pregnancy() >= 0.0) {
                    long duration = 66_000L;
                    Services.DATA.setPregnancy(animal, new Pregnancy(level.getGameTime() - Math.round(duration * spec.pregnancy()), duration, "minecraft:cow", 1.0, 1, 1.0));
                }
                spec.extra().accept(animal);
                spawned.add(animal.getUUID());
            }
            return spawned;
        });
        world.getConnection().waitForClientboundPackets();
        context.waitFor(client -> client.level != null && ids.stream().allMatch(id -> tracked(client.level.getEntity(id))));
        context.waitTicks(10);
        context.takeScreenshot(name);
        world.getServer().runOnServer(server -> {
            for (UUID id : ids) {
                Entity animal = server.overworld().getEntity(id);
                if (animal != null) {
                    animal.discard();
                }
            }
            server.overworld().getEntities(EntityTypes.ITEM, item -> true).forEach(Entity::discard);
        });
        world.getConnection().waitForClientboundPackets();
    }

    private static void variant(Animal animal, ResourceKey<CowVariant> key) {
        ((Cow) animal).setVariant(animal.level().registryAccess().lookupOrThrow(Registries.COW_VARIANT).getOrThrow(key));
    }

    private static boolean tracked(Entity entity) {
        return entity instanceof Animal animal && Services.DATA.get(animal).isPresent();
    }

    // A female UUID that HornRoll gives horns (or not) at this chance.
    private static UUID femaleUuid(double chance, boolean horned) {
        while (true) {
            UUID uuid = new UUID(0x6261726e79617264L, nextUuid++);
            if (HornRoll.femaleHorned(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), chance) == horned) {
                return uuid;
            }
        }
    }
}
