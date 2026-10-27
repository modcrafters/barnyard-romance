plugins {
    id("dev.kikugie.stonecutter")
    // see https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml for new versions
    id("net.fabricmc.fabric-loom") version "1.18.1" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "2.0.147" apply false
}

stonecutter active "26.3"

// Lets code check the loader with `//? if fabric` or `//? if neoforge`.
stonecutter parameters {
    constants.match(branch.id.ifEmpty { "common" }, "common", "fabric", "neoforge")
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

// Builds every loader jar and copies them to build/libs/<mod version>/ (the common jars aren't mods).
tasks.register<Copy>("collectJars") {
    group = "build"
    val jars = stonecutter.tasks.named<Jar>("jar") { branch.id.isNotEmpty() }
    dependsOn(jars.map { it.values })
    from(jars.map { it.values.map { jar -> jar.flatMap(Jar::getArchiveFile) } })
    into(layout.buildDirectory.dir("libs/${project.version}"))
}
