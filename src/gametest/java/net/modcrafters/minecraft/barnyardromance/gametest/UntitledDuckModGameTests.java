package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.compat.Jdk;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsItems;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;

import java.lang.reflect.Field;
import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests.eggsNear;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// These run with Untitled Duck Mod loaded, through the farm-mods runs (gradle/farm-mods-runs.gradle). They find its
// creatures and items by id, so the mod still doesn't compile against it. In a run without it they pass at once and
// prove nothing, except that a farm-mods run with no jar in its mods folder fails instead of looking green
// (FarmMods.present).
public final class UntitledDuckModGameTests {
    private static final String MOD = "untitledduckmod";
    private static final String DUCK = "untitledduckmod:duck";
    private static final String GOOSE = "untitledduckmod:goose";

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("duck_mod_duck_lays_by_our_rules_and_its_timer_is_held", 20, UntitledDuckModGameTests::duckLaysByOurRulesAndItsTimerIsHeld),
            new BreedingGameTests.Case("excluded_duck_keeps_its_own_timer", 20, UntitledDuckModGameTests::excludedDuckKeepsItsOwnTimer),
            new BreedingGameTests.Case("food_does_not_breed_duck_mod_birds", 20, UntitledDuckModGameTests::foodDoesNotBreedBirds),
            new BreedingGameTests.Case("a_thrown_duck_egg_never_hatches", 20, UntitledDuckModGameTests::aThrownDuckEggNeverHatches),
            new BreedingGameTests.Case("an_excluded_birds_thrown_eggs_hatch_as_the_mod_does", 20, UntitledDuckModGameTests::anExcludedBirdsThrownEggsHatch),
            new BreedingGameTests.Case("taming_food_is_refused_and_familiarity_tames", 20, UntitledDuckModGameTests::tamingFoodIsRefusedAndFamiliarityTames),
            new BreedingGameTests.Case("excluded_duck_is_tamed_by_the_mods_food", 20, UntitledDuckModGameTests::excludedDuckIsTamedByTheModsFood),
            new BreedingGameTests.Case("a_sacked_duck_keeps_its_traits", 20, UntitledDuckModGameTests::aSackedDuckKeepsItsTraits)
    );

    private UntitledDuckModGameTests() {
    }

    public static void duckLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        for (String id : List.of(DUCK, GOOSE)) {
            Animal bird = bird(helper, id, Sex.FEMALE, 0.5, 3);
            setLayTime(helper, bird, 1);

            for (int tick = 0; tick < 20; tick++) {
                bird.aiStep();
            }

            // Held at 6000 as each aiStep starts, and the mod's own countdown takes one off after that.
            helper.assertTrue(layTime(helper, bird) >= 5000, "a " + id + "'s timer ran down to " + layTime(helper, bird));
            helper.assertTrue(eggsNear(helper, bird, egg(helper, id)) == 0, "a " + id + " laid by the mod's own timer");

            inPen(id, () -> EggLaying.layDue(bird, helper.getLevel(), 1));

            helper.assertTrue(eggsNear(helper, bird, egg(helper, id)) == 1, "no " + id + " egg on the ground");
            helper.assertTrue(eggsNear(helper, bird, EggsItems.FERTILIZED_EGG) == 0, "a fertilized egg on the ground");
            bird.discard();
        }
        helper.succeed();
    }

    // The mod's own timer has to come back when the mod's rules stop applying, or its ducks would never lay at all.
    public static void excludedDuckKeepsItsOwnTimer(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.5, 4);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            for (String json : List.of(
                    "{\"exclude\": [\"untitledduckmod:duck\"]}",
                    "{\"species\": {\"untitledduckmod:duck\": {\"eggs\": {\"enabled\": false}}}}")) {
                BarnyardConfig.set(BarnyardConfig.parse(json));
                setLayTime(helper, duck, 100);

                duck.aiStep();

                helper.assertTrue(layTime(helper, duck) == 99, "with " + json + " the timer is at " + layTime(helper, duck));
            }
        } finally {
            BarnyardConfig.set(config);
        }
        helper.succeed();
    }

    public static void foodDoesNotBreedBirds(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 1.0, 2);
        Animal goose = bird(helper, GOOSE, Sex.FEMALE, 1.0, 5);
        // Tamed, so the mod's own feeding would take seeds for breeding, as it does for a tame bird.
        Player owner = holding(helper, Items.WHEAT_SEEDS);
        ((TamableAnimal) duck).tame(owner);
        ((TamableAnimal) goose).tame(owner);

        FamiliarityActions.interact(duck, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        FamiliarityActions.interact(goose, holding(helper, Items.WHEAT_SEEDS), InteractionHand.MAIN_HAND);
        GameTestCompat.useOn(duck, holding(helper, Items.WHEAT_SEEDS));
        GameTestCompat.useOn(goose, holding(helper, Items.WHEAT_SEEDS));
        duck.setInLove(null);
        goose.setInLove(null);

        helper.assertTrue(!duck.isInLove(), "seeds put a duck in love");
        helper.assertTrue(!goose.isInLove(), "seeds put a goose in love");
        helper.succeed();
    }

    public static void aThrownDuckEggNeverHatches(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        for (int i = 0; i < 64; i++) {
            throwAndLand(helper, "duck_egg");
            throwAndLand(helper, "goose_egg");
        }

        helper.assertTrue(birdsIn(helper) == 0, "thrown plain eggs hatched " + birdsIn(helper) + " birds");
        helper.succeed();
    }

    // The mod's hatching has to come back with its birds, or a pack that switches them off loses it.
    public static void anExcludedBirdsThrownEggsHatch(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"untitledduckmod:duck\", \"untitledduckmod:goose\"]}"));
            for (int i = 0; i < 64; i++) {
                throwAndLand(helper, "duck_egg");
                throwAndLand(helper, "goose_egg");
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(birdsIn(helper) > 0, "128 eggs thrown with the birds excluded hatched nothing");
        helper.succeed();
    }

    // The mod's taming food (fish for ducks, kelp for geese) would tame them behind familiarity's back.
    public static void tamingFoodIsRefusedAndFamiliarityTames(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.0, 2);
        Animal goose = bird(helper, GOOSE, Sex.FEMALE, 0.0, 5);

        for (int click = 0; click < 30; click++) {
            ServerPlayer player = MockPlayers.inLevel(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));
            GameTestCompat.useOn(duck, player);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.KELP));
            GameTestCompat.useOn(goose, player);
        }
        helper.assertTrue(!((TamableAnimal) duck).isTame(), "fish tamed a duck");
        helper.assertTrue(!((TamableAnimal) goose).isTame(), "kelp tamed a goose");
        helper.assertTrue(FamiliarityActions.blocksVanillaTaming(duck, holding(helper, Items.COD), new ItemStack(Items.COD)) == InteractionResult.CONSUME,
                "the fish wasn't turned down");
        helper.assertTrue(FamiliarityActions.blocksVanillaTaming(goose, holding(helper, Items.KELP), new ItemStack(Items.KELP)) == InteractionResult.CONSUME,
                "the kelp wasn't turned down");
        helper.assertTrue(FamiliarityActions.blocksVanillaTaming(duck, holding(helper, Items.STICK), new ItemStack(Items.STICK)) == null,
                "a stick was turned down");

        Animal familiar = bird(helper, DUCK, Sex.FEMALE, 1.0, 3);
        Animal familiarGoose = bird(helper, GOOSE, Sex.FEMALE, 1.0, 6);
        FamiliarityActions.tameIfFamiliar(familiar, holding(helper, Items.COD), 1.0);
        FamiliarityActions.tameIfFamiliar(familiarGoose, holding(helper, Items.KELP), 1.0);
        helper.assertTrue(((TamableAnimal) familiar).isTame(), "a familiar duck didn't tame");
        helper.assertTrue(((TamableAnimal) familiarGoose).isTame(), "a familiar goose didn't tame");
        helper.succeed();
    }

    // With the bird switched off the mod's own taming comes back, or it couldn't be tamed at all.
    public static void excludedDuckIsTamedByTheModsFood(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal duck = bird(helper, DUCK, Sex.FEMALE, 0.0, 2);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"untitledduckmod:duck\"]}"));
            for (int click = 0; click < 60 && !((TamableAnimal) duck).isTame(); click++) {
                GameTestCompat.useOn(duck, holding(helper, Items.COD));
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(((TamableAnimal) duck).isTame(), "60 fish never tamed an excluded duck");
        helper.succeed();
    }

    // The duck sack saves the duck whole, and sets it down again as the same duck.
    public static void aSackedDuckKeepsItsTraits(GameTestHelper helper) {
        if (!FarmMods.present(helper, MOD)) {
            return;
        }
        Animal duck = GameTestCompat.spawn(helper, type(helper, DUCK), 4, 1, 4);
        Services.DATA.set(duck, AnimalTraits.of(Sex.MALE, 1.17).with(CARE, FamiliarityState.NONE.fed(0.6, -1L)));
        AnimalTraits before = traits(helper, duck);
        ServerPlayer player = MockPlayers.inLevel(helper);
        // The mock player is in creative, where the mod's sack is never used up and so never swapped in hand.
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(helper, "empty_duck_sack")));

        InteractionResult sacked = GameTestCompat.useOn(duck, player);

        ItemStack sack = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(sack.is(item(helper, "duck_sack")), "the player holds " + sack + " after sacking the duck: " + sacked);
        helper.assertTrue(duck.isRemoved(), "the sacked duck is still there");

        BlockPos floor = helper.absolutePos(new BlockPos(6, 0, 6));
        InteractionResult placed = sack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
        helper.assertTrue(placed.consumesAction(), "the sack didn't place the duck: " + placed);

        List<Animal> released = helper.getLevel().getEntitiesOfClass(Animal.class, new AABB(floor).inflate(2.0, 3.0, 2.0),
                animal -> animal.getType() == type(helper, DUCK));
        helper.assertTrue(released.size() == 1, "the sack released " + released.size() + " ducks");
        AnimalTraits after = traits(helper, Jdk.first(released));
        helper.assertTrue(after.sex() == before.sex(), "the duck's sex went from " + before.sex() + " to " + after.sex());
        helper.assertTrue(after.scale() == before.scale(), "the duck's scale went from " + before.scale() + " to " + after.scale());
        helper.assertTrue(after.get(CARE).familiarity() == before.get(CARE).familiarity(), "the duck's familiarity went from "
                + before.get(CARE).familiarity() + " to " + after.get(CARE).familiarity());
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Animal> type(GameTestHelper helper, String id) {
        return (EntityType<? extends Animal>) BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + id));
    }

    private static Item item(GameTestHelper helper, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(MOD + ":" + path))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no item " + path));
    }

    private static Item egg(GameTestHelper helper, String speciesId) {
        return item(helper, speciesId.substring(speciesId.indexOf(':') + 1) + "_egg");
    }

    private static Animal bird(GameTestHelper helper, String id, Sex sex, double familiarity, int x) {
        Animal bird = GameTestCompat.spawn(helper, type(helper, id), x, 1, 4);
        Services.DATA.set(bird, AnimalTraits.of(sex, sex == Sex.MALE ? 1.2 : EggGameTests.mediumHen()).with(CARE, FamiliarityState.NONE.fed(familiarity, -1L)));
        Services.DATA.set(bird, EggsFeature.LAYING, new Laying(GameDay.of(helper.getLevel().getOverworldClockTime()), List.of(), 0));
        return bird;
    }

    // Short ranges, because tests run side by side within 16 blocks.
    private static void inPen(String species, Runnable action) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"species\": {\"" + species + "\": {\"eggs\": {\"boxRange\": 8, \"roosterRange\": 4}}}}"));
            action.run();
        } finally {
            BarnyardConfig.set(config);
        }
    }

    // One of the mod's plain eggs, dropped onto the pen's floor and ticked by hand until it breaks, all within this tick.
    private static void throwAndLand(GameTestHelper helper, String eggPath) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(MOD + ":" + eggPath))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + eggPath));
        ThrowableItemProjectile egg = (ThrowableItemProjectile) GameTestCompat.unspawned(helper, type);
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 3.0, 4.5));
        egg.setPos(at.x, at.y, at.z);
        egg.setItem(new ItemStack(item(helper, eggPath)));
        egg.setDeltaMovement(0.0, -1.0, 0.0);
        helper.getLevel().addFreshEntity(egg);
        for (int i = 0; i < 40 && !egg.isRemoved(); i++) {
            egg.tick();
        }
    }

    private static int birdsIn(GameTestHelper helper) {
        AABB pen = new AABB(helper.absoluteVec(Vec3.ZERO), helper.absoluteVec(new Vec3(9.0, 5.0, 9.0)));
        return helper.getLevel().getEntitiesOfClass(Animal.class, pen,
                animal -> animal.getType() == type(helper, DUCK) || animal.getType() == type(helper, GOOSE)).size();
    }

    private static Field layTimeField(GameTestHelper helper, Animal bird) {
        for (Class<?> level = bird.getClass(); level != null; level = level.getSuperclass()) {
            try {
                Field field = level.getDeclaredField("eggLayTime");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // up one class
            }
        }
        throw GameTestCompat.failure(helper, bird.getClass() + " has no eggLayTime");
    }

    private static int layTime(GameTestHelper helper, Animal bird) {
        try {
            return layTimeField(helper, bird).getInt(bird);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't read eggLayTime: " + exception);
        }
    }

    private static void setLayTime(GameTestHelper helper, Animal bird, int ticks) {
        try {
            layTimeField(helper, bird).setInt(bird, ticks);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't set eggLayTime: " + exception);
        }
    }
}
