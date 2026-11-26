package net.modcrafters.minecraft.barnyardromance.pail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

record MetalPail(Identifier material, int tint, Variant variant) {
    private static final Pattern HEX_TINT = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Codec<Identifier> MATERIAL_CODEC = Identifier.CODEC.validate(material ->
            material.getPath().chars().anyMatch(Character::isLetterOrDigit) ? DataResult.success(material)
                    : DataResult.error(() -> "Expected a material id such as lead or example:dark_steel"));
    private static final Codec<Integer> TINT_CODEC = Codec.STRING.comapFlatMap(
            tint -> HEX_TINT.matcher(tint).matches() ? DataResult.success(HexFormat.fromHexDigits(tint, 1, 7))
                    : DataResult.error(() -> "Expected a pail tint in #RRGGBB format"),
            tint -> "#" + HexFormat.of().toHexDigits(tint, 6));

    static final Codec<MetalPail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MATERIAL_CODEC.fieldOf("material").forGetter(MetalPail::material),
            TINT_CODEC.fieldOf("tint").forGetter(MetalPail::tint),
            Variant.CODEC.fieldOf("variant").forGetter(MetalPail::variant)
    ).apply(instance, MetalPail::new));

    enum Variant implements StringRepresentable {
        COPPER("copper"), IRON("iron");

        private static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    Item item() {
        return switch (variant) {
            case COPPER -> PailItems.COPPER_PAIL;
            case IRON -> PailItems.IRON_PAIL;
        };
    }

    DataComponentPatch components() {
        String title = Arrays.stream(material.getPath().split("[._/-]+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .collect(Collectors.joining(" "));
        Component materialName = Component.translatableWithFallback(material.toLanguageKey("pail_material.barnyard_romance"), title);
        DataComponentPatch.Builder components = DataComponentPatch.builder();
        components.set(DataComponents.ITEM_NAME, Component.translatableWithFallback("item.barnyard_romance.pail.named", "%s Pail", materialName));
        // The tint is the pail's metal, not a dye, so the tooltip doesn't say "Dyed".
        //? if >=1.21.5 {
        components.set(DataComponents.DYED_COLOR, new DyedItemColor(tint));
        components.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true));
        //?} else
        //components.set(DataComponents.DYED_COLOR, new DyedItemColor(tint, false));
        return components.build();
    }
}
