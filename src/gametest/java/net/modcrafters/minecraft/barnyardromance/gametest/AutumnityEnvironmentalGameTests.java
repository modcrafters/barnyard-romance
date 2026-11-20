package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.eggs.EggLaying;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsItems;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.shear.ShearActions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests.eggsNear;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

// These run with Autumnity and Environmental loaded, through the NeoForge 1.21.1 farm-mods run (neither has a 26.3
// build). They find the mods' creatures and items by id and reflection, so the mods still don't compile against them.
// In a run without them they pass at once and prove nothing, except that a farm-mods run with a jar missing fails
// instead of looking green (FarmMods.present).
public final class AutumnityEnvironmentalGameTests {
    private static final String AUTUMNITY = "autumnity";
    private static final String ENVIRONMENTAL = "environmental";
    private static final String TURKEY = "autumnity:turkey";
    private static final String DUCK = "environmental:duck";
    private static final String YAK = "environmental:yak";

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("autumnity_turkey_lays_by_our_rules_and_its_timer_is_held", 20, AutumnityEnvironmentalGameTests::turkeyLaysByOurRulesAndItsTimerIsHeld),
            new BreedingGameTests.Case("excluded_turkey_keeps_its_own_timer", 20, AutumnityEnvironmentalGameTests::excludedTurkeyKeepsItsOwnTimer),
            new BreedingGameTests.Case("food_does_not_breed_a_turkey", 20, AutumnityEnvironmentalGameTests::foodDoesNotBreedATurkey),
            new BreedingGameTests.Case("a_thrown_turkey_egg_never_hatches", 20, AutumnityEnvironmentalGameTests::aThrownTurkeyEggNeverHatches),
            new BreedingGameTests.Case("an_excluded_turkeys_thrown_eggs_hatch_as_the_mod_does", 20, AutumnityEnvironmentalGameTests::anExcludedTurkeysThrownEggsHatch),
            new BreedingGameTests.Case("environmental_duck_lays_by_our_rules_and_its_timer_is_held", 20, AutumnityEnvironmentalGameTests::duckLaysByOurRulesAndItsTimerIsHeld),
            new BreedingGameTests.Case("excluded_environmental_duck_keeps_its_own_timer", 20, AutumnityEnvironmentalGameTests::excludedDuckKeepsItsOwnTimer),
            new BreedingGameTests.Case("food_does_not_breed_an_environmental_duck", 20, AutumnityEnvironmentalGameTests::foodDoesNotBreedADuck),
            new BreedingGameTests.Case("a_thrown_environmental_duck_egg_never_hatches", 20, AutumnityEnvironmentalGameTests::aThrownDuckEggNeverHatches),
            new BreedingGameTests.Case("an_excluded_environmental_ducks_thrown_eggs_hatch_as_the_mod_does", 20, AutumnityEnvironmentalGameTests::anExcludedDucksThrownEggsHatch),
            new BreedingGameTests.Case("a_yak_is_milked_by_our_rules", 20, AutumnityEnvironmentalGameTests::aYakIsMilkedByOurRules),
            new BreedingGameTests.Case("a_yak_is_sheared_once_a_day", 20, AutumnityEnvironmentalGameTests::aYakIsShearedOnceADay),
            new BreedingGameTests.Case("a_dispenser_shears_a_yak_once_a_day", 20, AutumnityEnvironmentalGameTests::aDispenserShearsAYakOnceADay),
            new BreedingGameTests.Case("a_tracked_sows_litter_is_the_barnyards_with_environmental_loaded", 200, AutumnityEnvironmentalGameTests::aTrackedSowsLitterIsTheBarnyards),
            new BreedingGameTests.Case("an_excluded_sow_keeps_environmentals_larger_litter", 20, AutumnityEnvironmentalGameTests::anExcludedSowKeepsEnvironmentalsLargerLitter),
            new BreedingGameTests.Case("a_sow_with_pregnancy_off_keeps_environmentals_larger_litter", 20, AutumnityEnvironmentalGameTests::aSowWithPregnancyOffKeepsEnvironmentalsLargerLitter)
    );

    private AutumnityEnvironmentalGameTests() {
    }

    public static void turkeyLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        if (!FarmMods.present(helper, AUTUMNITY)) {
            return;
        }
        laysByOurRulesAndItsTimerIsHeld(helper, TURKEY);
    }

    public static void duckLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        laysByOurRulesAndItsTimerIsHeld(helper, DUCK);
    }

    private static void laysByOurRulesAndItsTimerIsHeld(GameTestHelper helper, String id) {
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
        helper.succeed();
    }

    public static void excludedTurkeyKeepsItsOwnTimer(GameTestHelper helper) {
        if (!FarmMods.present(helper, AUTUMNITY)) {
            return;
        }
        excludedKeepsItsOwnTimer(helper, TURKEY);
    }

    public static void excludedDuckKeepsItsOwnTimer(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        excludedKeepsItsOwnTimer(helper, DUCK);
    }

    // The mod's own timer has to come back when the mod's rules stop applying, or its birds would never lay at all.
    private static void excludedKeepsItsOwnTimer(GameTestHelper helper, String id) {
        Animal bird = bird(helper, id, Sex.FEMALE, 0.5, 4);
        BarnyardConfig config = BarnyardConfig.current();
        try {
            for (String json : List.of(
                    "{\"exclude\": [\"" + id + "\"]}",
                    "{\"species\": {\"" + id + "\": {\"eggs\": {\"enabled\": false}}}}")) {
                BarnyardConfig.set(BarnyardConfig.parse(json));
                setLayTime(helper, bird, 100);

                bird.aiStep();

                helper.assertTrue(layTime(helper, bird) == 99, "with " + json + " the timer is at " + layTime(helper, bird));
            }
        } finally {
            BarnyardConfig.set(config);
        }
        helper.succeed();
    }

    public static void foodDoesNotBreedATurkey(GameTestHelper helper) {
        if (!FarmMods.present(helper, AUTUMNITY)) {
            return;
        }
        foodDoesNotBreed(helper, TURKEY, item(helper, "autumnity:foul_berries"));
    }

    public static void foodDoesNotBreedADuck(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        foodDoesNotBreed(helper, DUCK, Items.SEAGRASS);
    }

    private static void foodDoesNotBreed(GameTestHelper helper, String id, Item food) {
        Animal bird = bird(helper, id, Sex.FEMALE, 1.0, 2);

        FamiliarityActions.interact(bird, holding(helper, food), InteractionHand.MAIN_HAND);
        GameTestCompat.useOn(bird, holding(helper, food));
        helper.assertTrue(bird.isFood(new ItemStack(food)), "the mod's own breeding food isn't " + food);
        bird.setInLove(null);

        helper.assertTrue(!bird.isInLove(), "food put a " + id + " in love");
        helper.succeed();
    }

    public static void aThrownTurkeyEggNeverHatches(GameTestHelper helper) {
        if (!FarmMods.present(helper, AUTUMNITY)) {
            return;
        }
        for (int i = 0; i < 64; i++) {
            throwAndLand(helper, "autumnity:turkey_egg");
        }

        helper.assertTrue(birdsIn(helper, TURKEY) == 0, "thrown plain turkey eggs hatched " + birdsIn(helper, TURKEY) + " turkeys");
        helper.succeed();
    }

    public static void aThrownDuckEggNeverHatches(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        for (int i = 0; i < 64; i++) {
            throwAndLand(helper, "environmental:duck_egg");
        }

        helper.assertTrue(birdsIn(helper, DUCK) == 0, "thrown plain duck eggs hatched " + birdsIn(helper, DUCK) + " ducks");
        helper.succeed();
    }

    // The mod's hatching has to come back with its birds, or a pack that switches them off loses it.
    public static void anExcludedTurkeysThrownEggsHatch(GameTestHelper helper) {
        if (!FarmMods.present(helper, AUTUMNITY)) {
            return;
        }
        excludedBirdsThrownEggsHatch(helper, TURKEY, "autumnity:turkey_egg");
    }

    public static void anExcludedDucksThrownEggsHatch(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        excludedBirdsThrownEggsHatch(helper, DUCK, "environmental:duck_egg");
    }

    private static void excludedBirdsThrownEggsHatch(GameTestHelper helper, String id, String egg) {
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"" + id + "\"]}"));
            for (int i = 0; i < 128; i++) {
                throwAndLand(helper, egg);
            }
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(birdsIn(helper, id) > 0, "128 eggs thrown with " + id + " excluded hatched nothing");
        helper.succeed();
    }

    // The yak milks in its own mobInteract with any iron bucket, which the barnyard's rules answer first: a bull and a
    // dry cow are refused, and a lactating familiar one gives milk once a day.
    public static void aYakIsMilkedByOurRules(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        Animal bull = male(helper, type(helper, YAK));
        Player player = holding(helper, Items.BUCKET);
        GameTestCompat.interact(bull, player);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a bull yak gave " + player.getMainHandItem());

        Animal dry = female(helper, type(helper, YAK));
        player = holding(helper, Items.BUCKET);
        GameTestCompat.interact(dry, player);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a dry yak gave " + player.getMainHandItem());

        // A small yak is milked once a day; the second bucket today is refused like any cow's.
        Animal yak = ProductGameTests.mother(helper, type(helper, YAK), 0.0);
        ProductGameTests.assertMilks(helper, yak, Items.BUCKET, Items.MILK_BUCKET);
        ProductGameTests.assertMilks(helper, yak, Items.BUCKET, Items.BUCKET);
        helper.succeed();
    }

    // Shears in hand reach the yak through NeoForge's IShearable (ShearsItem), and its own mobInteract has no shearing:
    // the first shearing counts, and the second the same day is refused with the "enough" message.
    public static void aYakIsShearedOnceADay(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        Animal yak = female(helper, type(helper, YAK));
        Shearable shearable = (Shearable) yak;
        helper.assertTrue(shearable.readyForShearing(), "a yak was not ready for shearing");

        GameTestCompat.useOn(yak, holding(helper, Items.SHEARS));
        helper.assertTrue(!shearable.readyForShearing(), "a familiar yak was not sheared");
        helper.assertTrue(ShearActions.shearedToday(yak), "the shearing was not counted");

        regrow(helper, yak);
        helper.assertTrue(shearable.readyForShearing(), "the hair did not grow back");
        Player player = holding(helper, Items.SHEARS);
        InteractionResult refused = ShearActions.refuses(yak, player, player.getMainHandItem());
        helper.assertTrue(refused == InteractionResult.FAIL, "the second shearing was answered " + refused + ", not refused with ENOUGH");
        GameTestCompat.useOn(yak, player);
        helper.assertTrue(shearable.readyForShearing(), "a yak was sheared twice in one day");
        helper.succeed();
    }

    // A real dispenser next to the yak shears it through IShearable.onSheared, and counts it; the second dispense the
    // same day does nothing.
    public static void aDispenserShearsAYakOnceADay(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        Animal yak = female(helper, type(helper, YAK));
        Shearable shearable = (Shearable) yak;
        ServerLevel level = helper.getLevel();
        BlockPos dispenserPos = yak.blockPosition().east();
        level.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.WEST), 3);
        DispenserBlockEntity dispenser = (DispenserBlockEntity) level.getBlockEntity(dispenserPos);
        dispenser.setItem(0, new ItemStack(Items.SHEARS));

        // The dispenser's own tick is what dispenses.
        level.getBlockState(dispenserPos).tick(level, dispenserPos, level.getRandom());

        helper.assertTrue(!shearable.readyForShearing(), "the dispenser did not shear the yak");
        helper.assertTrue(ShearActions.shearedToday(yak), "the dispenser's shearing was not counted");

        regrow(helper, yak);
        dispenser.setItem(0, new ItemStack(Items.SHEARS));
        level.getBlockState(dispenserPos).tick(level, dispenserPos, level.getRandom());

        helper.assertTrue(shearable.readyForShearing(), "a dispenser sheared a yak twice in one day");
        helper.succeed();
    }

    // Environmental adds piglets to every breeding in a BabyEntitySpawnEvent handler, which made a sow's litter come out
    // larger than the litter she was carrying. The barnyard owns the litter of a tracked sow, so exactly the stored
    // number of piglets is born with the mod loaded. This is BreedingGameTests.pigsConceiveALitterAndRest, run with
    // Environmental, and it counts the same way.
    public static void aTrackedSowsLitterIsTheBarnyards(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        long now = helper.getLevel().getGameTime();
        mother.setInLove(null);
        father.setInLove(null);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        var pregnancy = Services.DATA.get(mother, PregnancyFeature.PREGNANCY).orElseThrow(() -> GameTestCompat.failure(helper, "the sow didn't conceive"));
        int litterSize = pregnancy.litterSize();
        helper.assertTrue(helper.getEntities(EntityTypes.PIG).stream().noneMatch(Animal::isBaby), "piglets were born at conception");
        Services.DATA.set(mother, PregnancyFeature.PREGNANCY, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isEmpty(), "the sow is still pregnant");
            List<? extends Animal> babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).toList();
            helper.assertTrue(babies.size() == litterSize, "expected " + litterSize + " piglets, found " + babies.size());
        });
    }

    // A pack that switches pigs off gets Environmental's larger litters back: its handler isn't turned down for a species
    // the barnyard leaves alone.
    public static void anExcludedSowKeepsEnvironmentalsLargerLitter(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        BarnyardConfig config = BarnyardConfig.current();
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:pig\"]}"));
            Animal mother = female(helper, EntityTypes.PIG);
            Animal father = male(helper, EntityTypes.PIG);
            mother.setInLove(null);
            father.setInLove(null);

            mother.spawnChildFromBreeding(helper.getLevel(), father);

            long babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).count();
            helper.assertTrue(babies >= 2, "an excluded sow bore " + babies + " piglets, with no extras from Environmental");
        } finally {
            BarnyardConfig.set(config);
        }
        helper.succeed();
    }

    // With pregnancy switched off a tracked sow breeds the vanilla way, so the barnyard sets no litter and
    // Environmental's extra piglets come back.
    public static void aSowWithPregnancyOffKeepsEnvironmentalsLargerLitter(GameTestHelper helper) {
        if (!FarmMods.present(helper, ENVIRONMENTAL)) {
            return;
        }
        UpkeepGameTests.withConfig("{\"pregnancyEnabled\": false}", () -> {
            Animal mother = female(helper, EntityTypes.PIG);
            Animal father = male(helper, EntityTypes.PIG);
            mother.setInLove(null);
            father.setInLove(null);

            mother.spawnChildFromBreeding(helper.getLevel(), father);

            helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isEmpty(), "the sow conceived with pregnancy off");
            long babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).count();
            helper.assertTrue(babies >= 2, "a sow bred with pregnancy off bore " + babies + " piglets, with no extras from Environmental");
        });
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Animal> type(GameTestHelper helper, String id) {
        return (EntityType<? extends Animal>) BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + id));
    }

    private static Item item(GameTestHelper helper, String id) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(id))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no item " + id));
    }

    private static Item egg(GameTestHelper helper, String speciesId) {
        return item(helper, speciesId + "_egg");
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

    // One of the mod's plain eggs (its entity and item share a path), dropped onto the pen's floor and ticked by hand until
    // it breaks, all within this tick.
    private static void throwAndLand(GameTestHelper helper, String eggId) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(eggId))
                .orElseThrow(() -> GameTestCompat.failure(helper, "no entity " + eggId));
        ThrowableItemProjectile egg = (ThrowableItemProjectile) GameTestCompat.unspawned(helper, type);
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 3.0, 4.5));
        egg.setPos(at.x, at.y, at.z);
        egg.setItem(new ItemStack(item(helper, eggId)));
        egg.setDeltaMovement(0.0, -1.0, 0.0);
        helper.getLevel().addFreshEntity(egg);
        for (int i = 0; i < 40 && !egg.isRemoved(); i++) {
            egg.tick();
        }
    }

    private static int birdsIn(GameTestHelper helper, String id) {
        AABB pen = new AABB(helper.absoluteVec(Vec3.ZERO), helper.absoluteVec(new Vec3(9.0, 5.0, 9.0)));
        return helper.getLevel().getEntitiesOfClass(Animal.class, pen, animal -> animal.getType() == type(helper, id)).size();
    }

    private static Field layTimeField(GameTestHelper helper, Animal bird) {
        for (Class<?> level = bird.getClass(); level != null; level = level.getSuperclass()) {
            try {
                Field field = level.getDeclaredField("timeUntilNextEgg");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // up one class
            }
        }
        throw GameTestCompat.failure(helper, bird.getClass() + " has no timeUntilNextEgg");
    }

    private static int layTime(GameTestHelper helper, Animal bird) {
        try {
            return layTimeField(helper, bird).getInt(bird);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't read timeUntilNextEgg: " + exception);
        }
    }

    private static void setLayTime(GameTestHelper helper, Animal bird, int ticks) {
        try {
            layTimeField(helper, bird).setInt(bird, ticks);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't set timeUntilNextEgg: " + exception);
        }
    }

    private static void regrow(GameTestHelper helper, Animal yak) {
        try {
            Method setSheared = yak.getClass().getMethod("setSheared", boolean.class);
            setSheared.invoke(yak, false);
        } catch (ReflectiveOperationException exception) {
            throw GameTestCompat.failure(helper, "can't regrow a yak's hair: " + exception);
        }
    }
}
