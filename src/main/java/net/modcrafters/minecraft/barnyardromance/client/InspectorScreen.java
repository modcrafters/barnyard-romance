package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.age.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.age.AgeStage;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorRow;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorSheet;
import net.modcrafters.minecraft.barnyardromance.milk.MilkActions;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Fertility;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyClock;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyText;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Growth;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.sex.SizeClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//? if >=1.21.6
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.List;
import java.util.Optional;

public class InspectorScreen extends Screen {
    // Vanilla container colors: the classic panel, its title text, and the inventory's sunken entity box.
    private static final int TEXT = 0xFF404040;
    private static final int MUTED = 0xFF707070;
    private static final int GOOD = 0xFF2F6B1F;
    private static final int BAD = 0xFF8B2020;
    private static final int SHADE = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int FLOOR = 0xFF000000;
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");

    private final Animal animal;

    public InspectorScreen(Animal animal) {
        super(Component.translatable("item.barnyard_romance.inspector"));
        this.animal = animal;
    }

    public static void open(Animal animal) {
        //? if >=26.1 {
        Minecraft.getInstance().setScreenAndShow(new InspectorScreen(animal));
        //?} else
        //Minecraft.getInstance().setScreen(new InspectorScreen(animal));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    //? if >=26.1 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*// The default background blurs the world, which would hide the animal behind the panel.
    @Override
    public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    *///?}
        if (this.animal.isRemoved()) {
            this.onClose();
            return;
        }
        //? if >=26.1 {
        this.extractTransparentBackground(graphics);
        //?} else
        //super.render(graphics, mouseX, mouseY, partialTick);
        InspectorSheet sheet = sheet();
        int heartRows = HerdCardLayout.heartRows(HerdCardLayout.hearts(sheet.maxHealth()));
        List<InspectorLayout.Placed> rows = InspectorLayout.stack(sheet.rows(), heartRows);
        int panelHeight = InspectorLayout.panelHeight(rows);
        int x = (this.width - InspectorLayout.WIDTH) / 2;
        int y = (this.height - panelHeight) / 2;
        panel(graphics, x, y, InspectorLayout.WIDTH, panelHeight);
        title(graphics, x, y, sheet);
        portrait(graphics, x, y);
        if (sheet.known()) {
            header(graphics, x, y, sheet);
        } else {
            text(graphics, Text.of("gui.barnyard_romance.inspector.no_traits"), x + InspectorLayout.STATS_X, y + InspectorLayout.HEADER_Y + 3, MUTED);
        }
        for (InspectorLayout.Placed row : rows) {
            row(graphics, x, y + row.y(), row.row(), sheet);
        }
        //? if >=26.1
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void row(GuiGraphicsExtractor graphics, int x, int rowY, InspectorRow row, InspectorSheet sheet) {
        switch (row) {
            case FAMILIAR -> meter(graphics, x, rowY, "heart", Text.of("gui.barnyard_romance.inspector.familiar"), sheet.familiarity(), "fill_familiar", sheet.familiarityText());
            case FEED -> meter(graphics, x, rowY, "wheat", Text.of("gui.barnyard_romance.inspector.feed"), sheet.feed(), "fill_feed", sheet.feedText());
            case GROWTH -> growth(graphics, x, rowY, sheet);
            case WEIGHT -> meter(graphics, x, rowY, "weight", Text.of("gui.barnyard_romance.inspector.weight"), sheet.weight(), "fill_feed", sheet.weightText());
            case HEALTH -> health(graphics, x, rowY, sheet);
            case FERTILITY -> meter(graphics, x, rowY, "fertility", Text.of("gui.barnyard_romance.inspector.fertility"), sheet.fertility(), "fill_pregnant", sheet.fertilityText());
            case MILK -> milk(graphics, x, rowY, sheet);
            case BREEDING -> breeding(graphics, x, rowY, sheet);
            case DETAIL -> text(graphics, sheet.detailText(), x + InspectorLayout.barX(), rowY + 3, MUTED);
        }
    }

    private static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        for (ClassicPanel.Rect rect : ClassicPanel.rects(width, height)) {
            graphics.fill(x + rect.x0(), y + rect.y0(), x + rect.x1(), y + rect.y1(), rect.color());
        }
    }

    private void title(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        int titleX = x + InspectorLayout.TITLE_X;
        int titleY = y + InspectorLayout.TITLE_Y;
        text(graphics, Text.plain(sheet.title()), titleX, titleY, TEXT);
        if (!sheet.subtitle().isEmpty()) {
            text(graphics, Text.plain(sheet.subtitle()), titleX + this.font.width(sheet.title()) + 6, titleY, MUTED);
        }
        if (!sheet.scaleText().isEmpty()) {
            Component scale = TextComponents.of(sheet.scaleText());
            int scaleX = x + InspectorLayout.WIDTH - InspectorLayout.TITLE_X - this.font.width(scale);
            text(graphics, scale, scaleX, titleY, MUTED);
        }
    }

