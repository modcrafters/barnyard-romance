package net.modcrafters.minecraft.barnyardromance.core.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.modcrafters.minecraft.barnyardromance.core.Constants;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// The load condition barnyard_romance:config_enabled, which both loaders register under their own condition types. A
// recipe or advancement with {"key": "milkConversion"} loads only while that on/off switch in the config is on. The
// config is read at startup, so the condition follows it from the next launch.
public final class ConfigEnabled {
    public static final String ID = "config_enabled";
    // The loaders' condition types read the switch's name from this field.
    public static final MapCodec<String> KEY = Codec.STRING.fieldOf("key");
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ConfigEnabled() {
    }

    public static boolean test(String key) {
        if ("milkConversion".equals(key)) {
            return BarnyardConfig.current().milkConversion();
        }
        // A typo in a data file shouldn't quietly switch something on, or flood the log on every reload.
        if (WARNED.add(key)) {
            Constants.LOG.warn("The load condition {}:{} names an unknown config switch, {}; treating it as off", Constants.MOD_ID, ID, key);
        }
        return false;
    }
}
