package net.modcrafters.minecraft.barnyardromance.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
import net.modcrafters.minecraft.barnyardromance.herd.HerdPageText;
import net.modcrafters.minecraft.barnyardromance.herd.ShearState;
import net.modcrafters.minecraft.barnyardromance.network.HerdBookPayloads;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
//? if >=1.21.9
import net.minecraft.client.input.MouseButtonEvent;
//? if >=1.21.6
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// The herd book: species bookmarks down the edges, a contents page per species, and a page per animal.
// The whole book comes from the server in one message (HerdBookPayloads.ShowBook); paging happens here.
public class HerdBookScreen extends Screen {
    private static final Identifier BOOK = Identifier.withDefaultNamespace("textures/gui/book.png");
    private static final int SIZE = 192;
    private static final int TEXT_X = 36;
    private static final int TEXT_Y = 30;
    private static final int TEXT_WIDTH = 114;
    private static final int LINE = 9;
    // Family rows get a pixel more, so a link's underline doesn't touch the row below.
    private static final int LINK_LINE = 10;
    private static final int BUTTONS_Y = 157;
    private static final int TEXT = 0xFF000000;
    private static final int MUTED = 0xFF6B4A2A;
    private static final int HEADER = 0xFF8A5A22;
    private static final int LINK = 0xFF1F4F8A;
    private static final int FEMALE = 0xFFC2457E;
    private static final int MALE = 0xFF2F6FB0;
    private static final int HUNGRY = 0xFFB5482F;
    private static final int FROZEN = 0xFF8A8A8A;
    private static final int TAB_TEXT = 0xFF4C1A0B;
    // Where the cover's left and right edges are in the book texture.
    private static final int COVER_LEFT = 20;
    private static final int COVER_RIGHT = 166;
    private static final int TAB_TOP = 8;
    private static final int TAB_HEIGHT = 13;
    private static final int TAB_PITCH = 14;
    // How far a bookmark reaches under the cover, and how far it stands out past it.
    private static final int TAB_TUCK = 8;
    // A bookmark stands out as far as its label needs, and the open one a little further.
    private static final int TAB_PADDING = 8;
    private static final int TAB_SELECTED = 4;
    private static final String FEMALE_MARK = "♀";
    private static final String MALE_MARK = "♂";

