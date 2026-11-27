plugins {
    id("dev.kikugie.stonecutter")
    // see https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml for new versions
    id("net.fabricmc.fabric-loom") version "1.18.1" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "2.0.147" apply false
    // The same plugin for Forge 1.20.1, which NeoForm doesn't cover: it runs Forge and plain Minecraft through MCP.
    id("net.neoforged.moddev.legacyforge") version "2.0.147" apply false
}

stonecutter active "26.3"

// Lets code check the loader with `//? if fabric`, `//? if neoforge` or `//? if forge`.
stonecutter parameters {
    constants.match(branch.id.ifEmpty { "common" }, "common", "fabric", "neoforge", "forge")

    // The source is written for 26.3; older versions get the old names back. Each replacement turns the
    // first name into the second at and after its version, and back again before it. The version thresholds
    // are only checked against 1.21.1 and 26.3: confirm them before adding a version in between.
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            // ResourceKey.location() was renamed with it. Other classes still have a location() on 26.3; CI's
            // switch round trip fails if shared code ever calls one.
            replace(".location()", ".identifier()")
        }
        // The overworld clock: one day-time counter on 1.21.1 (every dimension reads the overworld's).
        string(current.parsed >= "26.1") {
            replace("getDayTime()", "getOverworldClockTime()")
        }
        // A menu click's kind.
        string(current.parsed >= "26.1") {
            replace("net.minecraft.world.inventory.ClickType;", "net.minecraft.world.inventory.ContainerInput;")
            replace("ClickType.", "ContainerInput.")
        }
        // Vanilla animals moved into one package per animal.
        string(current.parsed >= "26.1") {
            val animal = "net.minecraft.world.entity.animal"
            for ((type, pkg) in mapOf("Cat" to "feline", "Ocelot" to "feline", "Sheep" to "sheep", "Wolf" to "wolf", "MushroomCow" to "cow", "Chicken" to "chicken"))
                replace("$animal.$type;", "$animal.$pkg.$type;")
            for (type in listOf("AbstractHorse", "Donkey", "Horse", "Llama", "TraderLlama"))
                replace("$animal.horse.$type;", "$animal.equine.$type;")
            replace("net.minecraft.world.entity.projectile.ThrownEgg;", "net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;")
            replace("net.minecraft.world.entity.projectile.ThrowableItemProjectile;", "net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;")
            // The EntityType constants moved to EntityTypes. The import keeps a marker so it can turn back.
            replace("import net.minecraft.world.entity.EntityType /* EntityTypes */;", "import net.minecraft.world.entity.EntityTypes;")
        }
        regex(current.parsed >= "26.1") {
            replace(
                // EntityType keeps its own constants (CODEC, STREAM_CODEC, NO_UPDATE_INTERVAL).
                """\bEntityType\.(?!(?:CODEC|STREAM_CODEC|NO_UPDATE_INTERVAL)\b)([A-Z][A-Z0-9_]*)\b""", "EntityTypes.\$1",
                """\bEntityTypes\.([A-Z][A-Z0-9_]*)\b""", "EntityType.\$1"
            )
            // GUI drawing: screens fill a GuiGraphicsExtractor instead of drawing into GuiGraphics.
            replace("""\bGuiGraphics\b""", "GuiGraphicsExtractor", """\bGuiGraphicsExtractor\b""", "GuiGraphics")
            replace("""\bgraphics\.drawString\(""", "graphics.text(", """\bgraphics\.text\(""", "graphics.drawString(")
            replace(
                """\brenderEntityInInventoryFollowsMouse\b""", "extractEntityInInventoryFollowsMouse",
                """\bextractEntityInInventoryFollowsMouse\b""", "renderEntityInInventoryFollowsMouse"
            )
        }
        // 1.20.1 has no stream codecs, custom payloads or item components (all 1.20.5), so it imports the mod's own
        // stand-ins of the same shape from compat.legacy, which only the 1.20.1 build has.
        string(current.parsed >= "1.20.5") {
            val legacy = "net.modcrafters.minecraft.barnyardromance.compat.legacy"
            replace("$legacy.StreamCodec;", "net.minecraft.network.codec.StreamCodec;")
            replace("$legacy.ByteBufCodecs;", "net.minecraft.network.codec.ByteBufCodecs;")
            replace("$legacy.RegistryFriendlyByteBuf;", "net.minecraft.network.RegistryFriendlyByteBuf;")
            replace("$legacy.CustomPacketPayload;", "net.minecraft.network.protocol.common.custom.CustomPacketPayload;")
            replace("$legacy.DataComponentType;", "net.minecraft.core.component.DataComponentType;")
            replace("$legacy.ComponentSerialization;", "net.minecraft.network.chat.ComponentSerialization;")
            // UUIDUtil's stream codec, which 1.20.1's UUIDUtil doesn't have.
            replace("$legacy.LegacyStreamCodecs.UUID", "UUIDUtil.STREAM_CODEC")
            // A container that keeps its slots in a list, as 1.20.5's BaseContainerBlockEntity does.
            replace("$legacy.BaseContainerBlockEntity;", "net.minecraft.world.level.block.entity.BaseContainerBlockEntity;")
            // The game tests' own stand-ins (versions/1.20.1/src/gametest): a crafting grid, and a recipe with its id.
            val gametestLegacy = "net.modcrafters.minecraft.barnyardromance.gametest.legacy"
            replace("$gametestLegacy.CraftingInput;", "net.minecraft.world.item.crafting.CraftingInput;")
            replace("$gametestLegacy.RecipeHolder;", "net.minecraft.world.item.crafting.RecipeHolder;")
        }
        // Methods 1.20.5 and later added or renamed, as 1.20.1 has them. The calls the mod's own helper stands in for
        // (compat.legacy.Legacy) are written out whole, receiver and all, so nothing else turns into them.
        string(current.parsed >= "1.20.5") {
            val legacy = "net.modcrafters.minecraft.barnyardromance.compat.legacy.Legacy"
            replace("ItemStack.isSameItemSameTags(", "ItemStack.isSameItemSameComponents(")
            // DataResult.getOrThrow() came with DataFixerUpper 7; 6 takes whether to allow a partial result, and a
            // callback for the error before it throws.
            replace(".getOrThrow(false, error -> {})", ".getOrThrow()")
            replace(".getAbilities().instabuild/*hasInfiniteMaterials*/", ".hasInfiniteMaterials()")
            replace("$legacy.consume(stack, 1, player)", "stack.consume(1, player)")
            replace("$legacy.isPanicking(animal)", "animal.isPanicking()")
            replace("$legacy.isInLiquid(animal)", "animal.isInLiquid()")
            replace("$legacy.random(self)", "self.getRandom()")
            replace("$legacy.dropContentsOnDestroy(", "Containers.dropContentsOnDestroy(")
            // Tooltips get the level rather than a context; the mod's items don't read either, so the name stays.
            replace(
                "ItemStack stack, net.minecraft.world.level.Level context, List<Component> lines, TooltipFlag flag",
                "ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag"
            )
            // A block's use without an item was its plain use, with a hand.
            replace(
                "public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {",
                "protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {"
            )
            // A superfood's lore line is a component from 1.20.5; before that the item writes it (LoreItem).
            replace("new net.modcrafters.minecraft.barnyardromance.compat.legacy.LoreItem(ItemProperties.superfood(", "new Item(ItemProperties.superfood(")
        }
        // Block methods were public before 1.20.5. Only the block overrides the mod has, by their first parameter.
        regex(current.parsed >= "1.20.5") {
            val overrides = """(?:void onRemove|RenderShape getRenderShape|boolean hasAnalogOutputSignal|int getAnalogOutputSignal|""" +
                    """BlockState rotate|BlockState mirror|VoxelShape getShape|VoxelShape getCollisionShape)\(BlockState """
            replace("""\bpublic (?=$overrides)""", "protected ", """\bprotected (?=$overrides)""", "public ")
        }
        // The id factories came in 1.21; 1.20.1 calls the mod's own (compat.legacy.Ids), which use its constructors.
        // Replacements all run in one pass and the longest match wins, so these name the class themselves: on 1.21.1
        // the factory belongs to ResourceLocation and on 26.3 to Identifier. Each pattern going to Ids, and the one
        // coming back from it on 26.3, also takes the character after the bracket, so it beats the other group's. That
        // character can't start another replacement, so a call's first argument is never another replaced name.
        val idsFactory = "net.modcrafters.minecraft.barnyardromance.compat.legacy.Ids"
        val legacyGui = "net.modcrafters.minecraft.barnyardromance.compat.legacy.LegacyGui"
        val ids = """\Q$idsFactory.\E(fromNamespaceAndPath|withDefaultNamespace|parse)\("""
        regex(current.parsed >= "1.20.5") {
            replace(
                ids, "ResourceLocation.\$1(",
                """\b(?:Identifier|ResourceLocation)\.(fromNamespaceAndPath|withDefaultNamespace|parse)\(([\s\S])""", "$idsFactory.\$1(\$2"
            )
        }
        regex(current.parsed >= "1.21.11") {
            replace(
                """$ids([\s\S])""", "Identifier.\$1(\$2",
                """\bIdentifier\.(fromNamespaceAndPath|withDefaultNamespace|parse)\(""", "ResourceLocation.\$1("
            )
        }
        // GUI sprites name their render pipeline.
        regex(current.parsed >= "1.21.6") {
            replace(
                """\.blitSprite\((?!RenderPipelines)""", ".blitSprite(RenderPipelines.GUI_TEXTURED, ",
                """\.blitSprite\(RenderPipelines\.GUI_TEXTURED, """, ".blitSprite("
            )
            // Back from 1.20.1 (below), taking a character more so it beats the next group's way back to 1.21.1.
            replace(
                """\Q$legacyGui.blitSprite(graphics, \E([\s\S])""", "graphics.blitSprite(RenderPipelines.GUI_TEXTURED, \$1",
                // Nothing goes the other way: the next group takes 1.20.1 back to 1.21.1 itself.
                """(?!)never""", "never"
            )
        }
        // 1.20.1 has no GUI sprite atlas: compat.legacy.LegacyGui draws each sprite from its own file.
        regex(current.parsed >= "1.20.5") {
            replace(
                """\Q$legacyGui.blitSprite(graphics, \E""", "graphics.blitSprite(",
                """\bgraphics\.blitSprite\((?:RenderPipelines\.GUI_TEXTURED, )?([\s\S])""", "$legacyGui.blitSprite(graphics, \$1"
            )
        }
    }
}

