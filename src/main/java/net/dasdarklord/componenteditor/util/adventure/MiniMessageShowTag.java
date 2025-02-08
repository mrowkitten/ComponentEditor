package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiniMessageShowTag implements TagResolver {
    public static final TagResolver INSTANCE = new MiniMessageShowTag();

    private MiniMessageShowTag() { }

    private final TagResolver standard = TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations(TextDecoration.BOLD),
            StandardTags.decorations(TextDecoration.ITALIC),
            StandardTags.decorations(TextDecoration.UNDERLINED),
            StandardTags.decorations(TextDecoration.STRIKETHROUGH),
            StandardTags.reset(),
            StandardTags.gradient(),
            StandardTags.rainbow(),
            CustomTags.invisReset()
    );

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        return standard.resolve(name, arguments, ctx);
    }

    @Override
    public boolean has(@NotNull String name) {
        return standard.has(name);
    }
}
