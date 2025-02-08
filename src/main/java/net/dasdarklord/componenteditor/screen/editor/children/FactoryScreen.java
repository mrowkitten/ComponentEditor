package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.screen.editor.EditItemChild;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public abstract class FactoryScreen extends EditItemChild {

    protected TextRenderer font;

    protected FactoryScreen(EditItemScreen parent) {
        super(parent);

        this.font = parent.font;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Text title = ColorUtil.translate("<gradient:#fc9af3:#9aaafc>" + getFactoryTitle());
        int width = font.getWidth(title);
        int x = this.width / 2 - width / 2;

        Rect2i buttonsRect = new Rect2i(parent.saveButton.getX(), parent.saveButton.getY(), parent.cancelButton.getX() + parent.cancelButton.getWidth() - parent.saveButton.getX() + 3, parent.saveButton.getY() + parent.saveButton.getHeight());
        if (buttonsRect.contains(x, parent.saveButton.getY())) {
            x = buttonsRect.getX() + buttonsRect.getWidth() + 4;
        }

        int y = -9;
        Rect2i otherRect = new Rect2i(parent.factoryButton.getX(), parent.factoryButton.getY(), parent.factoryButton.getWidth(), parent.factoryButton.getHeight());
        Rect2i textRect = new Rect2i(x, otherRect.getY(), width, font.fontHeight);
        otherRect = otherRect.intersection(textRect);
        if (otherRect.getWidth() > 0 && otherRect.getHeight() > 0) {
            x = this.width / 2 - width / 2;
            y = -9 - parent.saveButton.getHeight() - 4;
        }

        context.drawText(font, title, x, y, 0xffffff, false);
    }

    public abstract String getFactoryTitle();

    public abstract String saveButtonText();

    public abstract void save();

    public abstract void cancel();

    public boolean hasError() {
        return false;
    }

    @Override
    public boolean handlesEscape() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        parent.setChildScreen(new FactorySelectionScreen(parent));
        removed();
    }

}
