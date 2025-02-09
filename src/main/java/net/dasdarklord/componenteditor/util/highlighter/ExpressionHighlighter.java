package net.dasdarklord.componenteditor.util.highlighter;

import net.dasdarklord.componenteditor.util.ColorUtil;
import net.dasdarklord.componenteditor.util.adventure.ComponentConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.IntegerRange;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Edited version of <a href=https://github.com/DFOnline/CodeClient/blob/main/src/main/java/dev/dfonline/codeclient/dev/highlighter/ExpressionHighlighter.java#L75>CodeClient's ExpressionHighlighter</a>
 */
public class ExpressionHighlighter {
    private int cachedInput;
    private String cachedPartial;
    private OrderedText cachedOutput;

    private final MiniMessage formatter = ColorUtil.MINIMESSAGE;
    private final Highlighter highlighter;

    public ExpressionHighlighter(Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    public OrderedText format(String input, String partial, IntegerRange range) {
        if (range.getMinimum() == cachedInput && partial.equals(cachedPartial)) {
            return cachedOutput;
        }

        Component text = highlighter.highlight(input);
        OrderedText ordered = ComponentConverter.componentToText(text).asOrderedText();
        OrderedText sub = subSequence(ordered, range);

        cachedInput = range.getMinimum();
        cachedPartial = partial;
        cachedOutput = sub;

        return sub;
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