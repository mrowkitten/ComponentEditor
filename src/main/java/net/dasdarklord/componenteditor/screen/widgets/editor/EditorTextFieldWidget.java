package net.dasdarklord.componenteditor.screen.widgets.editor;

import net.dasdarklord.componenteditor.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EditorTextFieldWidget extends TextFieldWidget {
    private static final ButtonTextures TEXTURES = TextFieldWidgetAccessor.getButtonTextures();

    private final TextRenderer textRenderer;
    private int selectingStart;
    private boolean selecting;
    private boolean wasSelecting;

    public EditorTextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
        this.textRenderer = textRenderer;
    }

    public EditorTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.textRenderer = textRenderer;
    }

    public EditorTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!isFocused()) {
            setSelectionStart(0);
            setSelectionEnd(0);
        }

        if (this.isVisible()) {
            TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) this;

            if (this.drawsBackground()) {
                Identifier identifier = TEXTURES.get(this.isNarratable(), this.isFocused());
                context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }


            int i = accessor.getIsEditable() ? accessor.getEditableColor() : accessor.getUneditableColor();
            int j = accessor.getSelectionStart() - accessor.getFirstCharacterIndex();
            String string = this.textRenderer.trimToWidth(getText().substring(accessor.getFirstCharacterIndex()), this.getInnerWidth());
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && (Util.getMeasuringTimeMs() - accessor.getLastSwitchFocusTime()) / 300L % 2L == 0L && bl;
            int k = this.drawsBackground() ? this.getX() + 4 : this.getX();
            int l = this.drawsBackground() ? this.getY() + (this.height - 8) / 2 : this.getY();
            int m = k;
            int n = MathHelper.clamp(accessor.getSelectionEnd() - accessor.getFirstCharacterIndex(), 0, string.length());
            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                m = context.drawTextWithShadow(this.textRenderer, accessor.getRenderTextProvider().apply(string2, accessor.getFirstCharacterIndex()), k, l, i);
            }

            boolean bl3 = accessor.getSelectionStart() < getText().length() || getText().length() >= accessor.invokeGetMaxTextLength();
            int o = m;
            if (!bl) {
                o = j > 0 ? k + this.width : k;
            } else if (bl3) {
                o = m - 1;
                --m;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                context.drawTextWithShadow(this.textRenderer, accessor.getRenderTextProvider().apply(string.substring(j), accessor.getSelectionStart()), m, l, i);
            }

            if (accessor.getPlaceholder() != null && string.isEmpty() && !this.isFocused()) {
                context.drawTextWithShadow(this.textRenderer, accessor.getPlaceholder(), m, l, i);
            }

            if (!bl3 && accessor.getSuggestion() != null) {
                context.drawTextWithShadow(this.textRenderer, accessor.getSuggestion(), o - 1, l, -8355712);
            }

            if (bl2) {
                if (bl3) {
                    RenderLayer var10001 = RenderLayer.getGuiOverlay();
                    int var10003 = l - 1;
                    int var10004 = o + 1;
                    int var10005 = l + 1;
                    Objects.requireNonNull(this.textRenderer);
                    context.fill(var10001, o, var10003, var10004, var10005 + 9, -3092272);
                } else {
                    context.drawTextWithShadow(this.textRenderer, "_", o, l, i);
                }
            }

            if (n != j) {
                int p = k + this.textRenderer.getWidth(string.substring(0, n));
                int var19 = l - 1;
                int var20 = p - 1;
                int var21 = l + 1;
                Objects.requireNonNull(this.textRenderer);
                this.drawSelectionHighlight(context, o, var19, var20, var21 + 9);
            }

        }
    }

    private void drawSelectionHighlight(DrawContext context, int x1, int y1, int x2, int y2) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }

        if (x2 > this.getX() + this.width) {
            x2 = this.getX() + this.width;
        }

        if (x1 > this.getX() + this.width) {
            x1 = this.getX() + this.width;
        }

        int highlightColor = 0x88CACACA;
        context.fill(x1, y1, x2, y2, highlightColor);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isVisible() && isMouseOver(mouseX, mouseY)) {
            if (!wasSelecting) selectingStart = getCursor();
            wasSelecting = true;
            selecting = true;

            int i = MathHelper.floor(mouseX) - this.getX();
            if (drawsBackground()) {
                i -= 4;
            }

            setSelectionEnd(selectingStart);

            TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) this;
            String string = textRenderer.trimToWidth(getText().substring(accessor.getFirstCharacterIndex()), this.getInnerWidth());
            setCursor(textRenderer.trimToWidth(string, i).length() + accessor.getFirstCharacterIndex(), true);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        selecting = false;
        wasSelecting = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
