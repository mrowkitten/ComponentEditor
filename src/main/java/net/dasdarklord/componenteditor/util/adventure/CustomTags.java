package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class CustomTags {

    public static TagResolver showTag() {
        return MiniMessageShowTag.INSTANCE;
    }

    public static TagResolver emptyTag() {
        return MiniMessageEmptyTag.INSTANCE;
    }

    public static TagResolver newLineTag() {
        return MiniMessageNewLineTag.RESOLVER;
    }

    public static TagResolver spaceTag() {
        return MiniMessageSpaceTag.INSTANCE;
    }

    public static TagResolver invisReset() {
        return InvisResetTag.RESOLVER;
    }

}
