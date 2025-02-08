package net.dasdarklord.componenteditor.screen.widgets;

import net.dasdarklord.componenteditor.mixin.ButtonWidgetAccessor;
import net.dasdarklord.componenteditor.mixin.ClickableWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class SimpleButtonWidget extends ButtonWidget {

    private TooltipState tooltip;
    private boolean brightens;
    private float textSize;

    public SimpleButtonWidget(ButtonWidget widget) {
        this(widget, false, 1f);
    }

    public SimpleButtonWidget(ButtonWidget widget, boolean brightens, float textSize) {
        super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage(), ((ButtonWidgetAccessor)widget).getOnPress(), ((ButtonWidgetAccessor)widget).getNarrationSupplier());
        tooltip = ((ClickableWidgetAccessor)widget).getTooltipState();
        this.brightens = brightens;
        this.textSize = textSize;
    }

    protected SimpleButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button) && clicked(mouseX, mouseY)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            if (onPress != null) onPress.onPress(this);
            return true;
        }

        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();

        boolean clicked = clicked(mouseX, mouseY);

        int bgColor = this.active ? 0x65000000 : 0x50000000;
        if (clicked) {
            bgColor = brightens ? 0x354f4f4f : 0xaa000000;
        }

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);

        int textColor = this.active ? 16777215 : 10526880;

        int startX = getX() + 2;
        int endX = getX() + width - 2;

        int startY = getY();
        int endY = getY() + height;

        int textWidth = mc.textRenderer.getWidth(getMessage());
        boolean doScissor = textWidth > endX - startX;
        if (doScissor) context.enableScissor(startX, startY, endX, endY);

        int px = getX();
        int py = getY();

        setX(0);
        setY(0);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(px + ((width / 2f) * (1 - textSize)) + (textSize / 2), py + (((height + (textSize > 1 ? mc.textRenderer.fontHeight - 1 : 0)) / 2f) * (1 - textSize)), 0);
        matrices.scale(textSize, textSize, 1);

        drawScrollableText(context, mc.textRenderer, getMessage(), (2 + (width - 2)) / 2, 2, 0, width - 2, height, textColor);

        matrices.pop();

        setX(px);
        setY(py);

        if (doScissor) context.disableScissor();

        if (clicked && tooltip != null) {
            tooltip.render(true, isFocused(), getNavigationFocus());
        }
    }

    protected static void drawScrollableText(DrawContext context, TextRenderer textRenderer, Text text, int centerX, int startX, int startY, int endX, int endY, int color) {
        int i = textRenderer.getWidth(text);
        int var10000 = startY + endY;
        Objects.requireNonNull(textRenderer);
        int j = (var10000 - 9) / 2 + 1;
        int k = endX - startX;
        if (i > k) {
            int l = i - k;
            double d = (double) Util.getMeasuringTimeMs() / (double)1000.0F;
            double e = Math.max((double)l * (double)0.5F, 3.0F);
            double f = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d / e)) / (double)2.0F + (double)0.5F;
            double g = MathHelper.lerp(f, 0.0F, l);
//            context.enableScissor(startX, startY, endX, endY);
            context.drawTextWithShadow(textRenderer, text, startX - (int)g, j, color);
//            context.disableScissor();
        } else {
            int l = MathHelper.clamp(centerX, startX + i / 2, endX - i / 2);
            context.drawCenteredTextWithShadow(textRenderer, text, l, j, color);
        }

    }
}
