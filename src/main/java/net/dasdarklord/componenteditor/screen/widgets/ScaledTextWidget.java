package net.dasdarklord.componenteditor.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ScaledTextWidget extends TextWidget {

    public float scale;

    public ScaledTextWidget(Text message, TextRenderer textRenderer, float scale) {
        super(message, textRenderer);
        this.scale = scale;
    }

    public ScaledTextWidget(int width, int height, Text message, TextRenderer textRenderer, float scale) {
        super(width, height, message, textRenderer);
        this.scale = scale;
    }

    public ScaledTextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer, float scale) {
        super(x, y, width, height, message, textRenderer);
        this.scale = scale;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int px = getX();
        int py = getY();

        setX(0);
        setY(0);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(px, py, 0);
        matrices.scale(scale, scale, 1);

        super.renderWidget(context, mouseX, mouseY, delta);

        matrices.pop();

        setX(px);
        setY(py);
    }
}
