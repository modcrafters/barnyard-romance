package net.modcrafters.minecraft.barnyardromance;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The import rule between core, features and hooks: core imports only core; a feature's api imports core and other
// features' api; a feature's implementation imports core, itself and other features' api; hooks import anything.
// MIGRATING says where today's packages are headed and ALLOWED lists today's exceptions; both only shrink, and end empty.
class FeatureBoundaryScanTest {
    private static final Set<String> FEATURES = Set.of("familiarity", "breeding", "pregnancy", "eggs", "feed", "food",
            "milk", "shear", "vigour", "rivalry", "drops", "herd", "stats", "advancement", "naturalist");
    private static final Set<String> HOOK_PACKAGES = Set.of("mixin", "mixin/client", "client", "look", "command", "compat/jade");
    // The composition root, in the mod's root package.
    private static final Set<String> HOOK_CLASSES = Set.of("BarnyardRomance", "BarnyardFeatures");
    // Mixin accessors stay where Mixin needs them but count as core.
    private static final Set<String> CORE_CLASSES_IN_HOOK_PACKAGES = Set.of("mixin/AnimalEating",
            "mixin/AnimalLoveCauseAccessor", "mixin/OcelotAccessor", "mixin/MobGoalsAccessor");
    // An old package, or package/Class, to the zone it will belong to. Class entries win over package entries.
    private static final Map<String, String> MIGRATING = Map.ofEntries(
            Map.entry("Constants", "core"),
            Map.entry("ServerScoped", "core"),
            Map.entry("BarnyardRegistrations", "core"),
            Map.entry("platform", "core"),
            Map.entry("platform/services", "core"),
            Map.entry("data", "core"),
            Map.entry("config", "core"),
            Map.entry("text", "core"),
            Map.entry("age", "core"),
            Map.entry("sex/Sex", "core"),
            Map.entry("sex/SizeClass", "core"),
            Map.entry("sex/Lineage", "core"),
            Map.entry("sex/AnimalSex", "core"),
            Map.entry("sex/AnimalTraits", "core"),
            Map.entry("sex/AnimalTraitCodecs", "core"),
            Map.entry("sex/Growth", "core"),
            Map.entry("sex/SizeSettings", "core"),
            Map.entry("sex", "breeding"),
            Map.entry("compat/VersionCompat", "core"),
            Map.entry("compat/JadeBridge", "hook"),
            Map.entry("compat", "naturalist"),
            Map.entry("item/BarnyardTab", "core"),
            Map.entry("item/BarnyardItems", "core"),
            Map.entry("item/BarnyardComponents", "core"),
            Map.entry("item/DairyMashItem", "milk"),
            Map.entry("item", "herd"),
            Map.entry("nest/BarnyardBlocks", "core"),
            Map.entry("nest/NestCompat", "naturalist"),
            Map.entry("nest", "eggs"),
            Map.entry("network", "herd"),
            Map.entry("command/HerdTool", "herd"),
            Map.entry("command/HerdToolRules", "herd"),
            Map.entry("client/BarnyardParticles", "core"),
            Map.entry("food/PregnancyCareActions", "pregnancy"),
            Map.entry("familiarity/FamiliarShearing", "shear")
    );
    // "<file> -> <imported class>" to the reason it's let through for now. Every reason starts with "migration:".
    private static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("BarnyardRegistrations -> feed/FeederKind", "migration: registers feed's content until feed registers its own"),
            Map.entry("BarnyardRegistrations -> item/HerdBookCopyRecipe", "migration: registers herd's content until herd registers its own"),
            Map.entry("advancement/BarnyardAdvancements -> familiarity/Familiarity", "migration: advancement uses familiarity's internals until familiarity has an api"),
            Map.entry("advancement/BarnyardAdvancements -> familiarity/FamiliarityCare", "migration: advancement uses familiarity's internals until familiarity has an api"),
            Map.entry("advancement/FamilyTree -> herd/HerdBookData", "migration: advancement uses herd's internals until herd has an api"),
            Map.entry("advancement/FamilyTree -> herd/HerdLedger", "migration: advancement uses herd's internals until herd has an api"),
            Map.entry("config/BarnyardConfig -> compat/NaturalistCompat", "migration: naturalist hands its default ignore list to config instead"),
            Map.entry("data/EntityDataService -> eggs/Laying", "migration: the attachment becomes generic, registered by eggs"),
            Map.entry("data/EntityDataService -> pregnancy/Pregnancy", "migration: the attachment becomes generic, registered by pregnancy"),
            Map.entry("eggs/EggLaying -> familiarity/Familiarity", "migration: eggs uses familiarity's internals until familiarity has an api"),
            Map.entry("eggs/EggLaying -> familiarity/FamiliarityCare", "migration: eggs uses familiarity's internals until familiarity has an api"),
            Map.entry("eggs/EggLaying -> pregnancy/LitterRoll", "migration: eggs uses pregnancy's internals until pregnancy has an api"),
            Map.entry("eggs/EggLaying -> rivalry/Reach", "migration: eggs uses rivalry's internals until rivalry has an api"),
            Map.entry("eggs/EggLaying -> vigour/VigourActions", "migration: eggs uses vigour's internals until vigour has an api"),
            Map.entry("eggs/Hatching -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("eggs/Hatching -> herd/HerdBooks", "migration: eggs uses herd's internals until herd has an api"),
            Map.entry("eggs/Hatching -> sex/Offspring", "migration: eggs uses breeding's internals until breeding has an api"),
            Map.entry("eggs/Hatching -> sex/ScaleRoll", "migration: eggs uses breeding's internals until breeding has an api"),
            Map.entry("eggs/Hatching -> sex/SexRoll", "migration: eggs uses breeding's internals until breeding has an api"),
            Map.entry("eggs/ThrownEggs -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("eggs/ThrownEggs -> sex/ScaleRoll", "migration: eggs uses breeding's internals until breeding has an api"),
            Map.entry("eggs/ThrownEggs -> sex/SexRoll", "migration: eggs uses breeding's internals until breeding has an api"),
            Map.entry("eggs/WildEggs -> pregnancy/LitterRoll", "migration: eggs uses pregnancy's internals until pregnancy has an api"),
            Map.entry("familiarity/Escapes -> rivalry/Reach", "migration: familiarity uses rivalry's internals until rivalry has an api"),
            Map.entry("familiarity/FamiliarityActions -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("familiarity/FamiliarityActions -> compat/NaturalistCompat", "migration: familiarity uses naturalist's internals until naturalist has an api"),
            Map.entry("familiarity/FamiliarityActions -> eggs/EggLaying", "migration: familiarity uses eggs' internals until eggs has an api"),
            Map.entry("familiarity/FamiliarityActions -> food/Feed", "migration: familiarity uses food's internals until food has an api"),
            Map.entry("familiarity/FamiliarityActions -> food/FoodData", "migration: familiarity uses food's internals until food has an api"),
            Map.entry("familiarity/FamiliarityActions -> food/FoodHeal", "migration: familiarity uses food's internals until food has an api"),
            Map.entry("familiarity/FamiliarityActions -> food/MinecraftItemFacts", "migration: familiarity uses food's internals until food has an api"),
            Map.entry("familiarity/FamiliarityActions -> food/PregnancyCareActions", "migration: familiarity uses pregnancy's internals until pregnancy has an api"),
            Map.entry("familiarity/FamiliarityActions -> milk/MilkActions", "migration: familiarity uses milk's internals until milk has an api"),
            Map.entry("familiarity/FamiliarityActions -> vigour/VigourActions", "migration: familiarity uses vigour's internals until vigour has an api"),
            Map.entry("familiarity/FamiliarityCare -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("familiarity/FamiliarityCare -> feed/FeederIndex", "migration: familiarity uses feed's internals until feed has an api"),
            Map.entry("familiarity/FamiliarityCare -> herd/HerdBooks", "migration: familiarity uses herd's internals until herd has an api"),
            Map.entry("familiarity/FamiliarityCare -> vigour/Vigour", "migration: familiarity uses vigour's internals until vigour has an api"),
            Map.entry("familiarity/FamiliarityCare -> vigour/VigourRules", "migration: familiarity uses vigour's internals until vigour has an api"),
            Map.entry("feed/EatAtFeederGoal -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("feed/FeederCatchUp -> familiarity/DailyUpkeep", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederCatchUp -> familiarity/Familiarity", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederCatchUp -> familiarity/FamiliarityCare", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederCatchUp -> familiarity/Hunger", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederFoods -> food/Feed", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/FeederFoods -> food/FoodData", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/FeederMeal -> familiarity/Familiarity", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederMeal -> familiarity/FamiliarityCare", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/FeederMeal -> food/Feed", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/FeederMeal -> food/FoodData", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/FeederMeal -> food/FoodHeal", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/FeederMeal -> food/MinecraftItemFacts", "migration: feed uses food's internals until food has an api"),
            Map.entry("feed/HerdRefresh -> familiarity/Familiarity", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/HerdRefresh -> familiarity/Upkeep", "migration: feed uses familiarity's internals until familiarity has an api"),
            Map.entry("feed/HerdRefresh -> rivalry/Reach", "migration: feed uses rivalry's internals until rivalry has an api"),
            Map.entry("feed/MorningFeeding -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("food/FoodData -> feed/FeederFoods", "migration: food uses feed's internals until feed has an api"),
            Map.entry("food/PregnancyCareActions -> familiarity/FamiliarityCare", "migration: pregnancy uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdBookData -> pregnancy/PregnancyCodecs", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdBooks -> familiarity/Familiarity", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdBooks -> nest/NestingBoxes", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdBooks -> rivalry/Fights", "migration: herd uses rivalry's internals until rivalry has an api"),
            Map.entry("herd/HerdEntry -> eggs/EggOutlook", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdEntry -> stats/StatKind", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdRecord -> pregnancy/Pregnancy", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdRecords -> eggs/EggLaying", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdRecords -> eggs/Laying", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdRecords -> familiarity/Familiarity", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdRecords -> familiarity/FamiliarityCare", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdRecords -> milk/MilkActions", "migration: herd uses milk's internals until milk has an api"),
            Map.entry("herd/HerdRecords -> pregnancy/Pregnancy", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdRecords -> stats/LiveFacts", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdStatus -> eggs/EggOutlook", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdStatus -> eggs/Laying", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/HerdStatus -> familiarity/Familiarity", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdStatus -> familiarity/Hunger", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdStatus -> familiarity/WildCaught", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("herd/HerdStatus -> milk/MilkActions", "migration: herd uses milk's internals until milk has an api"),
            Map.entry("herd/HerdStatus -> milk/MilkStatus", "migration: herd uses milk's internals until milk has an api"),
            Map.entry("herd/HerdStatus -> pregnancy/Fertility", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdStatus -> pregnancy/Pregnancy", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdStatus -> pregnancy/PregnancyClock", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdStatus -> pregnancy/PregnancyText", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("herd/HerdStatus -> shear/ShearRule", "migration: herd uses shear's internals until shear has an api"),
            Map.entry("herd/HerdStatus -> stats/AnimalFacts", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdStatus -> stats/LiveFacts", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdStatus -> stats/StatSheet", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdStatus -> stats/StatText", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("herd/HerdStatus -> vigour/VigourRules", "migration: herd uses vigour's internals until vigour has an api"),
            Map.entry("herd/NestCheck -> eggs/RoosterNear", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("herd/RecordSheet -> eggs/Laying", "migration: herd uses eggs' internals until eggs has an api"),
            Map.entry("item/BarnyardComponents -> command/HerdTool", "migration: registry content that moves to herd"),
            Map.entry("item/BarnyardComponents -> eggs/EggData", "migration: registry content that moves to eggs"),
            Map.entry("item/BarnyardComponents -> eggs/WildEgg", "migration: registry content that moves to eggs"),
            Map.entry("item/BarnyardItems -> eggs/FertilizedEggItem", "migration: registry content that moves to eggs"),
            Map.entry("item/BarnyardTab -> command/HerdTool", "migration: tab order names herd's content until herd registers it"),
            Map.entry("item/BarnyardTab -> feed/FeederKind", "migration: tab order names feed's content until feed registers it"),
            Map.entry("item/HerdBookItem -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("item/HerdStick -> command/AnimalInfoText", "migration: herd calls a command helper until it moves out of the hook"),
            Map.entry("item/HerdStick -> command/DebugSteps", "migration: herd calls a command helper until it moves out of the hook"),
            Map.entry("item/HerdStick -> familiarity/Familiarity", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("item/HerdStick -> familiarity/FamiliarityActions", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("item/HerdStick -> familiarity/FamiliarityCare", "migration: herd uses familiarity's internals until familiarity has an api"),
            Map.entry("item/HerdStick -> milk/MilkActions", "migration: herd uses milk's internals until milk has an api"),
            Map.entry("item/HerdStick -> pregnancy/Pregnancy", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("item/HerdStick -> pregnancy/PregnancyClock", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("item/HerdStick -> pregnancy/PregnancyText", "migration: herd uses pregnancy's internals until pregnancy has an api"),
            Map.entry("item/HerdStick -> stats/StatText", "migration: herd uses stats' internals until stats has an api"),
            Map.entry("milk/MilkActions -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("milk/MilkActions -> familiarity/Familiarity", "migration: milk uses familiarity's internals until familiarity has an api"),
            Map.entry("milk/MilkActions -> familiarity/FamiliarityActions", "migration: milk uses familiarity's internals until familiarity has an api"),
            Map.entry("milk/MilkActions -> familiarity/FamiliarityCare", "migration: milk uses familiarity's internals until familiarity has an api"),
            Map.entry("milk/MilkActions -> familiarity/Refusal", "migration: milk uses familiarity's internals until familiarity has an api"),
            Map.entry("milk/MilkActions -> food/Feed", "migration: milk uses food's internals until food has an api"),
            Map.entry("milk/MilkActions -> food/FoodHeal", "migration: milk uses food's internals until food has an api"),
            Map.entry("milk/MilkActions -> food/MinecraftItemFacts", "migration: milk uses food's internals until food has an api"),
            Map.entry("nest/BarnyardBlocks -> feed/BirdFeederBlock", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/FeederBlock", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/FeederBlockEntity", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/FeederKind", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/FeederMenu", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/GrainFeederBlock", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/MangerBlock", "migration: registry content that moves to feed"),
            Map.entry("nest/BarnyardBlocks -> feed/PetBowlBlock", "migration: registry content that moves to feed"),
            Map.entry("pregnancy/MatingGate -> familiarity/Familiarity", "migration: pregnancy uses familiarity's internals until familiarity has an api"),
            Map.entry("pregnancy/MatingGate -> rivalry/Courtship", "migration: pregnancy uses rivalry's internals until rivalry has an api"),
            Map.entry("pregnancy/PregnancyController -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("pregnancy/PregnancyController -> familiarity/Familiarity", "migration: pregnancy uses familiarity's internals until familiarity has an api"),
            Map.entry("pregnancy/PregnancyController -> herd/HerdBooks", "migration: pregnancy uses herd's internals until herd has an api"),
            Map.entry("pregnancy/PregnancyController -> rivalry/Courtship", "migration: pregnancy uses rivalry's internals until rivalry has an api"),
            Map.entry("pregnancy/PregnancyController -> sex/Offspring", "migration: pregnancy uses breeding's internals until breeding has an api"),
            Map.entry("pregnancy/PregnancyController -> sex/ScaleRoll", "migration: pregnancy uses breeding's internals until breeding has an api"),
            Map.entry("pregnancy/PregnancyController -> sex/SexRoll", "migration: pregnancy uses breeding's internals until breeding has an api"),
            Map.entry("pregnancy/VanillaBirth -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("pregnancy/VanillaBirth -> herd/HerdBooks", "migration: pregnancy uses herd's internals until herd has an api"),
            Map.entry("rivalry/Courtship -> familiarity/Familiarity", "migration: rivalry uses familiarity's internals until familiarity has an api"),
            Map.entry("rivalry/Courtship -> familiarity/FamiliarityCare", "migration: rivalry uses familiarity's internals until familiarity has an api"),
            Map.entry("rivalry/Courtship -> vigour/VigourActions", "migration: rivalry uses vigour's internals until vigour has an api"),
            Map.entry("rivalry/Fights -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("rivalry/Fights -> familiarity/Familiarity", "migration: rivalry uses familiarity's internals until familiarity has an api"),
            Map.entry("rivalry/Fights -> food/FoodData", "migration: rivalry uses food's internals until food has an api"),
            Map.entry("rivalry/RivalRolls -> eggs/EggSchedule", "migration: rivalry uses eggs' internals until eggs has an api"),
            Map.entry("rivalry/RivalRolls -> familiarity/Familiarity", "migration: rivalry uses familiarity's internals until familiarity has an api"),
            Map.entry("rivalry/RivalRolls -> familiarity/FamiliarityCare", "migration: rivalry uses familiarity's internals until familiarity has an api"),
            Map.entry("sex/AnimalSex -> milk/MilkActions", "migration: milk's join step becomes an animal-joined hook"),
            Map.entry("sex/AnimalTraitCodecs -> familiarity/Hunger", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraitCodecs -> familiarity/Origin", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraitCodecs -> familiarity/Refusals", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraitCodecs -> familiarity/Upkeep", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraitCodecs -> milk/Lactation", "migration: milk's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraitCodecs -> vigour/Vigour", "migration: vigour's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> familiarity/Hunger", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> familiarity/Origin", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> familiarity/Refusals", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> familiarity/Upkeep", "migration: familiarity's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> milk/Lactation", "migration: milk's fields become its slice of the animal record"),
            Map.entry("sex/AnimalTraits -> vigour/Vigour", "migration: vigour's fields become its slice of the animal record"),
            Map.entry("sex/Offspring -> familiarity/Familiarity", "migration: breeding uses familiarity's internals until familiarity has an api"),
            Map.entry("sex/Offspring -> familiarity/Origin", "migration: breeding uses familiarity's internals until familiarity has an api"),
            Map.entry("shear/ShearActions -> advancement/BarnyardAdvancements", "migration: advancement listens to events instead"),
            Map.entry("shear/ShearActions -> familiarity/Familiarity", "migration: shear uses familiarity's internals until familiarity has an api"),
            Map.entry("shear/ShearActions -> familiarity/FamiliarityActions", "migration: shear uses familiarity's internals until familiarity has an api"),
            Map.entry("shear/ShearActions -> familiarity/FamiliarityCare", "migration: shear uses familiarity's internals until familiarity has an api"),
            Map.entry("shear/ShearActions -> familiarity/Refusal", "migration: shear uses familiarity's internals until familiarity has an api"),
            Map.entry("shear/ShearRule -> herd/ShearState", "migration: shear uses herd's internals until herd has an api"),
            Map.entry("stats/AnimalFacts -> eggs/EggOutlook", "migration: stats uses eggs' internals until eggs has an api"),
            Map.entry("stats/AnimalFacts -> herd/Familiar", "migration: stats uses herd's internals until herd has an api"),
            Map.entry("stats/AnimalFacts -> herd/ShearState", "migration: stats uses herd's internals until herd has an api"),
            Map.entry("stats/LiveFacts -> eggs/EggLaying", "migration: stats uses eggs' internals until eggs has an api"),
            Map.entry("stats/LiveFacts -> eggs/Laying", "migration: stats uses eggs' internals until eggs has an api"),
            Map.entry("stats/LiveFacts -> familiarity/Familiarity", "migration: stats uses familiarity's internals until familiarity has an api"),
            Map.entry("stats/LiveFacts -> familiarity/FamiliarityCare", "migration: stats uses familiarity's internals until familiarity has an api"),
            Map.entry("stats/LiveFacts -> herd/Familiar", "migration: stats uses herd's internals until herd has an api"),
            Map.entry("stats/LiveFacts -> milk/MilkActions", "migration: stats uses milk's internals until milk has an api"),
            Map.entry("stats/LiveFacts -> pregnancy/Fertility", "migration: stats uses pregnancy's internals until pregnancy has an api"),
            Map.entry("stats/LiveFacts -> pregnancy/Pregnancy", "migration: stats uses pregnancy's internals until pregnancy has an api"),
            Map.entry("stats/LiveFacts -> pregnancy/PregnancyClock", "migration: stats uses pregnancy's internals until pregnancy has an api"),
            Map.entry("stats/LiveFacts -> shear/ShearRule", "migration: stats uses shear's internals until shear has an api"),
            Map.entry("stats/LiveFacts -> vigour/VigourRules", "migration: stats uses vigour's internals until vigour has an api"),
            Map.entry("stats/StatSheet -> familiarity/Familiarity", "migration: stats uses familiarity's internals until familiarity has an api"),
            Map.entry("stats/StatSheet -> pregnancy/PregnancyText", "migration: stats uses pregnancy's internals until pregnancy has an api"),
            Map.entry("stats/StatText -> familiarity/Familiarity", "migration: stats uses familiarity's internals until familiarity has an api"),
            Map.entry("stats/StatText -> familiarity/Upkeep", "migration: stats uses familiarity's internals until familiarity has an api"),
            Map.entry("stats/StatText -> milk/MilkStatus", "migration: stats uses milk's internals until milk has an api"),
            Map.entry("vigour/VigourActions -> familiarity/Familiarity", "migration: vigour uses familiarity's internals until familiarity has an api"),
            Map.entry("vigour/VigourActions -> familiarity/FamiliarityCare", "migration: vigour uses familiarity's internals until familiarity has an api"),
            Map.entry("vigour/VigourActions -> food/Feed", "migration: vigour uses food's internals until food has an api"),
            Map.entry("vigour/VigourActions -> food/PregnancyCareActions", "migration: vigour uses pregnancy's internals until pregnancy has an api")
    );
    // Files one version adds rather than overrides, as <version>/<path>; anything else under versions/ needs a main file.
    private static final Set<String> VERSION_ONLY = Set.of(
            "1.21.1/mixin/client/ChickenModelAccessor",
            "1.21.1/mixin/client/QuadrupedModelAccessor",
            "26.3/mixin/client/LivingEntityRenderStateMixin",
            "26.3/mixin/client/ModelFeatureRendererMixin"
    );
    private static final String MOD_PACKAGE = "net.modcrafters.minecraft.barnyardromance.";
    private static final Pattern IMPORT = Pattern.compile(
            "^\\s*import\\s+(?:static\\s+)?" + Pattern.quote(MOD_PACKAGE) + "([\\w.*]+)\\s*;");

    @Test
    void everyImportKeepsToTheRule() {
        Scan scan = scan();
        assertTrue(scan.unclassified.isEmpty(), () -> "add these to FEATURES, HOOK_PACKAGES or MIGRATING:\n"
                + String.join("\n", scan.unclassified));
        List<String> unlisted = scan.violations.entrySet().stream().filter(violation -> !ALLOWED.containsKey(violation.getKey()))
                .map(violation -> violation.getKey() + "   (" + violation.getValue() + ")").toList();
        assertTrue(unlisted.isEmpty(), () -> unlisted.size() + " imports break the rule between core, features and hooks:\n"
                + String.join("\n", unlisted));
    }

    @Test
    void everyAllowanceIsStillNeeded() {
        Scan scan = scan();
        Path root = SourceScan.repoRoot();
        List<String> stale = new ArrayList<>();
        ALLOWED.forEach((allowance, reason) -> {
            if (!scan.violations.containsKey(allowance)) {
                stale.add("ALLOWED " + allowance + " no longer breaks the rule");
            }
            if (!reason.startsWith("migration:")) {
                stale.add("ALLOWED " + allowance + " needs a reason starting \"migration:\"");
            }
        });
        for (String old : MIGRATING.keySet()) {
            if (!existsInMain(root, old)) {
                stale.add("MIGRATING " + old + " is no longer a package or class");
            }
        }
        assertTrue(stale.isEmpty(), () -> String.join("\n", stale));
    }

    @Test
    void everyVersionOverrideHasAMainFile() {
        Path root = SourceScan.repoRoot();
        Path main = root.resolve("src/main/java");
        List<String> orphans = new ArrayList<>();
        Set<String> versionOnly = new TreeSet<>();
        for (Path sources : SourceScan.versionSources(root)) {
            String version = root.resolve("versions").relativize(sources).getName(0).toString();
            for (Path file : SourceScan.javaFiles(sources)) {
                if (Files.exists(main.resolve(sources.relativize(file)))) {
                    continue;
                }
                String name = version + "/" + SourceScan.modPath(sources, file);
                if (VERSION_ONLY.contains(name)) {
                    versionOnly.add(name);
                } else {
                    orphans.add(name);
                }
            }
        }
        assertTrue(orphans.isEmpty(), () -> "these overrides no longer override anything; move them with their main file:\n"
                + String.join("\n", orphans));
        assertTrue(versionOnly.containsAll(VERSION_ONLY), () -> "these VERSION_ONLY entries are gone or now have a main file: "
                + VERSION_ONLY.stream().filter(name -> !versionOnly.contains(name)).toList());
    }

    // Each violation, "<file> -> <imported class>", to the zones it crosses.
    private record Scan(Map<String, String> violations, Set<String> unclassified) {
    }

    // A zone is core, hook, or a feature; a feature's api subpackage is its own zone for the rule.
    private record Zone(String name, boolean api) {
        static final Zone CORE = new Zone("core", false);
        static final Zone HOOK = new Zone("hook", false);

        static Zone named(String name) {
            return switch (name) {
                case "core" -> CORE;
                case "hook" -> HOOK;
                default -> {
                    if (!FEATURES.contains(name)) {
                        throw new IllegalStateException("MIGRATING names an unknown zone " + name);
                    }
                    yield new Zone(name, false);
                }
            };
        }

        boolean mayImport(Zone imported) {
            if (this.equals(HOOK) || imported.equals(CORE)) {
                return true;
            }
            if (imported.equals(HOOK) || this.equals(CORE)) {
                return false;
            }
            if (api) {
                return imported.api;
            }
            return imported.api || imported.name.equals(name);
        }

        @Override
        public String toString() {
            return api ? name + " api" : name;
        }
    }

    private static Scan scan() {
        Path root = SourceScan.repoRoot();
        List<Path> dirs = new ArrayList<>(List.of(root.resolve("src/main/java")));
        dirs.addAll(SourceScan.versionSources(root));
        Map<String, String> violations = new TreeMap<>();
        Set<String> unclassified = new TreeSet<>();
        for (Path sources : dirs) {
            for (Path file : SourceScan.javaFiles(sources)) {
                String name = SourceScan.modPath(sources, file);
                Zone from = zoneOf(name);
                if (from == null) {
                    unclassified.add(unclassifiedMessage(name));
                    continue;
                }
                for (String line : SourceScan.lines(file)) {
                    Matcher matcher = IMPORT.matcher(line);
                    if (!matcher.find()) {
                        continue;
                    }
                    String imported = importedPath(matcher.group(1));
                    Zone to = zoneOf(imported);
                    if (to == null) {
                        unclassified.add(unclassifiedMessage(imported));
                    } else if (!from.mayImport(to)) {
                        violations.put(name + " -> " + imported, from + " may not import " + to);
                    }
                }
            }
        }
        return new Scan(violations, unclassified);
    }

    // The imported top-level class as a path, e.g. sex/AnimalTraits for sex.AnimalTraits.Inner or a static import
    // from it; a package wildcard keeps its *, e.g. sex/*.
    private static String importedPath(String dotted) {
        StringBuilder path = new StringBuilder();
        for (String segment : dotted.split("\\.")) {
            if (!path.isEmpty()) {
                path.append('/');
            }
            path.append(segment);
            if (segment.equals("*") || Character.isUpperCase(segment.charAt(0))) {
                break;
            }
        }
        return path.toString();
    }

    private static Zone zoneOf(String path) {
        int slash = path.lastIndexOf('/');
        String pkg = slash < 0 ? "" : path.substring(0, slash);
        String top = pkg.contains("/") ? pkg.substring(0, pkg.indexOf('/')) : pkg;
        if (CORE_CLASSES_IN_HOOK_PACKAGES.contains(path)) {
            return Zone.CORE;
        }
        if (pkg.isEmpty() && HOOK_CLASSES.contains(path)) {
            return Zone.HOOK;
        }
        if (MIGRATING.containsKey(path)) {
            return Zone.named(MIGRATING.get(path));
        }
        if (MIGRATING.containsKey(pkg)) {
            return Zone.named(MIGRATING.get(pkg));
        }
        if (HOOK_PACKAGES.contains(pkg)) {
            return Zone.HOOK;
        }
        if (top.equals("core")) {
            return Zone.CORE;
        }
        if (FEATURES.contains(top)) {
            return new Zone(top, pkg.equals(top + "/api") || pkg.startsWith(top + "/api/"));
        }
        return null;
    }

    private static String unclassifiedMessage(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? "unclassified class " + path + " in the root package" : "unclassified package " + path.substring(0, slash);
    }

    private static boolean existsInMain(Path root, String old) {
        Path main = root.resolve("src/main/java/" + MOD_PACKAGE.replace('.', '/'));
        return Files.isDirectory(main.resolve(old)) || Files.exists(main.resolve(old + ".java"));
    }
}
