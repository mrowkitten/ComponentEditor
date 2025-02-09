package net.dasdarklord.componenteditor.screen.widgets;

import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemEditBoxWidget;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.dasdarklord.componenteditor.util.highlighter.ExpressionHighlighter;
import net.dasdarklord.componenteditor.util.highlighter.MiniMessageHighlighter;
import net.dasdarklord.componenteditor.util.highlighter.NBTElementHighlighter;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.apache.commons.lang3.IntegerRange;

public class CEWidgetUtil {

    private static void setEditBoxHighlighter(EditItemEditBoxWidget editBox, ExpressionHighlighter highlighter, boolean resetNewLines) {
        editBox.setRenderTextProvider((original, firstCharacterIndex, lineIndex) -> {
            String text = editBox.getText();
            if (resetNewLines) text = text.replace("\n", "\n<ce_invis_reset>"); // Add a invisible <reset> tag (custom mm tag)

            var range = IntegerRange.of(firstCharacterIndex, (Integer)(firstCharacterIndex + original.length()));
            OrderedText ordered = highlighter.format(text, original, range);
            if (ordered == null) return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
            return ordered;
        });
    }

    public static void setNBTElementHighlighterProvider(EditItemEditBoxWidget editBox) {
        ExpressionHighlighter highlighter = new ExpressionHighlighter(new NBTElementHighlighter());
        setEditBoxHighlighter(editBox, highlighter, false);
    }

    public static void setMiniMessageProvider(EditItemEditBoxWidget editBox) {
        ExpressionHighlighter highlighter = new ExpressionHighlighter(new MiniMessageHighlighter());
        setEditBoxHighlighter(editBox, highlighter, true);
    }

    public static void setMiniMessageProvider(TextFieldWidget textField) {
        ExpressionHighlighter highlighter = new ExpressionHighlighter(new MiniMessageHighlighter());
        textField.setRenderTextProvider((original, firstCharacterIndex) -> {
            var range = IntegerRange.of(firstCharacterIndex, (Integer)(firstCharacterIndex + original.length()));
            OrderedText ordered = highlighter.format(textField.getText(), original, range);
            if (ordered == null) return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
            return ordered;
        });
    }

}
