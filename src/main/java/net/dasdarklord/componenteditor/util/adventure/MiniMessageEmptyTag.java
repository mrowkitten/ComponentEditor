package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiniMessageEmptyTag implements TagResolver {

    public static final TagResolver INSTANCE = new MiniMessageEmptyTag();

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!name.equals("empty")) return null;
        return Tag.inserting(Component.empty());
    }

    @Override
    public boolean has(@NotNull String name) {
        return name.equals("empty");
    }

}
