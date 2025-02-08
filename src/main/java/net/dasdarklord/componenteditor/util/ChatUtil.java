package net.dasdarklord.componenteditor.util;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.List;
import java.util.Optional;

public class ChatUtil {

    /**
     * Modified version of {@link net.minecraft.client.util.ChatMessages#breakRenderedChatMessageLines(StringVisitable, int, TextRenderer)}, but instead of
     * adding a space after wrapping, it adds nothing.
     * <p>
     * Original:<br/>
     * sigma sigma boy sigma boy<br/>
     * &nbsp;&nbsp;&nbsp;turns into<br/>
     * sigma sigma<br/>
     * &nbsp;boy sigma<br/>
     * &nbsp;boy<br/>
     * <p>
     * Modified:<br/>
     * sigma sigma boy sigma boy<br/>
     * &nbsp;&nbsp;&nbsp;turns into<br/>
     * sigma sigma<br/>
     * boy sigma<br/>
     * boy<br/>
     */
    public static List<OrderedText> breakRenderedChatMessageLines(StringVisitable message, int width, TextRenderer textRenderer) {
        return breakRenderedChatMessageLines(message, width, textRenderer, null);
    }

    /**
     * Modified version of {@link net.minecraft.client.util.ChatMessages#breakRenderedChatMessageLines(StringVisitable, int, TextRenderer)} with the only
     * difference being that you can control what it puts in front of the wrapped text with {@code wrapStart}.
     * <p>
     * Original:<br/>
     * sigma sigma boy sigma boy<br/>
     * &nbsp;&nbsp;&nbsp;turns into<br/>
     * sigma sigma<br/>
     * &nbsp;boy sigma<br/>
     * &nbsp;boy<br/>
     * <p>
     * Modified:<br/>
     * sigma sigma boy sigma boy<br/>
     * &nbsp;&nbsp;&nbsp;turns into<br/>
     * sigma sigma<br/>
     * boy sigma<br/>
     * boy<br/>
     */
    public static List<OrderedText> breakRenderedChatMessageLines(StringVisitable message, int width, TextRenderer textRenderer, OrderedText wrapStart) {
        OrderedText finalWrapStart;
        if (wrapStart == null) finalWrapStart = Text.empty().asOrderedText();
        else finalWrapStart = wrapStart;
        TextCollector textCollector = new TextCollector();
        message.visit((style, messagex) -> {
            String rendered = MinecraftClient.getInstance().options.getChatColors().getValue() ? messagex : Formatting.strip(messagex);
            textCollector.add(StringVisitable.styled(rendered, style));
            return Optional.empty();
        }, Style.EMPTY);
        List<OrderedText> list = Lists.newArrayList();
        textRenderer.getTextHandler().wrapLines(textCollector.getCombined(), width, Style.EMPTY, (text, lastLineWrapped) -> {
            OrderedText orderedText = Language.getInstance().reorder(text);
            list.add(lastLineWrapped ? OrderedText.concat(finalWrapStart, orderedText) : orderedText);
        });
        return list.isEmpty() ? Lists.newArrayList(OrderedText.EMPTY) : list;
    }

}
