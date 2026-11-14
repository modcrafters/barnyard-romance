package net.modcrafters.minecraft.barnyardromance.eggs.api;

import net.modcrafters.minecraft.barnyardromance.core.text.Text;

import java.util.ArrayList;
import java.util.List;

// What a hen will lay tomorrow, as the herd book shows it, and why it isn't better (see EggsApi.eggsTomorrow).
// Tomorrow's eggs depend on today: she lays only after a day she ate, they're fertile only with a rooster that ate
// today and a nesting box with room near her, and a familiar hen lays one more. The rooster and the box are where
// they are now; the book can't know where she'll be tomorrow.
public record EggOutlook(Kind kind, int count, List<Reason> reasons) {
    public enum Kind {
        NO_EGGS,
        PLAIN,
        FERTILIZED
    }

    public enum Reason {
        NOT_FED("gui.barnyard_romance.herd_book.eggs_not_fed"),
        NO_ROOSTER("gui.barnyard_romance.herd_book.eggs_no_rooster"),
        ROOSTER_HUNGRY("gui.barnyard_romance.herd_book.eggs_rooster_hungry"),
        NO_BOX("gui.barnyard_romance.herd_book.eggs_no_box"),
        NOT_FAMILIAR("gui.barnyard_romance.herd_book.eggs_not_familiar"),
        NO_MALE("gui.barnyard_romance.herd_book.eggs_no_male"),
        MALE_HUNGRY("gui.barnyard_romance.herd_book.eggs_male_hungry");

        private final String key;

        Reason(String key) {
            this.key = key;
        }

        public Text text() {
            return Text.of(key);
        }
    }

    public EggOutlook {
        reasons = List.copyOf(reasons);
    }

    public Text title() {
        return switch (kind) {
            case FERTILIZED -> Text.of("gui.barnyard_romance.herd_book.eggs_fertilized", count);
            case PLAIN -> Text.of("gui.barnyard_romance.herd_book.eggs_plain", count);
            case NO_EGGS -> Text.of("gui.barnyard_romance.herd_book.eggs_none");
        };
    }

    // One line for the herd book's icon: what she'll lay, then each reason it isn't better.
    public Text tooltip() {
        List<Text> parts = new ArrayList<>();
        parts.add(title());
        reasons.forEach(reason -> parts.add(reason.text()));
        return Text.join(". ", parts);
    }
}
