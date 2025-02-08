package net.dasdarklord.componenteditor.screen.widgets.editor;

import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;

/**
 * Moves the {@link DrawContext#enableScissor} to scissor inside the translation of an {@link net.dasdarklord.componenteditor.screen.editor.EditItemChild}
 */
public abstract class EditItemScrollableWidget extends ScrollableWidget {

    public EditItemScrollableWidget(int x, int y, int width, int height, Text text) {
        super(x, y, width, height, text);
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.drawBox(context);
//            context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1); Original code
            context.enableScissor(
                    this.getX() + 1 + EditItemScreen.CHILD_OFFSET_X,
                    this.getY() + 1 + EditItemScreen.CHILD_OFFSET_Y
                    , this.getX() + this.width - 1 + EditItemScreen.CHILD_OFFSET_X,
                    this.getY() + this.height - 1 + EditItemScreen.CHILD_OFFSET_Y);
            context.getMatrices().push();
            context.getMatrices().translate((double)0.0F, -this.getScrollY(), (double)0.0F);
            this.renderContents(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
            context.disableScissor();
            this.renderOverlay(context);
        }
    }

    public double getScrollY() {
        return super.getScrollY();
    }

}