    private void portrait(GuiGraphicsExtractor graphics, int x, int y) {
        int x0 = x + InspectorLayout.PORTRAIT_X;
        int y0 = y + InspectorLayout.PORTRAIT_Y;
        int x1 = x0 + InspectorLayout.PORTRAIT;
        int y1 = y0 + InspectorLayout.PORTRAIT;
        graphics.fill(x0 - 1, y0 - 1, x1, y0, SHADE);
        graphics.fill(x0 - 1, y0, x0, y1, SHADE);
        graphics.fill(x0, y1, x1 + 1, y1 + 1, LIGHT);
        graphics.fill(x1, y0, x1 + 1, y1, LIGHT);
        graphics.fill(x0, y0, x1, y1, FLOOR);
        int entityScale = InspectorLayout.portraitScale(InspectorLayout.PORTRAIT, this.animal.getBbHeight(), this.animal.getBbWidth());
        graphics.enableScissor(x0, y0, x1, y1);
        try {
            float centerX = (x0 + x1) / 2.0F;
            float centerY = (y0 + y1) / 2.0F;
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    graphics,
                    x0,
                    y0,
                    x1,
                    y1,
                    entityScale,
                    0.0F,
                    centerX - 48.0F,
                    centerY - 22.0F,
                    this.animal
            );
        } catch (RuntimeException ignored) {
            // A modded renderer that cannot draw in a GUI leaves the empty box.
        }
        graphics.disableScissor();
    }

    private void header(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        int rowX = x + InspectorLayout.STATS_X;
        int rowY = y + InspectorLayout.HEADER_Y;
        sprite(graphics, sheet.female() ? "female" : "male", rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        Text line = Text.of("gui.barnyard_romance.inspector.header", sheet.sexText(), sheet.sizeLetter(), sheet.stage().text());
        text(graphics, line, rowX + InspectorLayout.ICON + 4, rowY + 3, TEXT);
    }

    private void meter(
            GuiGraphicsExtractor graphics,
            int x,
            int rowY,
            String icon,
            Text label,
            double fill,
            String fillSprite,
            Text value
    ) {
        int rowX = x + InspectorLayout.STATS_X;
        sprite(graphics, icon, rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        text(graphics, label, rowX + InspectorLayout.ICON + 4, rowY + 3, TEXT);
        int barX = x + InspectorLayout.barX();
        int barY = rowY + 4;
        sprite(graphics, "bar", barX, barY, InspectorLayout.BAR_W, 6);
        int filled = (int) Math.round(Math.clamp(fill, 0.0, 1.0) * (InspectorLayout.BAR_W - 2));
        if (filled > 0) {
            sprite(graphics, fillSprite, barX + 1, barY + 1, filled, 4);
        }
        text(graphics, value, x + InspectorLayout.valueX(), rowY + 3, TEXT);
    }

    private void growth(GuiGraphicsExtractor graphics, int x, int rowY, InspectorSheet sheet) {
        meter(graphics, x, rowY, "hourglass", Text.of("gui.barnyard_romance.inspector.growth"), 0.0, "fill_familiar", sheet.growthText());
        int barX = x + InspectorLayout.barX();
        sprite(graphics, "marker", InspectorLayout.markerX(barX, InspectorLayout.BAR_W, sheet.growth()), rowY + 2, InspectorLayout.MARKER_W, 10);
        int labelY = rowY + 14;
        Component fast = Component.translatable("gui.barnyard_romance.inspector.fast");
        text(graphics, Text.of("gui.barnyard_romance.inspector.slow"), barX, labelY, MUTED);
        text(graphics, fast, barX + InspectorLayout.BAR_W - this.font.width(fast), labelY, MUTED);
    }

    private void health(GuiGraphicsExtractor graphics, int x, int rowY, InspectorSheet sheet) {
        int rowX = x + InspectorLayout.STATS_X;
        sprite(graphics, "gold_heart", rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        text(graphics, Text.of("gui.barnyard_romance.inspector.health"), rowX + InspectorLayout.ICON + 4, rowY + 3, TEXT);
        int count = HerdCardLayout.hearts(sheet.maxHealth());
        int heartsX = x + InspectorLayout.barX();
        for (int i = 0; i < count; i++) {
            int hx = heartsX + (i % HerdCardLayout.HEARTS_PER_ROW) * HerdCardLayout.HEART_STEP;
            int hy = rowY + 2 + (i / HerdCardLayout.HEARTS_PER_ROW) * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, hx, hy, 9, 9);
            float remaining = sheet.health() - i * 2.0F;
            if (remaining >= 2.0F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, hx, hy, 9, 9);
            } else if (remaining >= 1.0F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF, hx, hy, 9, 9);
            }
        }
        text(graphics, Text.plain(sheet.healthText()), x + InspectorLayout.valueX(), rowY + 3, TEXT);
    }

    private void milk(GuiGraphicsExtractor graphics, int x, int rowY, InspectorSheet sheet) {
        int rowX = x + InspectorLayout.STATS_X;
        sprite(graphics, "milk", rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        text(graphics, Text.of("gui.barnyard_romance.inspector.milk"), rowX + InspectorLayout.ICON + 4, rowY + 3, TEXT);
        text(graphics, sheet.milkText(), x + InspectorLayout.barX(), rowY + 3, TEXT);
    }

    private void breeding(GuiGraphicsExtractor graphics, int x, int rowY, InspectorSheet sheet) {
        if (sheet.pregnant()) {
            meter(graphics, x, rowY, "gold_heart", Text.of("gui.barnyard_romance.inspector.pregnant"), sheet.pregnancy(), "fill_pregnant", sheet.pregnancyText());
            return;
        }
        int rowX = x + InspectorLayout.STATS_X;
        sprite(graphics, "gold_heart", rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        text(graphics, Text.of("gui.barnyard_romance.inspector.breeding"), rowX + InspectorLayout.ICON + 4, rowY + 3, TEXT);
        text(graphics, sheet.breedingText(), x + InspectorLayout.barX(), rowY + 3, sheet.breedingReady() ? GOOD : BAD);
    }

    private InspectorSheet sheet() {
        String name = this.animal.getName().getString();
        String typeName = this.animal.getType().getDescription().getString();
        boolean named = this.animal.hasCustomName();
        float health = this.animal.getHealth();
        float maxHealth = this.animal.getMaxHealth();
        Optional<AnimalTraits> traits = Services.DATA.get(this.animal);
        if (traits.isEmpty()) {
            return InspectorSheet.unknown(name, typeName, named, this.animal.isBaby(), health, maxHealth);
        }
        AnimalTraits value = traits.get();
        long now = this.animal.level().getGameTime();
        long clock = this.animal.level().getOverworldClockTime();
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(this.animal);
        Text father = pregnancy.map(current -> InspectorSheet.father(
                SizeClass.of(Sex.MALE, current.fatherScale(), BarnyardConfig.current().sizeFor(current.fatherSpecies())).name(),
                current.fatherSpecies()
        )).orElse(Text.EMPTY);
        // These only breed when tamed; ocelots trust instead, which never blocks breeding.
        boolean needsTaming = this.animal instanceof AbstractHorse || this.animal instanceof Wolf || this.animal instanceof Cat;
        boolean tamed = this.animal instanceof AbstractHorse horse ? horse.isTamed()
                : this.animal instanceof TamableAnimal pet && pet.isTame();
        boolean female = value.sex() == Sex.FEMALE;
        AgeSettings age = BarnyardConfig.current().ageFor(AnimalSex.speciesId(this.animal));
        AgeStage stage = AgeController.stage(this.animal, value);
        double fertility = Fertility.current(value, age, now);
        Text breeding = InspectorSheet.breeding(
                needsTaming,
                tamed,
                value.familiarity(),
                stage == AgeStage.YOUNG,
                female && Fertility.wornOut(value, age, now),
                value.loveUntil(),
                value.cooldownUntil(),
                now
        );
        Identifier offspringId = Identifier.tryParse(value.lastBirthSpecies());
        String offspring = offspringId == null || value.lastBirthSpecies().isBlank() ? "" : BuiltInRegistries.ENTITY_TYPE.getOptional(offspringId)
                .map(type -> type.getDescription().getString())
                .orElse("");
        return new InspectorSheet(
                named ? name : typeName,
                named ? typeName : "",
                true,
                female,
                SizeClass.of(value.sex(), value.scale(), BarnyardConfig.current().sizeFor(AnimalSex.speciesId(this.animal))).name(),
                stage,
                InspectorSheet.scaleText(value.scale()),
                value.familiarity(),
                Familiarity.text(value.familiarity(), FamiliarityCare.tamed(this.animal)),
                Familiarity.feedReady(value.lastFedTick(), clock),
                InspectorSheet.feedText(value.lastFedTick(), clock),
                Growth.percent(value.growthTicks()),
                Growth.clock(value.growthTicks()),
                value.weight(),
                Familiarity.percent(value.weight()),
                health,
                maxHealth,
                fertility,
                Familiarity.percent(fertility),
                MilkActions.status(this.animal, value, now, clock).map(InspectorSheet::milkText).orElse(Text.EMPTY),
                pregnancy.isPresent(),
                pregnancy.map(current -> PregnancyClock.progress(now, current)).orElse(0.0),
                pregnancy.map(current -> PregnancyText.clock(PregnancyClock.remainingTicks(now, current))).orElse(Text.EMPTY),
                father,
                InspectorSheet.readyToBreed(breeding),
                breeding,
                InspectorSheet.lastBirth(offspring, value.lastBirthTick(), now)
        );
    }

    private void text(GuiGraphicsExtractor graphics, Text value, int x, int y, int color) {
        text(graphics, TextComponents.of(value), x, y, color);
    }

    private void text(GuiGraphicsExtractor graphics, Component value, int x, int y, int color) {
        graphics.text(this.font, value, x, y, color, false);
    }

    private static void sprite(GuiGraphicsExtractor graphics, String name, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "inspector/" + name), x, y, width, height);
    }
}
