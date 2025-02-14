package net.dasdarklord.componenteditor.screen.widgets.apps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.awt.*;

public class ColorPresetWidget extends ClickableWidget {

    public final Color color;
    private final PressAction onPress;

    public ColorPresetWidget(int x, int y, Color color, PressAction onPress) {
        super(x, y, 16, 16, Text.empty());
        this.color = color;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX() - 1, getY() - 1, getX() + getHeight() + 1, getY() + getWidth() + 1, 0xff000000);
        context.fill(getX(), getY(), getX() + getHeight(), getY() + getWidth(), color.getRGB());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (onPress != null) onPress.onPress(this);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(ColorPresetWidget button);
    }
}
