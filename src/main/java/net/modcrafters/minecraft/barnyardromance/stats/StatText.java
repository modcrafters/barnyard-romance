package net.modcrafters.minecraft.barnyardromance.stats;

import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.milk.MilkStatus;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.text.Text;

// The words the stat sheet is built from, shared by the herd book, the sneak card and the Inspector.
public final class StatText {
    public static final Text READY = Text.of("gui.barnyard_romance.inspector.ready");
    public static final Text NOT_TAMED = Text.of("gui.barnyard_romance.inspector.not_tamed");
    public static final Text IN_LOVE = Text.of("gui.barnyard_romance.inspector.in_love");
    public static final Text WORN_OUT = Text.of("gui.barnyard_romance.inspector.worn_out");
    public static final Text TOO_YOUNG = Text.of("gui.barnyard_romance.inspector.too_young");
    public static final Text VIGOUR_HUNGRY_TEXT = Text.of("gui.barnyard_romance.inspector.vigour_hungry");

    private StatText() {
    }

    public static Text father(String sizeLetter, String speciesId) {
        return Text.of("gui.barnyard_romance.inspector.father", sizeLetter, mate(speciesId));
    }

    // A hen's row counts her eggs; a male's says whether he makes them fertile.
    public static Text eggRowLabel(boolean female, String speciesId) {
        if (female) {
            return Text.of("gui.barnyard_romance.inspector.eggs");
        }
        return Text.of("minecraft:chicken".equals(speciesId) ? "gui.barnyard_romance.inspector.rooster" : "gui.barnyard_romance.inspector.male");
    }

    public static Text mate(String speciesId) {
        if (speciesId == null || speciesId.isBlank()) {
            return Text.of("gui.barnyard_romance.mate.unknown");
        }
        String path = speciesId;
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        return switch (path) {
            case "cow", "mooshroom" -> Text.of("gui.barnyard_romance.mate.bull");
            case "pig" -> Text.of("gui.barnyard_romance.mate.boar");
            case "chicken" -> Text.of("gui.barnyard_romance.mate.rooster");
            case "sheep" -> Text.of("gui.barnyard_romance.mate.ram");
            case "rabbit", "goat" -> Text.of("gui.barnyard_romance.mate.buck");
            case "horse" -> Text.of("gui.barnyard_romance.mate.stallion");
            case "donkey" -> Text.of("gui.barnyard_romance.mate.jack");
            case "cat" -> Text.of("gui.barnyard_romance.mate.tom");
            default -> Text.plain(path.replace('_', ' '));
        };
    }

    // Love and rest are stored as the game tick they end, so they expire on the client without a new sync.
    public static Text breeding(
            boolean needsTaming,
            boolean tamed,
            double familiarity,
            boolean young,
            boolean wornOut,
            long loveUntil,
            long cooldownUntil,
            long now
    ) {
        if (needsTaming && !tamed) {
            return NOT_TAMED;
        }
        if (!Familiarity.ready(familiarity)) {
            return Text.of("gui.barnyard_romance.inspector.needs_familiarity", Familiarity.percent(Familiarity.threshold()));
        }
        if (young) {
            return TOO_YOUNG;
        }
        if (cooldownUntil > now) {
            return Text.of("gui.barnyard_romance.inspector.resting", Growth.clock(cooldownUntil - now));
        }
        if (wornOut) {
            return WORN_OUT;
        }
        if (loveUntil > now) {
            return IN_LOVE;
        }
        return READY;
    }

    public static boolean readyToBreed(Text breedingText) {
        return READY.equals(breedingText) || IN_LOVE.equals(breedingText);
    }

    public static Text lastBirth(String offspringName, long lastBirthTick, long now) {
        if (lastBirthTick < 0L || offspringName == null || offspringName.isBlank()) {
            return Text.EMPTY;
        }
        return Text.of("gui.barnyard_romance.inspector.last_birth", offspringName, ago(now - lastBirthTick));
    }

    // Milk is counted in Minecraft days and hours, the way its three unmilked days are.
    public static Text milkText(MilkStatus status) {
        if (status.nursing()) {
            return Text.of("gui.barnyard_romance.inspector.milk_flowing", status.left(), status.perDay());
        }
        return Text.of("gui.barnyard_romance.inspector.milk_left", gameTime(status.ticksLeft()), status.left(), status.perDay());
    }

    // Nothing for an animal with no eggs today for any other reason (a chick, or a bird that doesn't lay).
    public static Text eggsText(int laid, int planned, boolean hungryYesterday) {
        if (hungryYesterday) {
            return Text.of("gui.barnyard_romance.inspector.eggs_hungry");
        }
        return planned <= 0 ? Text.EMPTY : Text.of("gui.barnyard_romance.inspector.eggs_today", laid, planned);
    }

    // A young rooster isn't fertile until he's grown; a grown one has as many eggs left as his Vigour pays for.
    public static Text roosterText(boolean young, double vigour, boolean hungry, double perEgg) {
        if (young) {
            return Text.of("gui.barnyard_romance.inspector.rooster_young");
        }
        long eggs = perEgg <= 0.0 ? 0L : (long) Math.floor((vigour + 1.0e-9) / perEgg);
        if (eggs <= 0L) {
            return Text.of("gui.barnyard_romance.inspector.rooster_spent");
        }
        if (eggs == 1L) {
            return Text.of(hungry ? "gui.barnyard_romance.inspector.rooster_one_egg_hungry" : "gui.barnyard_romance.inspector.rooster_one_egg");
        }
        return Text.of(hungry ? "gui.barnyard_romance.inspector.rooster_eggs_hungry" : "gui.barnyard_romance.inspector.rooster_eggs", eggs);
    }

    // Minecraft hours and days: under an hour, then hours, then whole days.
    public static Text gameTime(long ticks) {
        long hours = Math.max(0L, ticks) / 1_000L;
        if (hours < 1L) {
            return Text.of("gui.barnyard_romance.time.under_an_hour");
        }
        if (hours < 24L) {
            return Text.of("gui.barnyard_romance.time.hours", hours);
        }
        return Text.of("gui.barnyard_romance.time.days", hours / 24L);
    }

    public static Text ago(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        if (seconds < 60L) {
            return Text.of("gui.barnyard_romance.time.just_now");
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return Text.of("gui.barnyard_romance.time.minutes_ago", minutes);
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return Text.of("gui.barnyard_romance.time.hours_ago", hours);
        }
        return Text.of("gui.barnyard_romance.time.days_ago", hours / 24L);
    }

    // "Eggs: 2/3 today", "Rooster: Fertile, 3 eggs left" and "Breeding: Ready".
    public static Text labelled(Text label, Text value) {
        return Text.of("gui.barnyard_romance.sheet.labelled", label, value);
    }
}
