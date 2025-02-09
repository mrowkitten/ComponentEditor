package net.dasdarklord.componenteditor.screen.widgets.editor;

import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.lang3.function.TriFunction;

/**
 * Adds a RenderTextProvider, change color of selected text and move {@link DrawContext#enableScissor} into the MatrixStack translation of a {@link net.dasdarklord.componenteditor.screen.editor.EditItemChild}
 */
public class EditItemEditBoxWidget extends EditBoxWidget {

    private boolean moveScissors = false;
    private boolean drawsBackground = true;

    private final TextRenderer textRenderer;
    private final Text placeholder;
    private TriFunction<String, Integer, Integer, OrderedText> renderTextProvider;

    public EditItemEditBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
        super(textRenderer, x, y, width, height, placeholder, message);

        this.textRenderer = textRenderer;
        this.placeholder = placeholder;

        renderTextProvider = (original, firstCharacterIndex, lineIndex) -> OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!moveScissors) {
            super.renderWidget(context, mouseX, mouseY, delta);
            return;
        }

        if (this.visible) {
            if (drawsBackground) this.drawBox(context);

            // Original Code: context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            context.enableScissor(
                    this.getX() + 1 + EditItemScreen.CHILD_OFFSET_X,
                    this.getY() + 1 + EditItemScreen.CHILD_OFFSET_Y
                    , this.getX() + this.width - 1 + EditItemScreen.CHILD_OFFSET_X,
                    this.getY() + this.height - 1 + EditItemScreen.CHILD_OFFSET_Y);

            context.getMatrices().push();
            context.getMatrices().translate((double)0.0F, -getScrollY(), (double)0.0F);

            this.renderContents(context, mouseX, mouseY, delta);

            context.getMatrices().pop();

            context.disableScissor();

            this.renderOverlay(context);
        }
    }

    public void setScrollY(double y) {
        super.setScrollY(y);
    }

    public void setRenderTextProvider(TriFunction<String, Integer, Integer, OrderedText> renderTextProvider) {
        this.renderTextProvider = renderTextProvider;
    }

    @Override
    protected void renderOverlay(DrawContext context) {
        super.renderOverlay(context);
    }

    protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
        String text = this.editBox.getText();

        if (text.isEmpty() && !this.isFocused()) {
            context.drawTextWrapped(
                    this.textRenderer,
                    placeholder,
                    this.getX() + this.getPadding(),
                    this.getY() + this.getPadding(),
                    this.width - this.getPaddingDoubled(),
                    -857677600
            );
            return;
        }

        int cursorPos = this.editBox.getCursor();
        boolean cursorVisible = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L;
        boolean cursorWithinText = cursorPos < text.length();

        int textStartX = this.getX() + this.getPadding();
        int currentY = this.getY() + this.getPadding();
        int cursorX = textStartX;
        int lastLineY = currentY;

        int lineIndex = -1;
        for (EditBox.Substring line : this.editBox.getLines()) {
            lineIndex++;
            boolean isLineVisible = this.isVisible(currentY, currentY + 9);

            if (cursorVisible && cursorWithinText && cursorPos >= line.beginIndex() && cursorPos <= line.endIndex()) {
                if (isLineVisible) {
                    // Draw text up to cursor pos
                    cursorX = context.drawTextWithShadow(
                            this.textRenderer,
                            renderTextProvider.apply(text.substring(line.beginIndex(), cursorPos), line.beginIndex(), lineIndex),
                            textStartX,
                            currentY,
                            -2039584
                    ) - 1;

                    // Draw text after cursor pos
                    context.drawTextWithShadow(
                            this.textRenderer,
                            renderTextProvider.apply(text.substring(cursorPos, line.endIndex()), cursorPos, lineIndex),
                            cursorX,
                            currentY,
                            -2039584
                    );

                    // Draw cursor
                    context.fill(cursorX, currentY - 1, cursorX + 1, currentY + 10, -3092272);
                }
            } else {
                if (isLineVisible) {
                    cursorX = context.drawTextWithShadow(
                            this.textRenderer,
                            renderTextProvider.apply(text.substring(line.beginIndex(), line.endIndex()), line.beginIndex(), lineIndex),
                            textStartX,
                            currentY,
                            -2039584
                    ) - 1;
                }
                lastLineY = currentY;
            }

            currentY += 9;
        }

        // Draw cursor at end
        if (cursorVisible && !cursorWithinText && this.isVisible(lastLineY, lastLineY + 9)) {
            context.drawTextWithShadow(this.textRenderer, "_", cursorX, lastLineY, -3092272);
        }

        if (this.editBox.hasSelection()) {
            drawSelectionHighlight(context, text);
        }
    }

    private void drawSelectionHighlight(DrawContext context, String text) {
        EditBox.Substring selection = this.editBox.getSelection();
        int startX = this.getX() + this.getPadding();
        int currentY = this.getY() + this.getPadding();

        for (EditBox.Substring line : this.editBox.getLines()) {
            if (selection.beginIndex() > line.endIndex()) {
                currentY += 9;
                continue;
            }

            if (line.beginIndex() > selection.endIndex()) {
                break;
            }

            if (this.isVisible(currentY, currentY + 9)) {
                int selectionStartOffset = this.textRenderer.getWidth(text.substring(line.beginIndex(), Math.max(selection.beginIndex(), line.beginIndex())));
                int selectionEndOffset = selection.endIndex() > line.endIndex()
                        ? this.width - this.getPadding()
                        : this.textRenderer.getWidth(text.substring(line.beginIndex(), selection.endIndex()));

                int highlightColor = 0x88CACACA;
                context.fill(startX + selectionStartOffset, currentY, startX + selectionEndOffset, currentY + 9, highlightColor);
            }

            currentY += 9;
        }
    }

    public void setMoveScissors(boolean moveScissors) {
        this.moveScissors = moveScissors;
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
    }
}
