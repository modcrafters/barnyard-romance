package net.modcrafters.minecraft.barnyardromance.herd;

import net.modcrafters.minecraft.barnyardromance.eggs.EggOutlook;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// One animal as the herd book shows it, worked out on the server when the book is opened.
// pregnancyPercent is -1 when not pregnant; shear is NONE for animals that aren't sheared; eggs is what a hen lays
// tomorrow (empty for everything else) and eggTip says why; the page's lines are already translatable components.
public record HerdEntry(
        UUID id,
        String species,
        String name,
        boolean female,
        boolean kid,
        boolean alive,
        Familiar familiar,
        boolean fedToday,
        int pregnancyPercent,
        boolean milkReady,
        ShearState shear,
        Optional<EggOutlook.Kind> eggs,
        Component eggTip,
        Optional<UUID> mother,
        Optional<UUID> father,
        List<Line> lines
) {
    // A line of the animal's page; see HerdPageText.Line.
    public record Line(HerdPageText.Kind kind, Component text, float fill) {
        private static final HerdPageText.Kind[] KINDS = HerdPageText.Kind.values();
        public static final StreamCodec<RegistryFriendlyByteBuf, Line> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT.map(i -> KINDS[Math.clamp(i, 0, KINDS.length - 1)], HerdPageText.Kind::ordinal), Line::kind,
                ComponentSerialization.STREAM_CODEC, Line::text,
                ByteBufCodecs.FLOAT, Line::fill,
                Line::new);
    }

    private static Optional<EggOutlook.Kind> eggs(int ordinal) {
        EggOutlook.Kind[] kinds = EggOutlook.Kind.values();
        return ordinal < 0 || ordinal >= kinds.length ? Optional.empty() : Optional.of(kinds[ordinal]);
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<UUID>> OPTIONAL_UUID = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Line>> LINES = Line.STREAM_CODEC.apply(ByteBufCodecs.list());
    // Written field by field: StreamCodec.composite only takes six fields on 1.21.1.
    public static final StreamCodec<RegistryFriendlyByteBuf, HerdEntry> STREAM_CODEC = StreamCodec.of(
            (buf, entry) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, entry.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.species());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.name());
                ByteBufCodecs.BOOL.encode(buf, entry.female());
                ByteBufCodecs.BOOL.encode(buf, entry.kid());
                ByteBufCodecs.BOOL.encode(buf, entry.alive());
                ByteBufCodecs.VAR_INT.encode(buf, entry.familiar().ordinal());
                ByteBufCodecs.BOOL.encode(buf, entry.fedToday());
                ByteBufCodecs.VAR_INT.encode(buf, entry.pregnancyPercent());
                ByteBufCodecs.BOOL.encode(buf, entry.milkReady());
                ByteBufCodecs.VAR_INT.encode(buf, entry.shear().ordinal());
                ByteBufCodecs.VAR_INT.encode(buf, entry.eggs().map(Enum::ordinal).orElse(-1));
                ComponentSerialization.STREAM_CODEC.encode(buf, entry.eggTip());
                OPTIONAL_UUID.encode(buf, entry.mother());
                OPTIONAL_UUID.encode(buf, entry.father());
                LINES.encode(buf, entry.lines());
            },
            // Arguments are evaluated left to right, so this reads the fields in the order they were written.
            buf -> new HerdEntry(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    Familiar.values()[Math.clamp(ByteBufCodecs.VAR_INT.decode(buf), 0, Familiar.values().length - 1)],
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ShearState.values()[Math.clamp(ByteBufCodecs.VAR_INT.decode(buf), 0, ShearState.values().length - 1)],
                    eggs(ByteBufCodecs.VAR_INT.decode(buf)),
                    ComponentSerialization.STREAM_CODEC.decode(buf),
                    OPTIONAL_UUID.decode(buf),
                    OPTIONAL_UUID.decode(buf),
                    LINES.decode(buf)
            )
    );
}
