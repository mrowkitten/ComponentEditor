package net.dasdarklord.componenteditor.screen.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.*;

public class HoverableTextWidget extends TextWidget {

    private MinecraftClient client;

    public HoverableTextWidget(MinecraftClient client, Text message, TextRenderer textRenderer) {
        super(message, textRenderer);
        this.client = client;
    }

    public HoverableTextWidget(int width, int height, MinecraftClient client, Text message, TextRenderer textRenderer) {
        super(width, height, message, textRenderer);
        this.client = client;
    }

    public HoverableTextWidget(int x, int y, int width, int height, MinecraftClient client, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message, textRenderer);
        this.client = client;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        Style style = getStyleAt(mouseX, mouseY);
        if (style != null) {
            HoverEvent hover = style.getHoverEvent();
            if (hover != null) context.drawHoverEvent(client.textRenderer, style, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        Style style = getStyleAt(mouseX, mouseY);
        if (style != null) {
            ClickEvent click = style.getClickEvent();
            if (click != null) {
                handleClick(click);

                return true;
            }
        }

        return false;
    }

    private void handleClick(ClickEvent event) {
        client.keyboard.setClipboard(event.getValue()); // Just always save it, I'm too lazy to make it actually do something
    }

    private Style getStyleAt(double mouseX, double mouseY) {
        if (mouseY < getY()) return null;
        if (mouseY > getY() + client.textRenderer.fontHeight) return null;
        if (mouseX < getX()) return null;
        if (mouseX > getX() + getWidth()) return null;

        OrderedText ordered = getMessage().asOrderedText();
        return client.textRenderer.getTextHandler().getStyleAt(ordered, (int) mouseX - getX());
    }

}
