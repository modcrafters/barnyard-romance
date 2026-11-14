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
// There are no exceptions, and new ones aren't allowed: an import that breaks the rule needs an api, an event or a
// move, not an allowance here.
class FeatureBoundaryScanTest {
    private static final Set<String> FEATURES = Set.of("familiarity", "breeding", "pregnancy", "eggs", "feed", "food",
            "milk", "shear", "vigour", "rivalry", "drops", "herd", "stats", "advancement", "naturalist");
    private static final Set<String> HOOK_PACKAGES = Set.of("mixin", "mixin/client", "client", "look", "command", "compat/jade");
    // The composition root, in the mod's root package, and the flag the Jade plugin sets, which loads without Jade.
    private static final Set<String> HOOK_CLASSES = Set.of("BarnyardRomance", "BarnyardFeatures", "compat/JadeBridge");
    // Mixin accessors stay where Mixin needs them but count as core.
    private static final Set<String> CORE_CLASSES_IN_HOOK_PACKAGES = Set.of("mixin/AnimalEating",
            "mixin/AnimalLoveCauseAccessor", "mixin/OcelotAccessor", "mixin/MobGoalsAccessor");
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
        assertTrue(scan.unclassified.isEmpty(), () -> "add these to FEATURES or HOOK_PACKAGES:\n"
                + String.join("\n", scan.unclassified));
        List<String> broken = scan.violations.entrySet().stream()
                .map(violation -> violation.getKey() + "   (" + violation.getValue() + ")").toList();
        assertTrue(broken.isEmpty(), () -> broken.size() + " imports break the rule between core, features and hooks:\n"
                + String.join("\n", broken));
    }

    // A class listed by name that moved or went away would leave its new home classified by its package instead.
    @Test
    void everyListedClassStillExists() {
        Path root = SourceScan.repoRoot();
        List<String> stale = new ArrayList<>();
        for (String listed : HOOK_CLASSES) {
            if (!existsInMain(root, listed)) {
                stale.add("HOOK_CLASSES " + listed + " is no longer a class");
            }
        }
        for (String listed : CORE_CLASSES_IN_HOOK_PACKAGES) {
            if (!existsInMain(root, listed)) {
                stale.add("CORE_CLASSES_IN_HOOK_PACKAGES " + listed + " is no longer a class");
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

    // The imported top-level class as a path, e.g. core/animal/AnimalTraits for core.animal.AnimalTraits.Inner or a
    // static import from it; a package wildcard keeps its *, e.g. breeding/*.
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
        if (HOOK_CLASSES.contains(path)) {
            return Zone.HOOK;
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
