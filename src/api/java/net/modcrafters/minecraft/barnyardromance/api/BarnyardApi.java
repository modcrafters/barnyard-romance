package net.modcrafters.minecraft.barnyardromance.api;

/**
 * The way in. Check that the mod "barnyard_romance" is loaded before you touch any of this.
 */
public final class BarnyardApi {
    private static volatile BarnyardAnimals animals;
    private static volatile BarnyardPails pails;

    private BarnyardApi() {
    }

    public static BarnyardAnimals animals() {
        BarnyardAnimals installed = animals;
        if (installed == null) {
            throw new IllegalStateException("Barnyard Romance isn't loaded");
        }
        return installed;
    }

    /**
     * For Barnyard Romance only: it installs the queries while it starts.
     */
    public static void install(BarnyardAnimals queries) {
        animals = queries;
    }

    /**
     * Reading and filling pails.
     */
    public static BarnyardPails pails() {
        BarnyardPails installed = pails;
        if (installed == null) {
            throw new IllegalStateException("Barnyard Romance isn't loaded");
        }
        return installed;
    }

    /**
     * For Barnyard Romance only: it installs the pail queries while it starts.
     */
    public static void installPails(BarnyardPails queries) {
        pails = queries;
    }
}
