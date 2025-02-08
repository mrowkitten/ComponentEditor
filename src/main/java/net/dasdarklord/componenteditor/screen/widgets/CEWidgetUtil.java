package net.dasdarklord.componenteditor.screen.widgets;

import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemEditBoxWidget;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.apache.commons.lang3.IntegerRange;

public class CEWidgetUtil {

    public static void setMiniMessageProvider(EditItemEditBoxWidget editBox) {
        editBox.setRenderTextProvider((original, firstCharacterIndex, lineIndex) -> {
            var range = IntegerRange.of(firstCharacterIndex, (Integer)(firstCharacterIndex + original.length()));
            OrderedText ordered = ColorUtil.MiniMessageExpressionHighlighter.format(editBox.getText().replace("\n", "\n<ce_invis_reset>"), original, range);
            if (ordered == null) return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
            return ordered;
        });
    }

    public static void setMiniMessageProvider(TextFieldWidget textField) {
        textField.setRenderTextProvider((original, firstCharacterIndex) -> {
            var range = IntegerRange.of(firstCharacterIndex, (Integer)(firstCharacterIndex + original.length()));
            OrderedText ordered = ColorUtil.MiniMessageExpressionHighlighter.format(textField.getText(), original, range);
            if (ordered == null) return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
            return ordered;
        });
    }

}
