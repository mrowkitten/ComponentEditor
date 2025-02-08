package net.dasdarklord.componenteditor.util.adventure;

import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ComponentConverter {

    public static OrderedText reverseOrderedText(OrderedText ordered) {
        AtomicReference<Text> txt = new AtomicReference<>(Text.empty());
        ordered.accept((styleIndex, style, codePoint) -> {
            txt.set(Text.literal(Character.toString(codePoint)).fillStyle(style).append(txt.get()));
            return true;
        });
        return txt.get().asOrderedText();
    }

    public static String orderedToLegacy(OrderedText ordered) {
        StringBuilder current = new StringBuilder();

        AtomicInteger index = new AtomicInteger(0);
        ordered.accept((styleIndex, style, codePoint) -> {
            if (styleIndex == 0) {
                TextColor color = style.getColor();
                if (color == null) return false;

                Formatting formatting = Formatting.byName(color.getName());
                if (formatting != null) {
                    current.append('§').append(formatting.getCode());
                } else {
                    String hex = color.getHexCode();
                    StringBuilder legacyHex = new StringBuilder();
                    for (char code : hex.replaceFirst("#", "").toCharArray()) legacyHex.append('§').append(code);
                    current.append("§x").append(legacyHex);
                }
            }

            char character;
            try {
                character = (char) codePoint;
            } catch (Exception ignored) {
                character = '?';
            }
            current.append(character);

            return true;
        });

        return current.toString();
    }

    public static MutableText orderedToText(OrderedText ordered) {
        return orderedToText(ordered, 0, Integer.MAX_VALUE);
    }

    public static MutableText orderedToText(OrderedText ordered, int start, int stop) {
        return orderedToText(ordered, start, stop, null);
    }

    public static MutableText orderedToText(OrderedText ordered, int startIndex, int stopIndex, Style defStyle) {
        AtomicReference<MutableText> text = new AtomicReference<>(Text.empty().fillStyle(defStyle == null ? Style.EMPTY : defStyle));
        StringBuilder current = new StringBuilder();
        AtomicReference<Style> lastStyle = new AtomicReference<>(null);
        AtomicReference<Style> defaultStyle = new AtomicReference<>(defStyle);

        AtomicInteger index = new AtomicInteger(0);
        ordered.accept((styleIndex, style, codePoint) -> {
            if (index.get() == 0) {
                if (defaultStyle.get() == null) defaultStyle.set(style);
            }
            if (index.get() < startIndex) {
                index.incrementAndGet();
                return true;
            }
            if (index.get() > stopIndex) {
                index.incrementAndGet();
                return true;
            }
            if (styleIndex == 0) {
                MutableText t = Text.literal(current.toString());
                t.setStyle(lastStyle.get() == null ? style : lastStyle.get());

                if (text.get().getContent() == PlainTextContent.EMPTY) text.set(t);
                else text.get().append(Text.of(t));

                current.setLength(0);
            }

            char character;
            try {
                character = (char) codePoint;
            } catch (Exception ignored) {
                character = '?';
            }
            current.append(character);

            if (style.isEmpty()) style = defaultStyle.get();
            lastStyle.set(style);

            index.incrementAndGet();
            return true;
        });

        if (!current.toString().isEmpty()) {
            MutableText t = Text.literal(current.toString());
            t.setStyle(lastStyle.get() == null ? (defaultStyle.get() == null ? Style.EMPTY : defaultStyle.get()) : lastStyle.get());
            text.get().append(t);
        }

        return text.get();
    }

    public static Component textToComponent(Text text) {
        return NonWrappingComponentSerializer.INSTANCE.deserialize(text);
    }

    public static MutableText componentToText(Component component) {
        return NonWrappingComponentSerializer.INSTANCE.serialize(component);
    }

}
