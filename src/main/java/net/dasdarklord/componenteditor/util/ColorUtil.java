package net.dasdarklord.componenteditor.util;

import net.dasdarklord.componenteditor.util.adventure.ComponentConverter;
import net.dasdarklord.componenteditor.util.adventure.CustomTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorUtil {

    public static final MiniMessage MINIMESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolvers(StandardTags.color(), StandardTags.gradient(), StandardTags.rainbow(), StandardTags.reset(), StandardTags.transition(), StandardTags.rainbow(), StandardTags.insertion(), StandardTags.decorations())
                    .resolvers(StandardTags.font(), StandardTags.keybind(), StandardTags.translatable())
                    .resolvers(StandardTags.clickEvent(), StandardTags.hoverEvent(), StandardTags.insertion())
                    .resolvers(CustomTags.emptyTag(), CustomTags.spaceTag(), CustomTags.newLineTag())
                    .resolvers(CustomTags.invisReset())
                    .build())
            .build();

    public static String stripLegacy(String text) {
        return text.replaceAll("§[a-f0-9lmnoi]|§x(§[a-f0-9]){6}", "");
    }

    public static Text translateLegacy(String text) {
        return translateLegacy(text, '§');
    }

    public static Text translateLegacy(String text, char c) {
        Component noItalic = Component.empty().style(Style.empty().decoration(TextDecoration.ITALIC, false));
        Component comp = noItalic.append(LegacyComponentSerializer.legacy(c).deserialize(text));
        return ComponentConverter.componentToText(comp);
    }

    public static Text translate(String text) {
        return translate(text, false);
    }

    public static Text translate(String text, boolean inferItalic) {
        if (text.contains("§")) {
            MutableText legacyText = translateLegacy(text).copy();
            if (inferItalic) {
                legacyText.setStyle(legacyText.getStyle().withItalic(false));
            }
            return legacyText;
        }
        if (inferItalic && !text.startsWith("<!italic>")) {
            text = "<!italic>" + text;
        }
        Component component = MINIMESSAGE.deserialize(text);
        return ComponentConverter.componentToText(component);
    }

    public static String translateBack(Text text) {
        return translateBack(text, false);
    }

    public static String translateBack(Text text, boolean inferItalic) {
        Component component = ComponentConverter.textToComponent(text);
        String result = MINIMESSAGE.serialize(component);
        if (inferItalic) result = result.replaceFirst("^<!italic>", "");
        return result;
    }

}
