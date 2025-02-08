package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiniMessageSpaceTag implements TagResolver {

    public static final TagResolver INSTANCE = new MiniMessageSpaceTag();

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!name.equals("space")) return null;
        int spaces = 1;
        if (arguments.hasNext()) {
            spaces = arguments.pop().asInt().orElse(1);
        }
        return Tag.selfClosingInserting(Component.text(" ".repeat(spaces)));
    }

    @Override
    public boolean has(@NotNull String name) {
        return name.equals("space");
    }

}
