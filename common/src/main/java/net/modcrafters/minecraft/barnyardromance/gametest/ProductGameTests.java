package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.drops.MeatDrop;
import net.modcrafters.minecraft.barnyardromance.drops.ProductDrops;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeSettings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

// In-game checks for milk, sized drops, shearing and taming carnivores, run by each loader's game test server.
public final class ProductGameTests {
    private static final TagKey<Item> COMMON_MILK = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "buckets/milk"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("old_traits_load_without_lactation", 20, ProductGameTests::oldTraitsLoadWithoutLactation),
            new BreedingGameTests.Case("cake_takes_every_milk", 20, ProductGameTests::cakeTakesEveryMilk),
            new BreedingGameTests.Case("milk_buckets_are_in_the_common_tag", 20, ProductGameTests::milkBucketsAreInTheCommonTag),
            new BreedingGameTests.Case("goat_milk_clears_effects", 20, ProductGameTests::goatMilkClearsEffects),
            new BreedingGameTests.Case("fresh_mother_cow_gives_milk", 20, ProductGameTests::freshMotherCowGivesMilk),
            new BreedingGameTests.Case("goat_gives_goat_milk", 20, ProductGameTests::goatGivesGoatMilk),
            new BreedingGameTests.Case("sheep_gives_sheep_milk", 20, ProductGameTests::sheepGivesSheepMilk),
            new BreedingGameTests.Case("refused_milking_keeps_the_bucket", 20, ProductGameTests::refusedMilkingKeepsTheBucket),
            new BreedingGameTests.Case("large_cow_milks_twice_a_day", 20, ProductGameTests::largeCowMilksTwiceADay),
            new BreedingGameTests.Case("birth_starts_milk", 200, ProductGameTests::birthStartsMilk),
            new BreedingGameTests.Case("wild_cow_gets_one_milk_window", 20, ProductGameTests::wildCowGetsOneMilkWindow),
            new BreedingGameTests.Case("mooshroom_bowl_stays_vanilla", 20, ProductGameTests::mooshroomBowlStaysVanilla),
            new BreedingGameTests.Case("sized_drops_follow_the_rule", 20, ProductGameTests::sizedDropsFollowTheRule),
            new BreedingGameTests.Case("meat_keeps_its_curve", 20, ProductGameTests::meatKeepsItsCurve),
            new BreedingGameTests.Case("big_sheep_shear_more_wool", 20, ProductGameTests::bigSheepShearMoreWool)
    );

    private ProductGameTests() {
    }

    // Traits saved before Stage 5 have no lactation and load with none.
    public static void oldTraitsLoadWithoutLactation(GameTestHelper helper) {
        AnimalTraits saved = new AnimalTraits(Sex.FEMALE, 1.0, 0.5, 10L);
        CompoundTag tag = (CompoundTag) AnimalTraitCodecs.CODEC.encodeStart(NbtOps.INSTANCE, saved).getOrThrow();
        tag.remove("lactation");

        AnimalTraits loaded = AnimalTraitCodecs.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        helper.assertTrue(loaded.lactation().equals(Lactation.NONE), "old traits loaded with " + loaded.lactation());

        Lactation lactation = new Lactation(100L, 200L, 50L, 1, true);
        AnimalTraits milking = saved.withLactation(lactation);
        CompoundTag milkingTag = (CompoundTag) AnimalTraitCodecs.CODEC.encodeStart(NbtOps.INSTANCE, milking).getOrThrow();
        AnimalTraits reloaded = AnimalTraitCodecs.CODEC.parse(NbtOps.INSTANCE, milkingTag).getOrThrow();
        helper.assertTrue(reloaded.lactation().equals(lactation), "lactation came back as " + reloaded.lactation());
        ByteBuf buffer = Unpooled.buffer();
        AnimalTraitCodecs.STREAM_CODEC.encode(buffer, milking);
        AnimalTraits synced = AnimalTraitCodecs.STREAM_CODEC.decode(buffer);
        helper.assertTrue(synced.lactation().equals(lactation), "lactation synced as " + synced.lactation());
        helper.succeed();
    }

    // The mod's cake recipe takes any bucket in barnyard_romance:milk_buckets, and hands every bucket back.
    public static void cakeTakesEveryMilk(GameTestHelper helper) {
        for (Item milk : List.of(Items.MILK_BUCKET, BarnyardItems.SHEEP_MILK_BUCKET, BarnyardItems.GOAT_MILK_BUCKET)) {
            CraftingInput input = CraftingInput.of(3, 3, List.of(
                    new ItemStack(milk), new ItemStack(milk), new ItemStack(milk),
                    new ItemStack(Items.SUGAR), new ItemStack(Items.EGG), new ItemStack(Items.SUGAR),
                    new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT)
            ));
            Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getServer().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
            helper.assertTrue(recipe.isPresent(), "no recipe takes " + milk);
            ItemStack result = recipe.get().value().assemble(input);
            helper.assertTrue(result.is(Items.CAKE), milk + " crafts " + result);
            NonNullList<ItemStack> remaining = recipe.get().value().getRemainingItems(input);
            long buckets = remaining.stream().filter(stack -> stack.is(Items.BUCKET)).count();
            helper.assertTrue(buckets == 3, milk + " leaves " + buckets + " buckets, not 3");
        }
        helper.succeed();
    }

    public static void milkBucketsAreInTheCommonTag(GameTestHelper helper) {
        for (Item milk : List.of(BarnyardItems.SHEEP_MILK_BUCKET, BarnyardItems.GOAT_MILK_BUCKET)) {
            helper.assertTrue(new ItemStack(milk).is(COMMON_MILK), milk + " is not in c:buckets/milk");
        }
        helper.succeed();
    }

    public static void goatMilkClearsEffects(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        ItemStack left = new ItemStack(BarnyardItems.GOAT_MILK_BUCKET).finishUsingItem(helper.getLevel(), player);

        helper.assertTrue(player.getActiveEffects().isEmpty(), "goat milk left " + player.getActiveEffects());
        helper.assertTrue(left.is(Items.BUCKET), "goat milk left " + left + " behind");
        helper.succeed();
    }

    public static void freshMotherCowGivesMilk(GameTestHelper helper) {
        assertMilks(helper, mother(helper, EntityTypes.COW, 0.5), Items.MILK_BUCKET);
        helper.succeed();
    }

    public static void goatGivesGoatMilk(GameTestHelper helper) {
        assertMilks(helper, mother(helper, EntityTypes.GOAT, 0.5), BarnyardItems.GOAT_MILK_BUCKET);
        helper.succeed();
    }

    public static void sheepGivesSheepMilk(GameTestHelper helper) {
        assertMilks(helper, mother(helper, EntityTypes.SHEEP, 0.5), BarnyardItems.SHEEP_MILK_BUCKET);
        helper.succeed();
    }

    // A male, a dry female and a small female already milked today all keep the empty bucket.
    public static void refusedMilkingKeepsTheBucket(GameTestHelper helper) {
        long now = helper.getLevel().getGameTime();
        Animal bull = BreedingGameTests.male(helper, EntityTypes.COW);
        Services.DATA.set(bull, BreedingGameTests.traits(helper, bull).withLactation(Lactation.NONE.afterBirth(now, now + 24_000L, 72_000L)));
        Animal dry = BreedingGameTests.female(helper, EntityTypes.COW);
        Animal small = mother(helper, EntityTypes.COW, 0.1);
        AnimalTraits smallTraits = BreedingGameTests.traits(helper, small);
        Services.DATA.set(small, smallTraits.withLactation(smallTraits.lactation().milked(now, 72_000L)));
        for (Animal cow : List.of(bull, dry, small)) {
            Lactation before = BreedingGameTests.traits(helper, cow).lactation();
            Player player = FoodGameTests.holding(helper, Items.BUCKET);
            cow.interact(player, InteractionHand.MAIN_HAND, cow.position());
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a refused milking left " + player.getMainHandItem());
            helper.assertTrue(BreedingGameTests.traits(helper, cow).lactation().equals(before), "a refused milking changed the lactation");
        }
        helper.succeed();
    }

    public static void largeCowMilksTwiceADay(GameTestHelper helper) {
        Animal cow = mother(helper, EntityTypes.COW, 0.7);
        assertMilks(helper, cow, Items.MILK_BUCKET);
        assertMilks(helper, cow, Items.MILK_BUCKET);
        Player third = FoodGameTests.holding(helper, Items.BUCKET);
        cow.interact(third, InteractionHand.MAIN_HAND, cow.position());
        helper.assertTrue(third.getMainHandItem().is(Items.BUCKET), "an L cow gave a third bucket today");
        helper.succeed();
    }

    public static void birthStartsMilk(GameTestHelper helper) {
        Animal mother = BreedingGameTests.female(helper, EntityTypes.COW);
        Animal father = BreedingGameTests.male(helper, EntityTypes.COW);
        long now = helper.getLevel().getGameTime();
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        Pregnancy pregnancy = BreedingGameTests.pregnancy(helper, mother);
        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the mother is still pregnant");
            Lactation lactation = BreedingGameTests.traits(helper, mother).lactation();
            long later = helper.getLevel().getGameTime();
            helper.assertTrue(lactation.hasMilk(later, BarnyardConfig.current().tuning().milkKeepsFlowingTicks()), "the mother has no milk after a birth");
            helper.assertTrue(lactation.youngUntil() > later, "the calf is not counted as young");
        });
    }

    public static void wildCowGetsOneMilkWindow(GameTestHelper helper) {
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        AnimalSex.onJoin(cow);
        Lactation first = BreedingGameTests.traits(helper, cow).lactation();
        helper.assertTrue(first.hasMilk(helper.getLevel().getGameTime(), BarnyardConfig.current().tuning().milkKeepsFlowingTicks()), "a wild cow got no milk window");
        AnimalSex.onJoin(cow);
        helper.assertTrue(BreedingGameTests.traits(helper, cow).lactation().equals(first), "a wild cow got a second milk window");
        helper.succeed();
    }

    public static void mooshroomBowlStaysVanilla(GameTestHelper helper) {
        Animal mooshroom = BreedingGameTests.female(helper, EntityTypes.MOOSHROOM);
        Player player = FoodGameTests.holding(helper, Items.BOWL);
        mooshroom.interact(player, InteractionHand.MAIN_HAND, mooshroom.position());
        helper.assertTrue(player.getMainHandItem().is(Items.MUSHROOM_STEW), "a dry mooshroom gave " + player.getMainHandItem());
        helper.succeed();
    }

    // A female that just gave birth, sized at a point of her species range: 0 is the smallest, 1 the largest.
    static Animal mother(GameTestHelper helper, EntityType<? extends Animal> type, double point) {
        Animal animal = BreedingGameTests.female(helper, type);
        SizeSettings size = BarnyardConfig.current().sizeFor(AnimalSex.speciesId(animal));
        double scale = size.femaleScale() - size.variation() + 2.0 * size.variation() * point;
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(animal, new AnimalTraits(Sex.FEMALE, scale, 1.0, -1L)
                .withLactation(Lactation.NONE.afterBirth(now, now + 24_000L, 72_000L)));
        return animal;
    }

    static void assertMilks(GameTestHelper helper, Animal animal, Item milk) {
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        animal.interact(player, InteractionHand.MAIN_HAND, animal.position());
        helper.assertTrue(player.getMainHandItem().is(milk), animal.getType() + " gave " + player.getMainHandItem() + ", not " + milk);
    }

    // Twice the base scale doubles leather; a male of twice his base gets his quarter on top.
    public static void sizedDropsFollowTheRule(GameTestHelper helper) {
        SizeSettings size = BarnyardConfig.current().sizeFor("minecraft:cow");
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, 2.0 * size.femaleScale(), 1.0, -1L));
        Animal bull = BreedingGameTests.male(helper, EntityTypes.COW);
        Services.DATA.set(bull, new AnimalTraits(Sex.MALE, 2.0 * size.maleScale(), 1.0, -1L));

        int cowLeather = ProductDrops.resize(cow, new ItemStack(Items.LEATHER, 2), cow.getRandom()).getCount();
        int bullLeather = ProductDrops.resize(bull, new ItemStack(Items.LEATHER, 2), bull.getRandom()).getCount();
        int sticks = ProductDrops.resize(cow, new ItemStack(Items.STICK, 2), cow.getRandom()).getCount();

        helper.assertTrue(cowLeather == 4, "a cow of twice her base scale dropped " + cowLeather + " leather, not 4");
        helper.assertTrue(bullLeather == 5, "a bull of twice his base scale dropped " + bullLeather + " leather, not 5");
        helper.assertTrue(sticks == 2, "a stick drop was resized to " + sticks);
        helper.succeed();
    }

    // Beef still follows the meat curve for a base-scale female, whose sex and age factor is 1.
    public static void meatKeepsItsCurve(GameTestHelper helper) {
        SizeSettings size = BarnyardConfig.current().sizeFor("minecraft:cow");
        SizeSettings range = BarnyardConfig.current().defaultSize();
        Animal cow = BreedingGameTests.female(helper, EntityTypes.COW);
        Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, size.femaleScale(), 1.0, -1L));
        int lowest = MeatDrop.count(size.femaleScale(), range.smallest(), range.largest(), 0.0);
        int highest = MeatDrop.count(size.femaleScale(), range.smallest(), range.largest(), 0.999);

        for (int i = 0; i < 20; i++) {
            int beef = ProductDrops.resize(cow, new ItemStack(Items.BEEF, 2), cow.getRandom()).getCount();
            helper.assertTrue(beef >= lowest && beef <= highest, "a base-scale cow dropped " + beef + " beef, outside " + lowest + "-" + highest);
        }
        helper.succeed();
    }

    // Four times the base scale gives at least four wool, which vanilla's 1-3 never reaches.
    public static void bigSheepShearMoreWool(GameTestHelper helper) {
        SizeSettings size = BarnyardConfig.current().sizeFor("minecraft:sheep");
        Sheep sheep = (Sheep) BreedingGameTests.female(helper, EntityTypes.SHEEP);
        Services.DATA.set(sheep, new AnimalTraits(Sex.FEMALE, 4.0 * size.femaleScale(), 1.0, -1L));

        sheep.shear(helper.getLevel(), SoundSource.PLAYERS, new ItemStack(Items.SHEARS));

        int wool = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(sheep.blockPosition()).inflate(4.0)).stream()
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
        helper.assertTrue(wool >= 4, "a sheep of four times her base scale gave " + wool + " wool");
        helper.succeed();
    }
}
