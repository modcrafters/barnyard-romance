package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import net.modcrafters.minecraft.barnyardromance.core.registry.ItemProperties;

// The three pails and their window. They are made when this class loads, which is while the registries are open (see
// register).
public final class PailItems {
    public static final TagKey<Item> PAILS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pails"));

    public static final ResourceKey<Item> WOODEN_PAIL_ID = key("wooden_pail");
    public static final ResourceKey<Item> COPPER_PAIL_ID = key("copper_pail");
    public static final ResourceKey<Item> IRON_PAIL_ID = key("iron_pail");
    public static final Item WOODEN_PAIL = new PailItem(ItemProperties.of(WOODEN_PAIL_ID).stacksTo(1), 2000);
    public static final Item COPPER_PAIL = new PailItem(ItemProperties.of(COPPER_PAIL_ID).stacksTo(1), 3000);
    public static final Item IRON_PAIL = new PailItem(ItemProperties.of(IRON_PAIL_ID).stacksTo(1), 5000);
    public static final Identifier PAIL_MENU_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail");
    public static final MenuType<PailMenu> PAIL_MENU = Services.PLATFORM.menuType(PailMenu::new);

    private PailItems() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.ITEM, WOODEN_PAIL_ID.identifier(), () -> WOODEN_PAIL);
        sink.register(Registries.ITEM, COPPER_PAIL_ID.identifier(), () -> COPPER_PAIL);
        sink.register(Registries.ITEM, IRON_PAIL_ID.identifier(), () -> IRON_PAIL);
        sink.register(Registries.MENU, PAIL_MENU_ID, () -> PAIL_MENU);
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }
}