// Decompiling Minecraft for several versions at once can run the machine out of memory, so MDG does one at a time.
interface MinecraftArtifactsMutex : BuildService<BuildServiceParameters.None>

val mutex = gradle.sharedServices.registerIfAbsent("createMinecraftArtifactsMutex", MinecraftArtifactsMutex::class.java) {
    maxParallelUsages.set(1)
}

// Version projects share names across branches (:26.3, :fabric:26.3) and Gradle tells projects apart by
// group and name, so each branch's versions get their own group before anything resolves them.
subprojects {
    if (parent != rootProject) group = "${rootProject.group}.${parent!!.name}"
    else if (name.first().isDigit()) group = "${rootProject.group}.common"

    tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
        usesService(mutex)
    }
}

// Forge is left out until its code compiles (Task 11 of the 1.20.1 plan adds it back).
// Builds every loader jar and copies them to build/libs/<mod version>/ (the common jars aren't mods).
tasks.register<Copy>("collectJars") {
    group = "build"
    val jars = stonecutter.tasks.named<Jar>("jar") { branch.id.isNotEmpty() && branch.id != "forge" }
    dependsOn(jars.map { it.values })
    from(jars.map { it.values.map { jar -> jar.flatMap(Jar::getArchiveFile) } })
    into(layout.buildDirectory.dir("libs/${project.version}"))
}
