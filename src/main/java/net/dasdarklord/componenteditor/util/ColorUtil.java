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

    public static Component highlightedMM(String input) {
        return new MiniMessageHighlighter().highlight(input);
    }

    /**
     * Edited version of <a href=https://github.com/DFOnline/CodeClient/blob/main/src/main/java/dev/dfonline/codeclient/dev/highlighter/ExpressionHighlighter.java#L75>CodeClient's ExpressionHighlighter</a>
     */
    public static class MiniMessageExpressionHighlighter {
        private static final MiniMessageExpressionHighlighter INSTANCE = new MiniMessageExpressionHighlighter();
        private final MiniMessage formatter = MINIMESSAGE;
        private final MiniMessageHighlighter highlighter = new MiniMessageHighlighter();

        @Nullable
        public static OrderedText format(String input, String partial, IntegerRange range) {
            return INSTANCE.formats(input, partial, range);
        }

        private OrderedText formats(String input, String partial, IntegerRange range) {
            Component text = highlighter.highlight(input);
            OrderedText ordered = ComponentConverter.componentToText(text).asOrderedText();
            return subSequence(ordered, range);
        }

        private Component highlightExpressions(String input, boolean escapeTags) {
            String raw = escapeTags ? formatter.escapeTags(input) : input;
            return formatter.deserialize(raw);
        }

        private OrderedText subSequence(OrderedText original, int start, int end) {
            return visitor -> acceptWithAbsoluteIndex(original, (index, style, codePoint) -> {
                if (index >= start && index < end) {
                    return visitor.accept(index - start, style, codePoint);
                }
                return true;
            });
        }

        private OrderedText subSequence(OrderedText original, IntegerRange range) {
            int start = range.getMinimum();
            int end = range.getMaximum();

            return subSequence(original, start, end);
        }

        public boolean acceptWithAbsoluteIndex(OrderedText original, CharacterVisitor visitor) {
            AtomicInteger index = new AtomicInteger();
            return original.accept((ignored, style, codePoint) -> {
                final boolean shouldContinue = visitor.accept(index.getAndIncrement(), style, codePoint);
                if (Character.isHighSurrogate(Character.toString(codePoint).charAt(0))) {
                    index.getAndIncrement();
                }
                return shouldContinue;
            });
        }
    }

    /**
     * Lightly edited version of <a href="https://github.com/DFOnline/CodeClient/blob/main/src/main/java/dev/dfonline/codeclient/dev/highlighter/MiniMessageHighlighter.java">CodeClient's MiniMessageHighlighter</a>
     */
    public static class MiniMessageHighlighter {

        public MiniMessage HIGHLIGHTER = MiniMessage.builder()
                .tags(TagResolver.builder()
                    .resolvers(CustomTags.showTag())
                    .build())
                .build();

        public MiniMessage PARSER = MiniMessage.builder().tags(TagResolver.resolver(
                CustomTags.showTag(),
                CustomTags.newLineTag(),
                CustomTags.spaceTag(),
                CustomTags.emptyTag(),
                CustomTags.invisReset()
        )).build();

        private final String RESET_TAG = "<reset>";
        private final String INVIS_RESET_TAG = "<ce_invis_reset>";
        private String getTagStyle() {
            return TextColor.color(0x3a3a3a).asHexString();
        }

        public Component highlight(String input) {
            // handle "resets"
            var resets = input.split("(?i)"+RESET_TAG);
            if (resets.length > 1 || input.toLowerCase().endsWith(RESET_TAG)) {
                Component value = Component.empty();

                Component reset = PARSER.deserialize(String.format("<%s>", getTagStyle()) + HIGHLIGHTER.escapeTags(RESET_TAG) + String.format("</%s>", getTagStyle()));

                for (String partial : resets) {
                    value = value.append(highlight(partial))
                            .append(reset);
                }
                return value;
            }

            // handle "resets" v2
            resets = input.split("(?i)"+INVIS_RESET_TAG);
            if (resets.length > 1 || input.toLowerCase().endsWith(INVIS_RESET_TAG)) {
                Component value = Component.empty();

                Component reset = PARSER.deserialize(String.format("<%s>", getTagStyle()) + HIGHLIGHTER.escapeTags(INVIS_RESET_TAG) + String.format("</%s>", getTagStyle()));

                for (String partial : resets) {
                    value = value.append(highlight(partial));
                }
                return value;
            }

            Node.Root root = PARSER.deserializeToTree(input);
            StringBuilder newInput = new StringBuilder(input.length());

            handle(root, root.input(), newInput, new AtomicInteger(), new ArrayList<>());
            return HIGHLIGHTER.deserialize(newInput.toString());
        }

        @SuppressWarnings("UnstableApiUsage")
        private void handle(Node node, String full, StringBuilder sb, AtomicInteger index, ArrayList<String> decorations) {
            String style = getTagStyle();

            if (node instanceof TagNode tagNode) {
                String tagString = getTokenString(tagNode.token(), full);

                index.addAndGet(tagString.length());

                appendEscapedTag(sb, tagString, style, decorations);

                String tagName = tagNode.name();
                if (StandardTags.decorations().has(tagName)) {
                    decorations.add(tagName);
                }

                // prevent "space" and "newline" tags from being added extra as they dont get parsed in the chatbox.
                if (!(tagString.contains("space") || tagString.contains("newline") || tagString.contains("decode_invis_reset"))) sb.append(tagString);
            } else if (node instanceof ValueNode valueNode) {
                String value = valueNode.value();

                index.addAndGet(value.length());

                sb.append(PARSER.escapeTags(value));
            }

            for (Node child : node.children()) {
                handle(child, full, sb, index, decorations);
            }

            if (node instanceof TagNode tagNode) {
                String tagName = tagNode.name();
                String closing = String.format("</%s>", tagName);

                if (full.startsWith(closing, index.get())) {
                    if (StandardTags.decorations().has(tagName)) {
                        decorations.remove(tagName);
                    }

                    index.addAndGet(closing.length());
                    sb.append(closing);
                    appendEscapedTag(sb, closing, style, decorations);
                }
            }
        }

        private void appendEscapedTag(StringBuilder sb, String tag, String style, ArrayList<String> decorations) {
            // idk if someone wants to figure this out, it doesn't actually seem to work with multiple decorations.
            StringBuilder opening = new StringBuilder();

            StringBuilder closing = new StringBuilder();
            decorations.forEach(decoration -> {
                interpolate(opening, "<", decoration, ">");
                interpolate(closing, "</", decoration, ">");
            });
            //

            interpolate(sb, "<", style, ">", HIGHLIGHTER.escapeTags(tag), "</", style, ">");
        }

        private void interpolate(StringBuilder sb, String... substrings) {
            for (String substring : substrings) {
                sb.append(substring);
            }
        }

        @SuppressWarnings("UnstableApiUsage")
        private String getTokenString(Token token, String root) {
            int start = token.startIndex();
            int end = token.endIndex();
            return root.substring(start, end);
        }

    }

}
