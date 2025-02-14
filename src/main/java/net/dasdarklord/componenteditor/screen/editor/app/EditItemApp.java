package net.dasdarklord.componenteditor.screen.editor.app;

import net.dasdarklord.componenteditor.screen.editor.EditItemChild;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;

public abstract class EditItemApp extends EditItemChild {

    public int x;
    public int y;

    protected EditItemApp(EditItemScreen parent) {
        super(parent);
    }

    @Override
    public final void onEscClose() { }

    @Override
    public void close() {
        parent.removeApp(this);
        removed();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX > 0 && mouseY > 0 && mouseX < getWidth() && mouseY < getHeight();
    }

    public abstract int getWidth();
    public abstract int getHeight();
}
