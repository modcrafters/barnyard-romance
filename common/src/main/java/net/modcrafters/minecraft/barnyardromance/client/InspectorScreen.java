package net.modcrafters.minecraft.barnyardromance.client;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.inspect.InspectorSheet;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

public class InspectorScreen extends Screen {
    private static final int CREAM = 0xFFF4E4C4;
    private static final int CREAM_DIM = 0xFFD9C7A4;
    private static final int LABEL = 0xFFF0E2C4;
    private static final int VALUE = 0xFFF6EBD2;
    private static final int SCALE = 0xFFE7D7B0;
    private static final int HINT = 0xFFCDB892;
    private static final int ESC = 0xFFB89A62;
    private static final int INK = 0xFF3A2412;
    private static final int FATHER = 0xFFE4D2AE;
    private static final int FLOOR = 0xFF1A120C;
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");

    private final Animal animal;

    public InspectorScreen(Animal animal) {
        super(Component.literal("Barnyard Inspector"));
        this.animal = animal;
    }

    public static void open(Animal animal) {
        Minecraft.getInstance().setScreenAndShow(new InspectorScreen(animal));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.animal.isRemoved()) {
            this.onClose();
            return;
        }
        this.extractTransparentBackground(graphics);
        InspectorSheet sheet = sheet();
        int x = (this.width - InspectorLayout.WIDTH) / 2;
        int y = (this.height - InspectorLayout.HEIGHT) / 2;
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace("popup/background"),
                x,
                y,
                InspectorLayout.WIDTH,
                InspectorLayout.HEIGHT
        );
        portrait(graphics, x, y);
        nameplate(graphics, x, y, sheet);
        if (sheet.known()) {
            header(graphics, x, y, sheet);
            meter(graphics, x, y, InspectorLayout.ROW_FAMILIAR, "heart", "Familiar", sheet.familiarity(), "fill_familiar", sheet.familiarityText());
            meter(graphics, x, y, InspectorLayout.ROW_FEED, "wheat", "Feed", sheet.feed(), "fill_feed", sheet.feedText());
            growth(graphics, x, y, sheet);
            health(graphics, x, y, sheet);
            if (sheet.showPregnancy()) {
                pregnancy(graphics, x, y, sheet);
            }
        } else {
            graphics.text(this.font, "No barnyard traits yet", x + InspectorLayout.STATS_X, y + InspectorLayout.HEADER_Y + 4, CREAM);
            health(graphics, x, y, sheet);
        }
        graphics.centeredText(this.font, "Esc to close", x + InspectorLayout.WIDTH / 2, y + InspectorLayout.ESC_Y, ESC);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void portrait(GuiGraphicsExtractor graphics, int x, int y) {
        int frameX = x + InspectorLayout.PORTRAIT_X;
        int frameY = y + InspectorLayout.PORTRAIT_Y;
        int x0 = frameX + InspectorLayout.PORTRAIT_INSET;
        int y0 = frameY + InspectorLayout.PORTRAIT_INSET;
        int x1 = frameX + InspectorLayout.PORTRAIT - InspectorLayout.PORTRAIT_INSET;
        int y1 = frameY + InspectorLayout.PORTRAIT - InspectorLayout.PORTRAIT_INSET;
        graphics.fill(x0, y0, x1, y1, FLOOR);
        int entityScale = InspectorLayout.portraitScale(y1 - y0, this.animal.getBbHeight(), this.animal.getBbWidth());
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
            // A modded renderer that cannot draw in a GUI leaves the empty frame.
        }
        graphics.disableScissor();
        sprite(graphics, "portrait", frameX, frameY, InspectorLayout.PORTRAIT, InspectorLayout.PORTRAIT);
    }

    private void nameplate(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        int plateX = x + InspectorLayout.NAME_X;
        int plateY = y + InspectorLayout.NAME_Y;
        sprite(graphics, "nameplate", plateX, plateY, InspectorLayout.NAME_W, InspectorLayout.NAME_H);
        int center = plateX + InspectorLayout.NAME_W / 2;
        graphics.enableScissor(plateX + 12, plateY + 4, plateX + InspectorLayout.NAME_W - 12, plateY + InspectorLayout.NAME_H - 4);
        if (sheet.subtitle().isEmpty()) {
            graphics.centeredText(this.font, sheet.title(), center, plateY + 16, CREAM);
        } else {
            graphics.centeredText(this.font, sheet.title(), center, plateY + 8, CREAM);
            graphics.centeredText(this.font, sheet.subtitle(), center, plateY + 20, CREAM_DIM);
        }
        graphics.disableScissor();
    }

    private void header(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        int rowX = x + InspectorLayout.STATS_X;
        int rowY = y + InspectorLayout.HEADER_Y;
        sprite(graphics, sheet.female() ? "female" : "male", rowX, rowY + 1, InspectorLayout.ICON, InspectorLayout.ICON);
        int badgeX = rowX + 18;
        sprite(graphics, "badge", badgeX, rowY, 28, 16);
        graphics.centeredText(this.font, sheet.sizeLetter(), badgeX + 14, rowY + 4, INK);
        int plaqueX = badgeX + 32;
        sprite(graphics, "plaque", plaqueX, rowY, 56, 16);
        graphics.centeredText(this.font, sheet.life(), plaqueX + 28, rowY + 4, CREAM);
        int scaleX = x + InspectorLayout.WIDTH - 14 - this.font.width(sheet.scaleText());
        graphics.text(this.font, sheet.scaleText(), scaleX, rowY + 4, SCALE);
    }

    private void meter(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int row,
            String icon,
            String label,
            double fill,
            String fillSprite,
            String value
    ) {
        int rowX = x + InspectorLayout.STATS_X;
        int rowY = y + row;
        sprite(graphics, icon, rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        graphics.text(this.font, label, rowX + 18, rowY + 3, LABEL);
        int barX = x + InspectorLayout.barX();
        int barY = rowY + 4;
        sprite(graphics, "bar", barX, barY, InspectorLayout.BAR_W, 6);
        int filled = (int) Math.round(Math.clamp(fill, 0.0, 1.0) * (InspectorLayout.BAR_W - 2));
        if (filled > 0) {
            sprite(graphics, fillSprite, barX + 1, barY + 1, filled, 4);
        }
        graphics.text(this.font, value, barX + InspectorLayout.BAR_W + 6, rowY + 3, VALUE);
    }

    private void growth(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        meter(graphics, x, y, InspectorLayout.ROW_GROWTH, "hourglass", "Growth", 0.0, "fill_familiar", sheet.growthText());
        int barX = x + InspectorLayout.barX();
        int barY = y + InspectorLayout.ROW_GROWTH + 2;
        sprite(graphics, "marker", InspectorLayout.markerX(barX, InspectorLayout.BAR_W, sheet.growth()), barY, InspectorLayout.MARKER_W, 10);
        int labelY = y + InspectorLayout.ROW_GROWTH + 14;
        graphics.text(this.font, "Slow", barX, labelY, HINT);
        graphics.text(this.font, "Fast", barX + InspectorLayout.BAR_W - this.font.width("Fast"), labelY, HINT);
    }

    private void health(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        int rowX = x + InspectorLayout.STATS_X;
        int rowY = y + InspectorLayout.ROW_HEALTH;
        sprite(graphics, "gold_heart", rowX, rowY, InspectorLayout.ICON, InspectorLayout.ICON);
        graphics.text(this.font, "Health", rowX + 18, rowY + 3, LABEL);
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
        graphics.text(this.font, sheet.healthText(), heartsX + InspectorLayout.BAR_W + 6, rowY + 3, VALUE);
    }

    private void pregnancy(GuiGraphicsExtractor graphics, int x, int y, InspectorSheet sheet) {
        String value = sheet.pregnant() ? sheet.pregnancyText() : "None";
        meter(graphics, x, y, InspectorLayout.ROW_PREGNANT, "gold_heart", "Pregnant", sheet.pregnancy(), "fill_pregnant", value);
        if (!sheet.fatherText().isEmpty()) {
            int barX = x + InspectorLayout.barX();
            int fatherX = barX + (InspectorLayout.BAR_W - this.font.width(sheet.fatherText())) / 2;
            graphics.text(this.font, sheet.fatherText(), fatherX, y + InspectorLayout.ROW_PREGNANT + 14, FATHER);
        }
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
        Optional<Pregnancy> pregnancy = Services.DATA.getPregnancy(this.animal);
        String father = pregnancy.map(current -> InspectorSheet.father(
                SizeClass.of(Sex.MALE, current.fatherScale(), BarnyardConfig.current().sizeFor(current.fatherSpecies())).name(),
                current.fatherSpecies()
        )).orElse("");
        return new InspectorSheet(
                named ? name : typeName,
                named ? typeName : "",
                true,
                value.sex() == Sex.FEMALE,
                SizeClass.of(value.sex(), value.scale(), BarnyardConfig.current().sizeFor(AnimalSex.speciesId(this.animal))).name(),
                this.animal.isBaby() ? "Baby" : "Adult",
                InspectorSheet.scaleText(value.scale()),
                value.familiarity(),
                Familiarity.percent(value.familiarity()),
                Familiarity.feedReady(value.lastFedTick(), now),
                InspectorSheet.feedText(value.lastFedTick(), now),
                Growth.percent(value.growthTicks()),
                Growth.clock(value.growthTicks()),
                health,
                maxHealth,
                pregnancy.isPresent(),
                pregnancy.map(current -> PregnancyClock.progress(now, current.conceivedAt(), current.durationTicks())).orElse(0.0),
                pregnancy.map(current -> PregnancyText.clock(PregnancyClock.remainingTicks(now, current.conceivedAt(), current.durationTicks()))).orElse(""),
                father
        );
    }

    private static void sprite(GuiGraphicsExtractor graphics, String name, int x, int y, int width, int height) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "inspector/" + name),
                x,
                y,
                width,
                height
        );
    }
}
