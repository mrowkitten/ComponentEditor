package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;

@SuppressWarnings("UnstableApiUsage")
public final class MiniMessageNewLineTag {

    private static final String BR = "br";
    private static final String NEWLINE = "newline";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
            new HashSet<>(Arrays.asList(NEWLINE, BR)),
            MiniMessageNewLineTag::create,
            MiniMessageNewLineTag::claimComponent
    );

    private MiniMessageNewLineTag() {
    }

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        int times = 1;
        if (args.hasNext()) {
            times = args.pop().asInt().orElse(1);
        }

        Component component = Component.newline();
        for (int i = 0; i < times - 1; i++) {
            component = component.append(Component.newline());
        }

        return Tag.selfClosingInserting(component);
    }

    static @Nullable Emitable claimComponent(final Component input) {
        if (Component.newline().equals(input)) {
            return emit -> emit.selfClosingTag(BR);
        } else {
            return null;
        }
    }
}
