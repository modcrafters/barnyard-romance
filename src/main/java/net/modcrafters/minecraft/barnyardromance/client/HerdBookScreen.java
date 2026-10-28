package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.herd.Familiar;
import net.modcrafters.minecraft.barnyardromance.herd.HerdEntry;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// The herd book: species bookmarks on the right edge, a contents page per species, and a page per animal.
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
    private static final int FED = 0xFF4F8A2B;
    private static final int HUNGRY = 0xFFB5482F;
    private static final int MILK = 0xFF4A76A8;
    private static final int FROZEN = 0xFF8A8A8A;
    private static final int TAB = 0xFF8A5A22;
    private static final int TAB_ON = 0xFFE8D8B0;
    private static final int TAB_TEXT = 0xFFFFE3A1;
    private static final String FEMALE_MARK = "♀";
    private static final String MALE_MARK = "♂";

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
        this.forwardButton = addRenderableWidget(new PageButton(left + TEXT_X + 44, BUTTONS_Y + top(), true, button -> forward(), true));
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
        if (event.button() == 0 && click(event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && click(x, y)) {
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
    *///?}

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
            List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(Component.translatable("gui.barnyard_romance.herd_book.empty"), TEXT_WIDTH);
            for (int i = 0; i < lines.size(); i++) {
                graphics.text(this.font, lines.get(i), x, y + i * LINE, TEXT, false);
            }
        } else if (this.animal != null && this.entries.containsKey(this.animal)) {
            drawAnimal(graphics, this.entries.get(this.animal), x, y);
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

    private void drawBookmarks(GuiGraphicsExtractor graphics) {
        List<String> bookmarks = HerdBookLayout.bookmarks(this.rows);
        int height = HerdBookLayout.tabHeight(bookmarks.size());
        int columns = HerdBookLayout.tabColumns(bookmarks.size());
        int perColumn = (bookmarks.size() + columns - 1) / columns;
        for (int i = 0; i < bookmarks.size(); i++) {
            String tab = bookmarks.get(i);
            // Further columns stand out past the first, like a second row of bookmarks.
            int x0 = left() + SIZE - 34 + (i / perColumn) * 26;
            int y = top() + 8 + (i % perColumn) * height;
            boolean on = tab.equals(this.species);
            int x1 = x0 + (on ? 26 : 22);
            graphics.fill(x0, y, x1, y + height - 1, on ? TAB_ON : TAB);
            String name = speciesName(tab);
            String letters = this.font.plainSubstrByWidth(name, x1 - x0 - 4);
            graphics.text(this.font, letters, x0 + 3, y + (height - 8) / 2, on ? TEXT : TAB_TEXT, false);
            this.hits.add(new Hit(x0, y, x1, y + height - 1, () -> showContents(tab, 0), Component.literal(name)));
        }
    }

    private void drawContents(GuiGraphicsExtractor graphics, int x, int y) {
        List<List<HerdBookLayout.Line>> pages = contents();
        page(graphics, Component.literal(speciesName(this.species)), pages.size());
        List<HerdBookLayout.Line> lines = pages.get(Math.min(this.page, pages.size() - 1));
        for (int i = 0; i < lines.size(); i++) {
            int lineY = y + i * LINE;
            switch (lines.get(i)) {
                case HerdBookLayout.Header header -> graphics.text(this.font, Component.translatable(switch (header.group()) {
                    case FEMALES -> "gui.barnyard_romance.herd_book.females";
                    case MALES -> "gui.barnyard_romance.herd_book.males";
                    case KIDS -> "gui.barnyard_romance.herd_book.kids";
                }), x, lineY, HEADER, false);
                case HerdBookLayout.Animal row -> drawRow(graphics, this.entries.get(row.id()), x, lineY);
            }
        }
    }

    private void drawRow(GuiGraphicsExtractor graphics, HerdEntry entry, int x, int y) {
        graphics.text(this.font, entry.female() ? FEMALE_MARK : MALE_MARK, x, y, entry.female() ? FEMALE : MALE, false);
        int iconsX = drawIcons(graphics, entry, x + TEXT_WIDTH, y);
        int nameX = x + 8;
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
            String percent = entry.pregnancyPercent() + "%";
            x -= this.font.width(percent);
            graphics.text(this.font, percent, x, y, FEMALE, false);
            this.hits.add(new Hit(x, y, x + this.font.width(percent), y + 8, null,
                    Component.translatable("gui.barnyard_romance.herd_book.pregnant_icon", entry.pregnancyPercent())));
            x -= 3;
        } else if (entry.milkReady()) {
            x -= 4;
            graphics.fill(x, y + 1, x + 4, y + 7, MILK);
            this.hits.add(new Hit(x - 1, y, x + 5, y + 8, null, Component.translatable("gui.barnyard_romance.herd_book.milk_icon")));
            x -= 3;
        }
        x -= 4;
        graphics.fill(x, y + 2, x + 4, y + 6, entry.fedToday() ? FED : HUNGRY);
        this.hits.add(new Hit(x - 1, y, x + 5, y + 8, null, Component.translatable(entry.fedToday()
                ? "gui.barnyard_romance.herd_book.fed_today"
                : "gui.barnyard_romance.herd_book.not_fed_today")));
        return x;
    }

    private void drawAnimal(GuiGraphicsExtractor graphics, HerdEntry entry, int x, int y) {
        int lineY = y;
        graphics.text(this.font, entry.female() ? FEMALE_MARK : MALE_MARK, x, lineY, entry.female() ? FEMALE : MALE, false);
        graphics.text(this.font, fit(this.names.get(entry.id()), TEXT_WIDTH - 8), x + 8, lineY, entry.alive() ? TEXT : MUTED, false);
        lineY += LINE + 2;
        for (Component line : entry.lines()) {
            graphics.text(this.font, line, x, lineY, TEXT, false);
            lineY += LINE;
        }
        lineY += 2;
        graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.family"), x, lineY, HEADER, false);
        lineY += LINE;
        lineY = drawLink(graphics, Component.translatable("gui.barnyard_romance.herd_book.mother"), entry.mother(), x, lineY);
        lineY = drawLink(graphics, Component.translatable("gui.barnyard_romance.herd_book.father"), entry.father(), x, lineY);
        List<HerdEntry> young = this.entries.values().stream()
                .filter(other -> other.mother().equals(Optional.of(entry.id())) || other.father().equals(Optional.of(entry.id())))
                .sorted(Comparator.comparing((HerdEntry child) -> this.names.get(child.id())).thenComparing(HerdEntry::id))
                .toList();
        if (!young.isEmpty()) {
            graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.young"), x, lineY, MUTED, false);
            lineY += LINK_LINE;
            int linesLeft = Math.max(0, (top() + BUTTONS_Y - LINE - lineY) / LINK_LINE + 1);
            int shown = HerdBookLayout.youngShown(young.size(), linesLeft);
            for (HerdEntry child : young.subList(0, shown)) {
                link(graphics, fit(this.names.get(child.id()), TEXT_WIDTH - 8), child.id(), x + 8, lineY);
                lineY += LINK_LINE;
            }
            if (shown < young.size()) {
                graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.more", young.size() - shown), x + 8, lineY, MUTED, false);
            }
        }
        drawAnimalButtons(graphics, entry);
    }

    private int drawLink(GuiGraphicsExtractor graphics, Component label, Optional<UUID> parent, int x, int y) {
        graphics.text(this.font, label, x, y, MUTED, false);
        int linkX = x + this.font.width(label) + 4;
        if (parent.isPresent() && this.entries.containsKey(parent.get())) {
            link(graphics, fit(this.names.get(parent.get()), x + TEXT_WIDTH - linkX), parent.get(), linkX, y);
        } else {
            graphics.text(this.font, Component.translatable("gui.barnyard_romance.herd_book.unknown"), linkX, y, MUTED, false);
        }
        return y + LINK_LINE;
    }

    private void link(GuiGraphicsExtractor graphics, String name, UUID id, int x, int y) {
        HerdEntry target = this.entries.get(id);
        int color = target != null && target.alive() ? LINK : MUTED;
        graphics.text(this.font, name, x, y, color, false);
        graphics.fill(x, y + 8, x + this.font.width(name), y + 9, color);
        this.hits.add(new Hit(x, y - 1, x + this.font.width(name), y + LINE, () -> showAnimal(id), null));
    }

    // ↩ between the arrows goes back to the contents; ✕ on the right removes the animal from the book.
    private void drawAnimalButtons(GuiGraphicsExtractor graphics, HerdEntry entry) {
        int y = top() + BUTTONS_Y + 3;
        int backX = left() + TEXT_X + 30;
        graphics.text(this.font, "↩", backX, y, MUTED, false);
        List<List<HerdBookLayout.Line>> pages = contents();
        int contentsPage = entry.alive() ? HerdBookLayout.pageOf(pages, entry.id()) : 0;
        this.hits.add(new Hit(backX - 2, y - 2, backX + 10, y + 10, () -> showContents(this.species, contentsPage),
                Component.translatable("gui.barnyard_romance.herd_book.back")));
        Component remove = this.removeArmed
                ? Component.translatable("gui.barnyard_romance.herd_book.sure")
                : Component.literal("✕");
        int removeX = left() + TEXT_X + TEXT_WIDTH - this.font.width(remove);
        graphics.text(this.font, remove, removeX, y, HUNGRY, false);
        this.hits.add(new Hit(removeX - 2, y - 2, removeX + this.font.width(remove) + 2, y + 10, this::pressRemove,
                Component.translatable("gui.barnyard_romance.herd_book.remove")));
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
