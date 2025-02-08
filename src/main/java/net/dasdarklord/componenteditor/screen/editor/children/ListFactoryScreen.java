package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemScrollableWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ListFactoryScreen extends FactoryScreen {
    private EditItemScrollableWidget scrollableWidget;
    private final List<ElementW> listElements;
    private final boolean moveableEntries;
    protected SimpleButtonWidget addButton;

    protected ListFactoryScreen(EditItemScreen parent, boolean moveableEntries) {
        super(parent);
        this.moveableEntries = moveableEntries;
        listElements = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        scrollableWidget = new EditItemScrollableWidget(0, 0, width, height, Text.empty()) {
            @Override
            protected void drawBox(DrawContext context) { }

            @Override
            protected int getContentsHeight() {
                int padding = 5;
                int spacing = 3;
                int contentsHeight = listElements.stream().mapToInt(ElementW::getHeight).sum();
                return padding + contentsHeight + spacing * listElements.size() + extraContentHeight();
            }

            @Override
            protected double getDeltaYPerScroll() {
                return 9f / 2f;
            }

            @Override
            protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
                int index = 0;
                int y = 5;
                for (ElementW element : listElements) {
                    element.index = index;

                    int prevY = element.getY();
                    if (prevY != y) {
                        element.setY(y);
                        element.repositionWidgets();
                    }
                    element.render(context, mouseX, mouseY, delta);
                    y += element.getHeight() + 3;
                    index++;
                }
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) { }
        };
        addDrawableChild(scrollableWidget);

        ButtonWidget btnAddButton = ButtonWidget.builder(Text.literal("+").formatted(Formatting.GREEN), btn -> onAddPressed()).dimensions(width - 22 - 4, 4, 22, 22).build();
        addButton = new SimpleButtonWidget(btnAddButton);
        addDrawableChild(addButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY + (int) scrollableWidget.getScrollY(), delta);
    }

    protected void addElement(ListElement element) {
        ElementW w = new ElementW();
        w.setElement(element);
        listElements.add(w);
        w.clearAndInit();
    }

    protected void removeElement(ListElement element) {
        listElements.removeIf(e -> {
            boolean b = e.element.equals(element);
            if (b) {
                e.element.w = null;
            }
            return b;
        });
    }

    protected List<ListElement> getListElements() {
        return listElements.stream().map(x -> x.element).collect(Collectors.toList());
    }

    public abstract void onAddPressed();

    private ElementW previousClick;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < 0) return false;
        if (addButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (ElementW child : listElements) {
            if (child.mouseClicked(mouseX, mouseY + scrollableWidget.getScrollY(), button)) {
                if (previousClick != null && previousClick != child) {
                    previousClick.setFocused(null);
                    previousClick.element.setFocused(null);
                }
                previousClick = child;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ElementW child : listElements) {
            child.mouseReleased(mouseX, mouseY + scrollableWidget.getScrollY(), button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (ElementW child : listElements) {
            child.mouseDragged(mouseX, mouseY + scrollableWidget.getScrollY(), button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (ElementW child : listElements) {
            child.mouseMoved(mouseX, mouseY + scrollableWidget.getScrollY());
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ElementW child : listElements) {
            if (child.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ElementW child : listElements) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ElementW child : listElements) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (ElementW child : listElements) {
            if (child.mouseScrolled(mouseX, mouseY + scrollableWidget.getScrollY(), horizontalAmount, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected int extraContentHeight() {
        return 0;
    }

    private class ElementW extends AbstractParentElement implements Drawable, Element, Widget, Supplier<ListElement> {
        private ListElement element;

        private int index;
        private final List<Drawable> drawables;
        private final List<Element> children;

        private SimpleButtonWidget moveUpButton;
        private SimpleButtonWidget moveDownButton;
        private SimpleButtonWidget deleteButton;
        private SimpleButtonWidget copyButton;

        public ElementW() {
            drawables = new ArrayList<>();
            children = new ArrayList<>();
        }

        private void setElement(ListElement element) {
            this.element = element;
            element.w = this;
        }

        private void init() {
            element.setX(16);
            element.clearAndInit();

            setFocused(element);

            if (moveableEntries) {
                ButtonWidget btnMoveUpButton = ButtonWidget.builder(Text.literal("⏶"), btn -> {
                    int index = listElements.indexOf(this);
                    int newIndex = Math.max(0, index - 1);
                    listElements.remove(index);
                    listElements.add(newIndex, this);
                }).tooltip(Tooltip.of(Text.literal("Move Entry Up"))).dimensions(getX() - 14, getY() - 2, 10, 10).build();
                moveUpButton = new SimpleButtonWidget(btnMoveUpButton, true, 1f);
                addDrawableChild(moveUpButton);

                ButtonWidget btnMoveDownButton = ButtonWidget.builder(Text.literal("⏷"), btn -> {
                    int index = listElements.indexOf(this);
                    int newIndex = Math.min(index + 1, listElements.size() - 1);
                    listElements.remove(index);
                    listElements.add(newIndex, this);
                }).tooltip(Tooltip.of(Text.literal("Move Entry Down"))).dimensions(getX() - 14, getY() - 2 + 10 + 1, 10, 10).build();
                moveDownButton = new SimpleButtonWidget(btnMoveDownButton, true, 1f);
                addDrawableChild(moveDownButton);
            }

            int deleteY = moveableEntries ? 10 + 1 + 10 + 1 : 0;
            ButtonWidget btnDeleteEntryButton = ButtonWidget.builder(Text.literal("\uD83D\uDDD1"), btn -> removeElement(this.element)).tooltip(Tooltip.of(Text.literal("Delete Entry"))).dimensions(getX() - 14, getY() - 2 + deleteY, 10, 10).build();
            deleteButton = new SimpleButtonWidget(btnDeleteEntryButton, true, 1f);
            addDrawableChild(deleteButton);

            int dupeY = moveableEntries ? 10 + 1 + 10 + 1 + 10 + 1 : 10 + 1;
            ButtonWidget btnDuplicateEntryButton = ButtonWidget.builder(Text.literal("☰"), btn -> addElement(this.element.copy())).tooltip(Tooltip.of(Text.literal("Duplicate Entry"))).dimensions(getX() - 14, getY() - 2 + dupeY, 10, 10).build();
            copyButton = new SimpleButtonWidget(btnDuplicateEntryButton, true, 1f);
            addDrawableChild(copyButton);
        }

        private void repositionWidgets() {
            element.repositionWidgets();

            if (moveableEntries) {
                moveUpButton.setDimensionsAndPosition(10, 10, getX() - 14, getY() - 2);
                moveDownButton.setDimensionsAndPosition(10, 10, getX() - 14, getY() - 2 + 10 + 1);
            }

            int deleteY = moveableEntries ? 10 + 1 + 10 + 1 : 0;
            int dupeY = moveableEntries ? 10 + 1 + 10 + 1 + 10 + 1 : 10 + 1;

            deleteButton.setDimensionsAndPosition(10, 10, getX() - 14, getY() - 2 + deleteY);
            copyButton.setDimensionsAndPosition(10, 10, getX() - 14, getY() - 2 + dupeY);
        }

        private void clearAndInit() {
            drawables.clear();
            children.clear();
            init();
        }

        protected <T extends Drawable> void addDrawable(T drawable) {
            this.drawables.add(drawable);
        }

        protected <T extends Drawable & Element> void addDrawableChild(T child) {
            this.drawables.add(child);
            this.children.add(child);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            for (Element child : children) {
                if (child.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
            return element.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            super.setFocused(element);
            setFocused(true);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) return true;
            return element.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (super.mouseReleased(mouseX, mouseY, button)) return true;
            return element.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
            return element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
            return element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            for (Element child : children) {
                element.mouseMoved(mouseX, mouseY);
            }
            element.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            return super.charTyped(chr, modifiers);
        }

        @Override
        public ListElement get() {
            return element;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            int color = index % 2 == 0 ? 0x11646464 : 0x33646464;
            context.fill(getX() - 16, getY() - 3, getX() - 16 + getWidth(), getY() + getHeight(), color);

            for (Drawable drawable : drawables) {
                drawable.render(context, mouseX, mouseY, delta);
            }
            element.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void setFocused(boolean focused) {
            element.setFocused(focused);
        }

        @Override
        public boolean isFocused() {
            return element.isFocused();
        }

        @Override
        public ScreenRect getNavigationFocus() {
            return element.getNavigationFocus();
        }

        @Override
        public void setX(int x) {
            element.setX(x);
        }

        @Override
        public void setY(int y) {
            element.setY(y);
        }

        @Override
        public int getX() {
            return element.getX();
        }

        @Override
        public int getY() {
            return element.getY();
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            int buttonsHeight = -2 + 9 * 4 + 2 * 4;
            int elementHeight = element.getHeight();
            return Math.max(elementHeight, buttonsHeight);
        }

        @Override
        public void forEachChild(Consumer<ClickableWidget> consumer) {
            element.forEachChild(consumer);
        }
    }

    public static abstract class ListElement extends AbstractParentElement implements Drawable, Element, Widget {
        private ElementW w;

        protected ListFactoryScreen parent;
        protected TextRenderer font;

        public ListElement(ListFactoryScreen parent) {
            this.parent = parent;
            this.font = parent.font;

            drawables = new ArrayList<>();
            children = new ArrayList<>();
        }

        protected int getIndex() {
            if (w == null) return -1;
            return w.index;
        }

        protected abstract void init();

        protected abstract void repositionWidgets();

        public void clearAndInit() {
            drawables.clear();
            children.clear();
            init();
        }

        protected int x;
        protected int y;

        private final List<Drawable> drawables;
        private final List<Element> children;
        private boolean focused;

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            for (Drawable drawable : drawables) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public int getWidth() {
            if (w == null) return ComponentEditor.MC.getWindow().getScaledWidth() - EditItemScreen.CHILD_OFFSET_X * 2;
            return w.getWidth();
        }

        @Override
        public abstract int getHeight();

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            for (Element child : children) {
                if (child.isMouseOver(mouseX, mouseY)) return true;
            }
            return super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            for (Element element : children) {
                element.mouseMoved(mouseX, mouseY);
            }
            super.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public abstract ListElement copy();

        protected <T extends Drawable> void addDrawable(T drawable) {
            this.drawables.add(drawable);
        }

        protected <T extends Drawable & Element> void addDrawableChild(T child) {
            this.drawables.add(child);
            this.children.add(child);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public ScreenRect getNavigationFocus() {
            return super.getNavigationFocus();
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void forEachChild(Consumer<ClickableWidget> consumer) {
            for (Element element : children) {
                if (element instanceof ClickableWidget clickable) consumer.accept(clickable);
            }
        }
    }

}
