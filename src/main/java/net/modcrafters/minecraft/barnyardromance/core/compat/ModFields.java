package net.modcrafters.minecraft.barnyardromance.core.compat;

import net.modcrafters.minecraft.barnyardromance.core.Constants;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Sets an int field of another mod's class by name, not @Shadow: a @Shadow on a field a future version of that mod
// renamed would stop the game loading, and this only stops setting it and says so once.
public final class ModFields {
    // Per class, then per field name. An empty Optional is a field that couldn't be set, and isn't looked for again.
    private static final Map<Class<?>, Map<String, Optional<Field>>> FIELDS = new ConcurrentHashMap<>();

    private ModFields() {
    }

    // Sets the int field `field` of target, found on its class or a superclass, to value. A field that's missing, not an
    // int or not reachable is logged once for that class and field, and the call then does nothing.
    public static void holdInt(Object target, String field, int value) {
        Class<?> type = target.getClass();
        Optional<Field> found = FIELDS.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, name -> find(type, name));
        if (found.isEmpty()) {
            return;
        }
        try {
            found.get().setInt(target, value);
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            giveUp(type, field, exception);
        }
    }

    // True once holdInt stopped trying for this class and field.
    static boolean gaveUp(Class<?> type, String field) {
        Map<String, Optional<Field>> fields = FIELDS.get(type);
        return fields != null && fields.containsKey(field) && fields.get(field).isEmpty();
    }

    private static Optional<Field> find(Class<?> type, String name) {
        try {
            for (Class<?> level = type; level != null; level = level.getSuperclass()) {
                for (Field field : level.getDeclaredFields()) {
                    if (field.getName().equals(name)) {
                        if (field.getType() != int.class) {
                            throw new NoSuchFieldException(level.getName() + "." + name + " is a " + field.getType().getName());
                        }
                        field.setAccessible(true);
                        return Optional.of(field);
                    }
                }
            }
            throw new NoSuchFieldException(type.getName() + " has no field " + name);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            Constants.LOG.warn("Can't set the field {} on {}, so a mod's own behaviour there is left as it is", name,
                    type.getName(), exception);
            return Optional.empty();
        }
    }

    private static void giveUp(Class<?> type, String field, Exception exception) {
        FIELDS.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>()).put(field, Optional.empty());
        Constants.LOG.warn("Can't set the field {} on {}, so a mod's own behaviour there is left as it is", field,
                type.getName(), exception);
    }
}
