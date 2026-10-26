package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Optional;

// In-game checks for milk, sized drops, shearing and taming carnivores, run by each loader's game test server.
public final class ProductGameTests {
    private static final TagKey<Item> COMMON_MILK = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "buckets/milk"));

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("old_traits_load_without_lactation", 20, ProductGameTests::oldTraitsLoadWithoutLactation),
            new BreedingGameTests.Case("cake_takes_every_milk", 20, ProductGameTests::cakeTakesEveryMilk),
            new BreedingGameTests.Case("milk_buckets_are_in_the_common_tag", 20, ProductGameTests::milkBucketsAreInTheCommonTag),
            new BreedingGameTests.Case("goat_milk_clears_effects", 20, ProductGameTests::goatMilkClearsEffects)
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
}
