package net.dasdarklord.componenteditor.screen.editor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public abstract class EditItemChild extends Screen {

    protected EditItemScreen parent;

    protected EditItemChild(EditItemScreen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    public void close() {
        parent.setChildScreen(null);
        removed();
    }

    @Override
    public final boolean shouldPause() {
        return false;
    }

    @Override
    public final boolean shouldCloseOnEsc() {
        return false;
    }

    public void onEscClose() { }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
    }

    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, 0x80101010, 0xA0101010);
    }

    public boolean handlesEscape() {
        return false;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (focused != null) parent.setFocused(this);
        super.setFocused(focused);
    }

    @Override
    public @Nullable GuiNavigationPath getFocusedPath() {
        return null;
    }

    @Override
    public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return null;
    }

    @Override
    protected void switchFocus(GuiNavigationPath path) {
    }

}
