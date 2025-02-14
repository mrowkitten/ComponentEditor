package net.dasdarklord.componenteditor.screen.widgets.apps;

import net.dasdarklord.componenteditor.util.HSBColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

public class ColorPickerWidget extends ClickableWidget {

    private boolean leftDown;
    public HSBColor pickedColor;

    private float wheelWidth = 100f;
    private float wheelHeight = 100f;
    private float hueSliderOffset = 110;
    private float hueSliderWidth = 20;

    public ColorPickerWidget(int x, int y, HSBColor defaultColor) {
        this(x, y, defaultColor, 1f);
    }

    public ColorPickerWidget(int x, int y, HSBColor defaultColor, float scale) {
        super(x, y, 110 + 20, 100, Text.empty());
        pickedColor = defaultColor;

        wheelWidth = wheelWidth * scale;
        wheelHeight = wheelHeight * scale;
        hueSliderOffset = hueSliderOffset * scale;
        hueSliderWidth = hueSliderWidth * scale;

        width = (int) (hueSliderOffset + hueSliderWidth);
        height = (int) wheelHeight;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        width = (int) (hueSliderOffset + hueSliderWidth);
        height = (int) wheelHeight;

        int x = getX();
        int y = getY();

        float hueSliderX = x + hueSliderOffset;
        float hueSliderEndX = hueSliderX + hueSliderWidth;

        if (leftDown) {
            if (mouseX >= x && mouseY >= y && mouseX <= x + wheelWidth && mouseY <= y + wheelHeight) {
                float saturation = Math.clamp((float) (mouseX - x) / wheelWidth, 0f, 1f);
                float brightness = Math.clamp(1f - ((float) (mouseY - y) / wheelHeight), 0f, 1f);

                pickedColor.setSaturation(saturation);
                pickedColor.setBrightness(brightness);
            }

            if (mouseX >= hueSliderX && mouseY >= y && mouseX <= hueSliderEndX && mouseY <= y + wheelHeight) {
                float hue = Math.clamp((float) (mouseY - y) / wheelHeight, 0f, 1f);
                pickedColor.setHue(hue);
            }

            leftDown = leftDown && isMouseOver(mouseX, mouseY);
        }

        // Draw Wheel
        for (int i = 0; i < wheelWidth; i++) {
            float saturation = (float) i / wheelWidth;
            Color color1 = Color.getHSBColor(pickedColor.getHue(), saturation, 1f);
            Color color2 = Color.BLACK;

            MatrixStack matrices = context.getMatrices();
            context.fillGradient(x + i, y, x + i + 1, (int) (y + wheelHeight), color1.getRGB(), color2.getRGB());

            matrices.push();
            matrices.translate(0, 0, 1);

            int crossX = (int) (x + pickedColor.getSaturation() * wheelWidth);
            int crossY = (int) (y + (1 - pickedColor.getBrightness()) * wheelHeight);

            Color crossColor = Color.getHSBColor(pickedColor.getHue(), 0f, 1 - pickedColor.getBrightness());

            context.fill(crossX - 2, crossY, crossX + 3, crossY + 1, crossColor.getRGB());
            context.fill(crossX, crossY - 2, crossX + 1, crossY + 3, crossColor.getRGB());

            matrices.pop();
        }

        // Draw Hue Slider
        for (int i = 0; i < wheelHeight; i++) {
            float hue = (float) i / wheelHeight;
            Color color = Color.getHSBColor(hue, 1f, 1f);

            context.fill((int) hueSliderX, y + i, (int) hueSliderEndX, y + i + 1, color.getRGB());

            if (Math.abs(pickedColor.getHue() - hue) < 0.005f) {
                context.fill((int) hueSliderX, y + i, (int) hueSliderEndX, y + i + 1, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean colorPicker = mouseX >= getX() && mouseY >= getY() &&
                mouseX <= getX() + wheelWidth && mouseY <= getY() + wheelHeight;
        if (colorPicker) return true;

        return mouseX >= getX() + hueSliderOffset && mouseY >= getY() &&
                mouseX <= getX() + hueSliderOffset + hueSliderWidth && mouseY <= getY() + wheelHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) leftDown = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) leftDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void playDownSound(SoundManager soundManager) { }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

}