    // Status icons are the hunger bar's size.
    private static final int ICON = 9;
    private static final Identifier FOOD_EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
    private static final Identifier FOOD_FULL = Identifier.withDefaultNamespace("hud/food_full");
    private static final Identifier MILK_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/milk");
    private static final Identifier HEART_EMPTY = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier PREGNANT_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/pregnant");
    private static final Identifier MILK_EMPTY = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/milk_empty");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier SHEARS_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/shears");
    private static final Identifier SHEARS_EMPTY = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/shears_empty");
    private static final Identifier FERTILE_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/fertile");
    private static final Identifier UNKNOWN_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/unknown");
    private static final Identifier RETURN_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/back");
    private static final Identifier RETURN_HIGHLIGHTED = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/back_highlighted");
    private static final Identifier REMOVE_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/remove");
    private static final Identifier REMOVE_HIGHLIGHTED = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/remove_highlighted");

    // An animal's page starts higher than a contents page, which has a title there.
    private static final int ANIMAL_TOP = 14;
    private static final int PORTRAIT = 24;
    private static final int STATUS_LINE = 10;
    private static final int YOUNG_GAP = 2;
    private static final int BAR_HEIGHT = 5;
    private static final int INK = 0xFF3B2614;
    private static final int RULE = 0xFFD9C7A0;
    private static final int BAR_EDGE = 0xFF8A6A48;
    private static final int BAR_EMPTY = 0xFFEFE3C6;
    private static final int FAMILIAR_BAR = 0xFFD6523A;
    private static final int PREGNANT_BAR = 0xFFE07096;
    private static final int FERTILE_BAR = 0xFF78A840;
    // The bottom bar: page arrows on either side of the return arrow, the cross on the right.
    private static final int NEXT_X = 46;
    private static final int RETURN_X = 25;
    private static final int RETURN_WIDTH = 19;
    private static final int RETURN_HEIGHT = 13;
    private static final int REMOVE_WIDTH = 11;
    private static final int REMOVE_HEIGHT = 12;

    private record Hit(int x0, int y0, int x1, int y1, @Nullable Runnable action, @Nullable Component tooltip) {
        boolean contains(double x, double y) {
            return x >= x0 && x < x1 && y >= y0 && y < y1;
        }
    }

    private Optional<UUID> book = Optional.empty();
    private Map<UUID, HerdEntry> entries = Map.of();
    private List<HerdBookLayout.Row> rows = List.of();
    private Map<UUID, String> names = Map.of();
    private String species = "";
    private int page;
    private @Nullable UUID animal;
    private boolean removeArmed;
    private final List<Hit> hits = new ArrayList<>();
    private @Nullable PageButton backButton;
    private @Nullable PageButton forwardButton;

    public HerdBookScreen(HerdBookPayloads.ShowBook book) {
        super(Component.translatable("item.barnyard_romance.herd_book"));
        update(book);
    }

    // A new copy of the book from the server: keep the page if it still exists.
    public void update(HerdBookPayloads.ShowBook book) {
        this.book = book.book();
        Map<UUID, HerdEntry> byId = new LinkedHashMap<>();
        book.entries().forEach(entry -> byId.put(entry.id(), entry));
        this.entries = byId;
        Map<UUID, String> resolved = new LinkedHashMap<>();
        for (HerdEntry entry : book.entries()) {
            String motherName = entry.mother().map(byId::get).map(HerdEntry::name).orElse("");
            resolved.put(entry.id(), TextComponents.of(HerdBookLayout.displayName(entry.name(), speciesName(entry.species()), motherName)).getString());
        }
        this.names = resolved;
        this.rows = book.entries().stream()
                .map(entry -> new HerdBookLayout.Row(entry.id(), entry.species(), speciesName(entry.species()), resolved.get(entry.id()),
                        entry.female(), entry.kid(), entry.alive()))
                .toList();
        this.removeArmed = false;
        if (this.animal != null && !this.entries.containsKey(this.animal)) {
            this.animal = null;
        }
        List<String> bookmarks = HerdBookLayout.bookmarks(this.rows);
        if (!bookmarks.contains(this.species)) {
            this.species = bookmarks.isEmpty() ? "" : bookmarks.getFirst();
            this.page = 0;
        }
        this.page = Math.min(this.page, Math.max(0, contents().size() - 1));
        updateButtons();
    }

    // The animal whose page is open, or null on a contents page.
    public @Nullable UUID shownAnimal() {
        return this.animal;
    }

    public void showAnimal(UUID id) {
        HerdEntry entry = this.entries.get(id);
        if (entry == null) {
            return;
        }
        this.animal = id;
        this.species = entry.species();
        this.removeArmed = false;
        updateButtons();
    }

    public void showContents(String species, int page) {
        this.animal = null;
        this.species = species;
        this.page = page;
        this.removeArmed = false;
        updateButtons();
    }

    // The first click on the remove button asks "Sure?", the second removes.
    public void pressRemove() {
        if (this.animal == null || this.book.isEmpty()) {
            return;
        }
        if (!this.removeArmed) {
            this.removeArmed = true;
            return;
        }
        Services.NETWORK.sendToServer(new HerdBookPayloads.RemoveFromBook(this.book.get(), this.animal));
        this.removeArmed = false;
    }

    @Override
    protected void init() {
        int left = left();
        this.backButton = addRenderableWidget(new PageButton(left + TEXT_X, BUTTONS_Y + top(), false, button -> back(), true));
        this.forwardButton = addRenderableWidget(new PageButton(left + TEXT_X + NEXT_X, BUTTONS_Y + top(), true, button -> forward(), true));
        updateButtons();
    }

    private void back() {
        if (this.animal != null) {
            UUID previous = HerdBookLayout.previous(HerdBookLayout.order(contents()), this.animal);
            if (previous != null) {
                showAnimal(previous);
            }
        } else if (this.page > 0) {
            this.page--;
            updateButtons();
        }
    }

    private void forward() {
        if (this.animal != null) {
            UUID next = HerdBookLayout.next(HerdBookLayout.order(contents()), this.animal);
            if (next != null) {
                showAnimal(next);
            }
        } else if (this.page < contents().size() - 1) {
            this.page++;
            updateButtons();
        }
    }

    private void updateButtons() {
        if (this.backButton == null || this.forwardButton == null) {
            return;
        }
        if (this.animal != null) {
            List<UUID> order = HerdBookLayout.order(contents());
            boolean listed = order.contains(this.animal);
            this.backButton.visible = listed && HerdBookLayout.previous(order, this.animal) != null;
            this.forwardButton.visible = listed && HerdBookLayout.next(order, this.animal) != null;
        } else {
            this.backButton.visible = this.page > 0;
            this.forwardButton.visible = this.page < contents().size() - 1;
        }
    }

    private List<List<HerdBookLayout.Line>> contents() {
        return HerdBookLayout.contents(this.rows, this.species);
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawBookmarkTabs(graphics, mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left(), top(), 0.0F, 0.0F, SIZE, SIZE, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawBook(graphics, mouseX, mouseY);
    }
    //?} else {
    /*@Override
    public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        drawBookmarkTabs(graphics, mouseX, mouseY);
        graphics.blit(BOOK, left(), top(), 0, 0, SIZE, SIZE);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawBook(graphics, mouseX, mouseY);
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && click(event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && click(x, y)) {
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
    *///?}

    // The wheel turns pages like the arrows: down for the next page or animal, up for the one before.
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY < 0) {
            forward();
            return true;
        }
        if (scrollY > 0) {
            back();
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private boolean click(double x, double y) {
        for (Hit hit : this.hits) {
            if (hit.action() != null && hit.contains(x, y)) {
                hit.action().run();
                return true;
            }
        }
        return false;
    }

    private void drawBook(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.hits.clear();
        int x = left() + TEXT_X;
        int y = top() + TEXT_Y;
        drawBookmarks(graphics);
        if (this.entries.isEmpty() || this.species.isEmpty()) {
            List<FormattedCharSequence> lines = this.font.split(Component.translatable("gui.barnyard_romance.herd_book.empty"), TEXT_WIDTH);
            for (int i = 0; i < lines.size(); i++) {
                graphics.text(this.font, lines.get(i), x, y + i * LINE, TEXT, false);
            }
        } else if (this.animal != null && this.entries.containsKey(this.animal)) {
            drawAnimal(graphics, this.entries.get(this.animal), x, mouseX, mouseY);
        } else {
            drawContents(graphics, x, y);
        }
        for (Hit hit : this.hits) {
            if (hit.tooltip() != null && hit.contains(mouseX, mouseY)) {
                //? if >=26.1 {
                graphics.setTooltipForNextFrame(hit.tooltip(), mouseX, mouseY);
                //?} else
                //graphics.renderTooltip(this.font, hit.tooltip(), mouseX, mouseY);
                break;
            }
        }
    }

    // A bookmark on screen: the whole sprite, part of which is tucked under the cover, and the part that shows.
    private record TabBox(String species, String label, HerdBookLayout.Side side, int column, int x0, int x1, int shown0, int shown1, int y) {
        boolean contains(double x, double y) {
            return x >= shown0 && x < shown1 && y >= this.y && y < this.y + TAB_HEIGHT;
        }
    }

    // Inner columns come last, so they are drawn over the columns standing out past them.
    private List<TabBox> tabBoxes() {
        List<HerdBookLayout.Tab> tabs = HerdBookLayout.tabs(HerdBookLayout.bookmarks(this.rows));
        // Each column on the right starts past the widest bookmark of the column inside it.
        List<Integer> columnStarts = new ArrayList<>(List.of(0));
        for (HerdBookLayout.Tab tab : tabs) {
            if (tab.side() == HerdBookLayout.Side.RIGHT) {
                while (columnStarts.size() <= tab.column() + 1) {
                    columnStarts.add(columnStarts.getLast());
                }
                int end = columnStarts.get(tab.column()) + tabWidth(tab.species()) + TAB_SELECTED;
                columnStarts.set(tab.column() + 1, Math.max(columnStarts.get(tab.column() + 1), end));
            }
        }
        List<TabBox> boxes = new ArrayList<>();
        for (HerdBookLayout.Tab tab : tabs) {
            String label = HerdBookLayout.tabLabel(speciesName(tab.species()));
            int shown = tab.side() == HerdBookLayout.Side.RIGHT ? columnStarts.get(tab.column()) : 0;
            int out = shown + tabWidth(tab.species()) + (tab.species().equals(this.species) ? TAB_SELECTED : 0);
            int y = top() + TAB_TOP + tab.row() * TAB_PITCH;
            if (tab.side() == HerdBookLayout.Side.LEFT) {
                int edge = left() + COVER_LEFT;
                boxes.add(new TabBox(tab.species(), label, tab.side(), tab.column(), edge - out, edge + TAB_TUCK, edge - out, edge - shown, y));
            } else {
                int edge = left() + COVER_RIGHT;
                boxes.add(new TabBox(tab.species(), label, tab.side(), tab.column(), edge - TAB_TUCK, edge + out, edge + shown, edge + out, y));
            }
        }
        boxes.sort(Comparator.comparingInt(TabBox::column).reversed());
        return boxes;
    }

    private int tabWidth(String species) {
        return this.font.width(HerdBookLayout.tabLabel(speciesName(species))) + TAB_PADDING;
    }

    private @Nullable TabBox hoveredTab(List<TabBox> boxes, double mouseX, double mouseY) {
        for (int i = boxes.size() - 1; i >= 0; i--) {
            if (boxes.get(i).contains(mouseX, mouseY)) {
                return boxes.get(i);
            }
        }
        return null;
    }

    // Drawn before the book, so the cover hides the tucked-in ends.
    private void drawBookmarkTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<TabBox> boxes = tabBoxes();
        TabBox hovered = hoveredTab(boxes, mouseX, mouseY);
        for (TabBox box : boxes) {
            String state = box.species().equals(this.species) ? "bookmark_selected" : box == hovered ? "bookmark_highlighted" : "bookmark";
            String side = box.side() == HerdBookLayout.Side.LEFT ? "_left" : "_right";
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "herd_book/" + state + side),
                    box.x0(), box.y(), box.x1() - box.x0(), TAB_HEIGHT);
        }
    }

    private void drawBookmarks(GuiGraphicsExtractor graphics) {
        List<TabBox> boxes = tabBoxes();
        // Hits are checked first to last, so the tabs drawn on top go first.
        for (TabBox box : boxes.reversed()) {
            String name = speciesName(box.species());
            int textX = (box.shown0() + box.shown1() - this.font.width(box.label()) + 1) / 2;
            graphics.text(this.font, box.label(), textX, box.y() + 3, box.species().equals(this.species) ? TEXT : TAB_TEXT, false);
            this.hits.add(new Hit(box.shown0(), box.y(), box.shown1(), box.y() + TAB_HEIGHT, () -> showContents(box.species(), 0), Component.literal(name)));
        }
    }

    private void drawContents(GuiGraphicsExtractor graphics, int x, int y) {
        List<List<HerdBookLayout.Line>> pages = contents();
        page(graphics, Component.literal(speciesName(this.species)), pages.size());
        List<HerdBookLayout.Line> lines = pages.get(Math.min(this.page, pages.size() - 1));
        int lineY = y;
        for (int i = 0; i < lines.size(); i++) {
            switch (lines.get(i)) {
                case HerdBookLayout.Header header -> graphics.text(this.font, Component.translatable(switch (header.group()) {
                    case FEMALES -> "gui.barnyard_romance.herd_book.females";
                    case MALES -> "gui.barnyard_romance.herd_book.males";
                    case KIDS -> "gui.barnyard_romance.herd_book.kids";
                }), x, lineY, HEADER, false);
                case HerdBookLayout.Animal row -> drawRow(graphics, this.entries.get(row.id()), x, lineY);
            }
            lineY += LINE;
        }
    }

    private void drawRow(GuiGraphicsExtractor graphics, HerdEntry entry, int x, int y) {
        HerdBookHeads.draw(graphics, entry.id(), entry.species(), entry.kid(), x, y - 1, ICON);
        int markX = HerdBookHeads.AVAILABLE ? x + ICON + 2 : x;
        graphics.text(this.font, entry.female() ? FEMALE_MARK : MALE_MARK, markX, y, entry.female() ? FEMALE : MALE, false);
        int nameX = markX + 8;
        int iconsX = drawIcons(graphics, entry, x + TEXT_WIDTH, y);
        String name = fit(this.names.get(entry.id()), iconsX - 2 - nameX);
        graphics.text(this.font, name, nameX, y, TEXT, false);
        this.hits.add(new Hit(nameX, y - 1, nameX + this.font.width(name), y + LINE - 1, () -> showAnimal(entry.id()), null));
    }

    // Draws the status icons right-aligned to `right` and returns where they start.
    private int drawIcons(GuiGraphicsExtractor graphics, HerdEntry entry, int right, int y) {
        int x = right;
        if (entry.familiar() != Familiar.FAMILIAR) {
            x -= this.font.width("?");
            graphics.text(this.font, "?", x, y, FROZEN, false);
            this.hits.add(new Hit(x, y, x + 6, y + 8, null, Component.translatable(entry.familiar() == Familiar.NOT_YET
                    ? "gui.barnyard_romance.herd_book.not_familiar"
                    : "gui.barnyard_romance.herd_book.no_longer_familiar")));
            return x;
        }
        if (entry.pregnancyPercent() >= 0) {
            x -= ICON;
            pregnancyHeart(graphics, entry.pregnancyPercent(), x, y - 1);
            this.hits.add(new Hit(x, y - 1, x + ICON, y + 8, null,
                    Component.translatable("gui.barnyard_romance.herd_book.pregnant_icon", entry.pregnancyPercent())));
            x -= 1;
        } else if (entry.milkReady()) {
            x -= ICON;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MILK_ICON, x, y - 1, ICON, ICON);
            this.hits.add(new Hit(x, y - 1, x + ICON, y + 8, null, Component.translatable("gui.barnyard_romance.herd_book.milk_icon")));
            x -= 1;
        }
        if (entry.shear() != ShearState.NONE) {
            x -= ICON;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, entry.shear() == ShearState.READY ? SHEARS_ICON : SHEARS_EMPTY, x, y - 1, ICON, ICON);
            this.hits.add(new Hit(x, y - 1, x + ICON, y + 8, null, Component.translatable(shearKey(entry.shear()))));
            x -= 1;
        }
        x -= ICON;
        // Like the hunger bar: the dark outline always, the drumstick on top when fed.
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_EMPTY, x, y - 1, ICON, ICON);
        if (entry.fedToday()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_FULL, x, y - 1, ICON, ICON);
        }
        this.hits.add(new Hit(x - 1, y - 1, x + 10, y + 8, null, Component.translatable(entry.fedToday()
                ? "gui.barnyard_romance.herd_book.fed_today"
                : "gui.barnyard_romance.herd_book.not_fed_today")));
        return x;
    }

    // An animal's page: a card with its head, name and size, its status lines, then its family.
    private void drawAnimal(GuiGraphicsExtractor graphics, HerdEntry entry, int x, int mouseX, int mouseY) {
        int y = top() + ANIMAL_TOP;
        HerdBookHeads.draw(graphics, entry.id(), entry.species(), entry.kid(), x, y, PORTRAIT);
        int textX = HerdBookHeads.AVAILABLE ? x + PORTRAIT + 4 : x;
        int right = x + TEXT_WIDTH;
        graphics.text(this.font, entry.female() ? FEMALE_MARK : MALE_MARK, textX, y + 2, entry.female() ? FEMALE : MALE, false);
        graphics.text(this.font, fit(this.names.get(entry.id()), right - textX - 8), textX + 8, y + 2, entry.alive() ? INK : MUTED, false);
        int subtitleY = y + 12;
        for (HerdEntry.Line line : entry.lines()) {
            if (line.kind() == HerdPageText.Kind.SIZE) {
                for (FormattedCharSequence part : this.font.split(line.text(), right - textX)) {
                    graphics.text(this.font, part, textX, subtitleY, MUTED, false);
                    subtitleY += LINE;
                }
            }
        }
        y = Math.max(y + (HerdBookHeads.AVAILABLE ? PORTRAIT : 0), subtitleY - 1) + 2;
        graphics.fill(x, y, right, y + 1, RULE);
        y += 4;
        for (HerdEntry.Line line : entry.lines()) {
            if (line.kind() != HerdPageText.Kind.SIZE) {
                drawPageLine(graphics, entry, line, x, y);
                y += STATUS_LINE;
            }
        }
        y += 2;
        Component family = Component.translatable("gui.barnyard_romance.herd_book.family");
        graphics.text(this.font, family, x, y, HEADER, false);
        graphics.fill(x + this.font.width(family) + 3, y + 4, right, y + 5, RULE);
        y += STATUS_LINE + 1;
        Component mother = Component.translatable("gui.barnyard_romance.herd_book.mother");
        Component father = Component.translatable("gui.barnyard_romance.herd_book.father");
        Component youngLabel = Component.translatable("gui.barnyard_romance.herd_book.young");
        // Mother, father and young line up after the longest of the three labels.
        int valueX = x + Math.max(this.font.width(mother), Math.max(this.font.width(father), this.font.width(youngLabel))) + 4;
        drawParent(graphics, mother, entry.mother(), x, valueX, y);
        y += STATUS_LINE;
        drawParent(graphics, father, entry.father(), x, valueX, y);
        y += STATUS_LINE;
        List<HerdEntry> young = this.entries.values().stream()
                .filter(other -> other.mother().equals(Optional.of(entry.id())) || other.father().equals(Optional.of(entry.id())))
                .sorted(Comparator.comparing((HerdEntry child) -> this.names.get(child.id())).thenComparing(HerdEntry::id))
                .toList();
        if (!young.isEmpty()) {
            graphics.text(this.font, youngLabel, x, y, MUTED, false);
            if (HerdBookHeads.AVAILABLE) {
                drawYoungHeads(graphics, young, valueX, right, y);
            } else {
                drawYoungNames(graphics, young, x, y + LINK_LINE);
            }
        }
        drawAnimalButtons(graphics, entry, mouseX, mouseY);
    }

    // A status line: its icon, then its words, or a label and a bar for pregnancy, familiarity and fertility.
    private void drawPageLine(GuiGraphicsExtractor graphics, HerdEntry entry, HerdEntry.Line line, int x, int y) {
        int iconY = y - 1;
        switch (line.kind()) {
            case FED -> {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_EMPTY, x, iconY, ICON, ICON);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_FULL, x, iconY, ICON, ICON);
            }
            case NOT_FED -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_EMPTY, x, iconY, ICON, ICON);
            case PREGNANT -> pregnancyHeart(graphics, Math.round(line.fill() * 100.0F), x, iconY);
            case MILK -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, entry.milkReady() ? MILK_ICON : MILK_EMPTY, x, iconY, ICON, ICON);
            case FAMILIARITY -> {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_EMPTY, x, iconY, ICON, ICON);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, x, iconY, ICON, ICON);
            }
            case FERTILITY -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FERTILE_ICON, x, iconY, ICON, ICON);
            case SHEAR -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, entry.shear() == ShearState.READY ? SHEARS_ICON : SHEARS_EMPTY, x, iconY, ICON, ICON);
            case SIZE, NOTE -> {
            }
        }
        int textX = x + ICON + 3;
        int right = x + TEXT_WIDTH;
        Component label = switch (line.kind()) {
            case PREGNANT -> Component.translatable("gui.barnyard_romance.herd_book.pregnant_label");
            case FAMILIARITY -> Component.translatable("gui.barnyard_romance.herd_book.familiar_label");
            case FERTILITY -> Component.translatable("gui.barnyard_romance.herd_book.fertile_label");
            default -> null;
        };
        if (label != null) {
            graphics.text(this.font, label, textX, y, INK, false);
            bar(graphics, textX + barLabelWidth() + 4, y + 1, right, line.fill(), switch (line.kind()) {
                case PREGNANT -> PREGNANT_BAR;
                case FAMILIARITY -> FAMILIAR_BAR;
                default -> FERTILE_BAR;
            });
            // The bar's words, such as how long until the birth, are in its tooltip.
            this.hits.add(new Hit(x, iconY, right, iconY + ICON, null, line.text()));
            return;
        }
        String words = line.text().getString();
        String shown = fit(words, right - textX);
        graphics.text(this.font, shown, textX, y, line.kind() == HerdPageText.Kind.NOTE ? MUTED : INK, false);
        if (!shown.equals(words)) {
            this.hits.add(new Hit(textX, iconY, right, iconY + ICON, null, line.text()));
        }
    }

    // Every bar starts at the same place, past the longest label.
    private int barLabelWidth() {
        int width = 0;
        for (String key : List.of("gui.barnyard_romance.herd_book.pregnant_label", "gui.barnyard_romance.herd_book.familiar_label",
                "gui.barnyard_romance.herd_book.fertile_label")) {
            width = Math.max(width, this.font.width(Component.translatable(key)));
        }
        return width;
    }

    // An ink-edged bar on the paper, filled from the left.
    private static void bar(GuiGraphicsExtractor graphics, int x0, int y, int x1, float fill, int color) {
        graphics.fill(x0, y, x1, y + BAR_HEIGHT, BAR_EDGE);
        graphics.fill(x0 + 1, y + 1, x1 - 1, y + BAR_HEIGHT - 1, BAR_EMPTY);
        int filled = Math.round(Math.clamp(fill, 0.0F, 1.0F) * (x1 - x0 - 2));
        if (filled > 0) {
            graphics.fill(x0 + 1, y + 1, x0 + 1 + filled, y + BAR_HEIGHT - 1, color);
        }
    }

    // An empty heart that fills with color from the bottom as the pregnancy goes on.
    private static void pregnancyHeart(GuiGraphicsExtractor graphics, int percent, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_EMPTY, x, y, ICON, ICON);
        int filled = HerdBookLayout.filledRows(percent, ICON);
        if (filled > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PREGNANT_ICON, ICON, ICON, 0, ICON - filled, x, y + ICON - filled, ICON, filled);
        }
    }

    // A parent's head and a link to their page, or a faded "?" when the book doesn't know them.
    private void drawParent(GuiGraphicsExtractor graphics, Component label, Optional<UUID> parent, int x, int valueX, int y) {
        graphics.text(this.font, label, x, y, MUTED, false);
        int nameX = HerdBookHeads.AVAILABLE ? valueX + ICON + 2 : valueX;
        if (parent.isPresent() && this.entries.containsKey(parent.get())) {
            HerdEntry known = this.entries.get(parent.get());
            HerdBookHeads.draw(graphics, known.id(), known.species(), known.kid(), valueX, y - 1, ICON);
            link(graphics, fit(this.names.get(known.id()), x + TEXT_WIDTH - nameX), known.id(), nameX, y);
        } else {
            if (HerdBookHeads.AVAILABLE) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, UNKNOWN_ICON, valueX, y - 1, ICON, ICON);
            }
            graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.unknown"), nameX, y, MUTED, false);
        }
    }

    // Young as a row of heads, wrapping while there is room; hover one for its name, click it for its page.
    private void drawYoungHeads(GuiGraphicsExtractor graphics, List<HerdEntry> young, int x0, int right, int y) {
        int perRow = Math.max(1, (right - x0 + YOUNG_GAP) / (ICON + YOUNG_GAP));
        int rows = Math.max(1, (top() + BUTTONS_Y - 1 - y) / STATUS_LINE);
        int shown = HerdBookLayout.youngShown(young.size(), perRow * rows);
        for (int i = 0; i < shown; i++) {
            HerdEntry child = young.get(i);
            int headX = x0 + (i % perRow) * (ICON + YOUNG_GAP);
            int headY = y - 1 + (i / perRow) * STATUS_LINE;
            HerdBookHeads.draw(graphics, child.id(), child.species(), child.kid(), headX, headY, ICON);
            this.hits.add(new Hit(headX, headY, headX + ICON, headY + ICON, () -> showAnimal(child.id()), Component.literal(this.names.get(child.id()))));
        }
        if (shown < young.size()) {
            int moreX = x0 + (shown % perRow) * (ICON + YOUNG_GAP);
            graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.more", young.size() - shown), moreX,
                    y + (shown / perRow) * STATUS_LINE, MUTED, false);
        }
    }

    // Without heads (1.21.1), young are listed by name, one per line.
    private void drawYoungNames(GuiGraphicsExtractor graphics, List<HerdEntry> young, int x, int y) {
        int linesLeft = Math.max(0, (top() + BUTTONS_Y - LINE - y) / LINK_LINE + 1);
        int shown = HerdBookLayout.youngShown(young.size(), linesLeft);
        for (HerdEntry child : young.subList(0, shown)) {
            link(graphics, fit(this.names.get(child.id()), TEXT_WIDTH - 8), child.id(), x + 8, y);
            y += LINK_LINE;
        }
        if (shown < young.size()) {
            graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.more", young.size() - shown), x + 8, y, MUTED, false);
        }
    }

    private void link(GuiGraphicsExtractor graphics, String name, UUID id, int x, int y) {
        HerdEntry target = this.entries.get(id);
        int color = target != null && target.alive() ? LINK : MUTED;
        graphics.text(this.font, name, x, y, color, false);
        graphics.fill(x, y + 8, x + this.font.width(name), y + 9, color);
        this.hits.add(new Hit(x, y - 1, x + this.font.width(name), y + LINE, () -> showAnimal(id), null));
    }

    // The return arrow between the page arrows goes back to the contents; the cross on the right removes the animal
    // from the book. Both turn red under the mouse, like the page arrows.
    private void drawAnimalButtons(GuiGraphicsExtractor graphics, HerdEntry entry, int mouseX, int mouseY) {
        int y = top() + BUTTONS_Y;
        int backX = left() + TEXT_X + RETURN_X;
        Hit back = new Hit(backX, y, backX + RETURN_WIDTH, y + RETURN_HEIGHT, () -> showContents(this.species, contentsPage(entry)),
                Component.translatable("gui.barnyard_romance.herd_book.back"));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, back.contains(mouseX, mouseY) ? RETURN_HIGHLIGHTED : RETURN_ICON,
                backX, y, RETURN_WIDTH, RETURN_HEIGHT);
        this.hits.add(back);
        int removeX = left() + TEXT_X + TEXT_WIDTH - REMOVE_WIDTH;
        Hit remove = new Hit(removeX, y, removeX + REMOVE_WIDTH, y + REMOVE_HEIGHT, this::pressRemove,
                Component.translatable("gui.barnyard_romance.herd_book.remove"));
        boolean red = this.removeArmed || remove.contains(mouseX, mouseY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, red ? REMOVE_HIGHLIGHTED : REMOVE_ICON, removeX, y, REMOVE_WIDTH, REMOVE_HEIGHT);
        this.hits.add(remove);
        if (this.removeArmed) {
            Component sure = Component.translatable("gui.barnyard_romance.herd_book.sure");
            int sureX = removeX - 2 - this.font.width(sure);
            graphics.text(this.font, sure, sureX, y + 3, HUNGRY, false);
            this.hits.add(new Hit(sureX, y, removeX, y + REMOVE_HEIGHT, this::pressRemove, Component.translatable("gui.barnyard_romance.herd_book.remove")));
        }
    }

    private static String shearKey(ShearState state) {
        return switch (state) {
            case SHEARED_TODAY -> "gui.barnyard_romance.herd_book.sheared_today";
            case REGROWING -> "gui.barnyard_romance.herd_book.wool_regrowing";
            default -> "gui.barnyard_romance.herd_book.shear_ready";
        };
    }

    private int contentsPage(HerdEntry entry) {
        return entry.alive() ? HerdBookLayout.pageOf(contents(), entry.id()) : 0;
    }

    private void page(GuiGraphicsExtractor graphics, Component title, int pages) {
        graphics.text(this.font, title, left() + TEXT_X, top() + 16, HEADER, false);
        if (pages > 1) {
            Component number = Component.translatable("gui.barnyard_romance.herd_book.page", this.page + 1, pages);
            graphics.text(this.font, number, left() + TEXT_X + TEXT_WIDTH - this.font.width(number), top() + 16, MUTED, false);
        }
    }

    private String fit(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("…"))) + "…";
    }

    private static String speciesName(String species) {
        Identifier id = Identifier.tryParse(species);
        return id == null ? species : BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .map(type -> type.getDescription().getString())
                .orElse(species);
    }

    private int left() {
        return (this.width - SIZE) / 2;
    }

    private int top() {
        return 2;
    }
}
