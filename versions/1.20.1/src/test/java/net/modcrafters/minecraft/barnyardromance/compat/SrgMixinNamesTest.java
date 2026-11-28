package net.modcrafters.minecraft.barnyardromance.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

// In a real Forge 1.20.1 game Minecraft's members, and other mods' overrides of them, have SRG names (aiStep is m_8107_).
// The dev runs use Mojang's names for everything, so a mixin that only names Mojang's passes every game test and then
// does nothing in the shipped game. This reads the shipped jar (reobfJar's), every mixin in it, and the SRG mapping:
// - a selector the refmap doesn't remap (remap = false, as on a @Pseudo mixin into another mod) that names a method
//   Minecraft obfuscates must name its SRG alias as well: method = {"aiStep", "m_8107_"};
// - an @At target that is a member Minecraft obfuscates must have a refmap entry, so it needs remap = true on a mixin
//   that is otherwise remap = false.
// The test task builds the jar first and passes its path and the mapping's.
class SrgMixinNamesTest {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final Pattern SRG = Pattern.compile("[mf]_\\d+_");

    @Test
    void everyMixinReachesMinecraftsSrgNames() throws IOException {
        Mappings mappings = Mappings.read(path("barnyard_romance.srg_mappings"));
        List<String> problems = new ArrayList<>();
        int mixins = 0;
        try (JarFile jar = new JarFile(path("barnyard_romance.forge_mod_jar").toFile())) {
            JsonObject refmap = refmap(jar);
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                ClassNode node = new ClassNode();
                try (InputStream in = jar.getInputStream(entry)) {
                    new ClassReader(in).accept(node, ClassReader.SKIP_CODE);
                }
                AnnotationNode mixin = annotation(node.invisibleAnnotations, MIXIN);
                if (mixin == null) {
                    continue;
                }
                mixins++;
                JsonObject classMappings = refmap.has(node.name) ? refmap.getAsJsonObject(node.name) : new JsonObject();
                boolean mixinRemap = remap(mixin, true);
                for (MethodNode method : node.methods) {
                    for (AnnotationNode injector : annotations(method)) {
                        check(node.name + "." + method.name, injector, remap(injector, mixinRemap), mappings, classMappings, problems);
                    }
                }
            }
        }
        assertTrue(mixins > 0, "the mod jar has no mixins");
        assertTrue(problems.isEmpty(), () -> "these mixins won't find their targets in a real Forge 1.20.1 game:\n  "
                + String.join("\n  ", problems));
    }

    private static void check(String handler, AnnotationNode injector, boolean remap, Mappings mappings, JsonObject refmap,
                              List<String> problems) {
        List<String> selectors = strings(value(injector, "method"));
        if (!remap) {
            for (String selector : selectors) {
                String name = memberName(selector);
                Set<String> aliases = mappings.srgOf(name);
                if (!aliases.isEmpty() && selectors.stream().map(SrgMixinNamesTest::memberName).noneMatch(aliases::contains)) {
                    problems.add(handler + ": method \"" + name + "\" isn't remapped and has no SRG alias (one of " + aliases + ")");
                }
            }
        }
        for (AnnotationNode at : nested(injector, AT)) {
            Object target = value(at, "target");
            if (target instanceof String reference && mappings.obfuscated(reference) && !refmap.has(reference)) {
                problems.add(handler + ": @At target " + reference + " has no refmap entry (needs remap = true)");
            }
        }
    }

    // The member's name in a selector: aiStep, aiStep()V or Lowner;aiStep()V.
    private static String memberName(String selector) {
        String name = selector;
        int semicolon = name.indexOf(';');
        if (name.startsWith("L") && semicolon > 0) {
            name = name.substring(semicolon + 1);
        }
        int end = name.indexOf('(');
        if (end < 0) {
            end = name.indexOf(':');
        }
        return end < 0 ? name : name.substring(0, end);
    }

    private static Path path(String property) {
        String value = System.getProperty(property);
        assertTrue(value != null, () -> "the test task doesn't pass " + property);
        Path path = Path.of(value);
        assertTrue(Files.exists(path), () -> "nothing at " + path);
        return path;
    }

    private static JsonObject refmap(JarFile jar) throws IOException {
        JarEntry entry = jar.getJarEntry("barnyard_romance.refmap.json");
        assertTrue(entry != null, "the mod jar has no refmap");
        try (InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("mappings");
        }
    }

    private static List<AnnotationNode> annotations(MethodNode method) {
        List<AnnotationNode> all = new ArrayList<>();
        if (method.visibleAnnotations != null) {
            all.addAll(method.visibleAnnotations);
        }
        if (method.invisibleAnnotations != null) {
            all.addAll(method.invisibleAnnotations);
        }
        return all;
    }

    private static AnnotationNode annotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return null;
        }
        return annotations.stream().filter(annotation -> annotation.desc.equals(desc)).findFirst().orElse(null);
    }

    private static boolean remap(AnnotationNode annotation, boolean inherited) {
        return value(annotation, "remap") instanceof Boolean remap ? remap : inherited;
    }

    private static Object value(AnnotationNode annotation, String name) {
        if (annotation.values != null) {
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (annotation.values.get(i).equals(name)) {
                    return annotation.values.get(i + 1);
                }
            }
        }
        return null;
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof String string) {
            out.add(string);
        } else if (value instanceof List<?> list) {
            list.forEach(item -> out.addAll(strings(item)));
        }
        return out;
    }

    // Every annotation of type desc inside this one, at any depth (an injector's at, a slice's from and to).
    private static List<AnnotationNode> nested(AnnotationNode annotation, String desc) {
        List<AnnotationNode> found = new ArrayList<>();
        if (annotation.values != null) {
            for (int i = 1; i < annotation.values.size(); i += 2) {
                collect(annotation.values.get(i), desc, found);
            }
        }
        return found;
    }

    private static void collect(Object value, String desc, List<AnnotationNode> found) {
        if (value instanceof AnnotationNode node) {
            if (node.desc.equals(desc)) {
                found.add(node);
            }
            found.addAll(nested(node, desc));
        } else if (value instanceof List<?> list) {
            list.forEach(item -> collect(item, desc, found));
        }
    }

    // The SRG mapping ModDevGradle writes (intermediateToNamed.srg): "MD: owner/m_1_ (desc) owner/name (desc)" and
    // "FD: owner/f_1_ owner/name". Only obfuscated members, whose SRG name differs from Mojang's.
    private record Mappings(Map<String, Set<String>> srgByName, Set<String> fieldNames, Set<String> members) {
        static Mappings read(Path file) throws IOException {
            Map<String, Set<String>> srgByName = new HashMap<>();
            Set<String> fieldNames = new HashSet<>();
            Set<String> members = new HashSet<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] cells = line.split(" ");
                if (cells[0].equals("MD:") && cells.length == 5) {
                    String srg = cells[1].substring(cells[1].lastIndexOf('/') + 1);
                    if (SRG.matcher(srg).matches()) {
                        String named = cells[3];
                        String name = named.substring(named.lastIndexOf('/') + 1);
                        srgByName.computeIfAbsent(name, ignored -> new HashSet<>()).add(srg);
                        members.add("L" + named.substring(0, named.lastIndexOf('/')) + ";" + name + cells[4]);
                    }
                } else if (cells[0].equals("FD:") && cells.length == 3) {
                    String srg = cells[1].substring(cells[1].lastIndexOf('/') + 1);
                    if (SRG.matcher(srg).matches()) {
                        String named = cells[2];
                        fieldNames.add(named.substring(named.lastIndexOf('/') + 1));
                        members.add("L" + named.substring(0, named.lastIndexOf('/')) + ";" + named.substring(named.lastIndexOf('/') + 1));
                    }
                }
            }
            return new Mappings(srgByName, fieldNames, members);
        }

        Set<String> srgOf(String name) {
            return srgByName.getOrDefault(name, Set.of());
        }

        // Whether an @At target names an obfuscated member: Lowner;name(desc) for a method, Lowner;name:Ltype; for a field.
        // The mapping lists a member only on the class that declares it, so a target through a subclass of Minecraft's
        // (MushroomCow;discard()V) counts by its name: a Minecraft class's member named like an obfuscated one is one.
        boolean obfuscated(String reference) {
            int colon = reference.indexOf(':');
            if (members.contains(colon < 0 ? reference : reference.substring(0, colon))) {
                return true;
            }
            boolean minecraft = reference.startsWith("Lnet/minecraft/") || reference.startsWith("Lcom/mojang/");
            String name = memberName(reference);
            return minecraft && (colon < 0 ? srgByName.containsKey(name) : fieldNames.contains(name));
        }
    }
}
