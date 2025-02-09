package net.dasdarklord.componenteditor.screen.editor.children;

import com.mojang.brigadier.StringReader;
import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemChild;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.CEWidgetUtil;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemEditBoxWidget;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Function;

public class RawNBTScreen extends EditItemChild {

    private Consumer<NbtElement> callback;
    private Function<NbtElement, String> validationCallback;
    private final NbtElement tag;

    private Text errorText;
    private Text title;
    private final TextRenderer font;

    public EditItemEditBoxWidget nbtTextField;

    public RawNBTScreen(EditItemScreen parent, NbtElement tag) {
        super(parent);
        this.font = parent.font;
        this.tag = tag;
    }

    @Override
    protected void init() {
        super.init();

        nbtTextField = new EditItemEditBoxWidget(font, 0, 0, width , height, Text.empty(), Text.empty());
        nbtTextField.setText(tag.toString());
        CEWidgetUtil.setNBTElementHighlighterProvider(nbtTextField);
        nbtTextField.setScrollY(0);
        nbtTextField.setDrawsBackground(false);
        nbtTextField.setMoveScissors(true);

        addDrawableChild(nbtTextField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (title != null) {
            int width = font.getWidth(title);
            Vec2f vec = getPosition(width, -9);

            context.drawText(font, title, (int) vec.x, (int) vec.y, 0xffffff, false);
        }

        if (errorText != null) {
            int width = font.getWidth(errorText);
            Vec2f vec = getPosition(width, title == null ? -9 : -19);

            context.drawText(font, errorText, (int) vec.x, (int) vec.y, 0xffffff, false);
        }
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xDA000000);
    }

    private Vec2f getPosition(int width, int y) {
        int x = this.width / 2 - width / 2;

        Rect2i buttonsRect = new Rect2i(parent.saveButton.getX(), parent.saveButton.getY(), parent.cancelButton.getX() + parent.cancelButton.getWidth() - parent.saveButton.getX() + 3, parent.saveButton.getY() + parent.saveButton.getHeight());
        if (buttonsRect.contains(x, parent.saveButton.getY())) {
            x = buttonsRect.getX() + buttonsRect.getWidth() + 4;
        }

        Rect2i otherRect = new Rect2i(parent.factoryButton.getX(), parent.factoryButton.getY(), parent.factoryButton.getWidth(), parent.factoryButton.getHeight());
        Rect2i textRect = new Rect2i(x, otherRect.getY(), width, font.fontHeight);
        otherRect = otherRect.intersection(textRect);
        if (otherRect.getWidth() > 0 && otherRect.getHeight() > 0) {
            x = this.width / 2 - width / 2;
            y = y - parent.saveButton.getHeight() - 4;
        }

        return new Vec2f(x, y);
    }

    @Override
    public void tick() {
        super.tick();

        StringNbtReader reader = new StringNbtReader(new StringReader(nbtTextField.getText()));
        try {
            NbtElement element = reader.parseElement();
            if (validationCallback != null) {
                String error = validationCallback.apply(element);
                if (error != null) {
                    errorText = Text.literal(error).formatted(Formatting.RED);
                } else errorText = null;
            } else errorText = null;
        } catch (Exception ignored) {
            errorText = Text.literal("Invalid NBT").formatted(Formatting.RED);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = nbtTextField.getText();
        super.resize(client, width, height);
        nbtTextField.setText(text);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            parent.setChildScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        if (callback == null) return;
        StringNbtReader reader = new StringNbtReader(new StringReader(nbtTextField.getText()));
        try {
            NbtElement element = reader.parseElement();
            callback.accept(element);
        } catch (Exception e) {
            ComponentEditor.LOGGER.error("Couldn't parse NBT tag", e);
        }
    }

    public void setCallback(Consumer<NbtElement> callback) {
        this.callback = callback;
    }

    public void setValidationCallback(Function<NbtElement, String> validationCallback) {
        this.validationCallback = validationCallback;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

    @Override
    public void onEscClose() {
        callback = null;
    }

    public boolean hasError() {
        return errorText != null;
    }

    public static RawNBTScreen componentEditor(EditItemScreen parent, Component<?> component) {
        NbtElement tag = component.encode(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE)).getOrThrow();
        RawNBTScreen screen = new RawNBTScreen(parent, tag);
        screen.setTitle(ColorUtil.translate("<gradient:#fc9af3:#9aaafc>Editing " + component.type().toString()));
        screen.setCallback(nbt -> {
            Component<?> newComponent = Component.of(component.type(), component.type().getCodec().parse(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE), nbt).getOrThrow());
            parent.editing.set((ComponentType<Object>) newComponent.type(), newComponent.value());
            parent.reloadEditing();

            if (!parent.alwaysViewComponents.contains(component.type().toString())) {
                parent.alwaysViewComponents.add(component.type().toString());
            }
        });
        screen.setValidationCallback(element -> {
            try {
                component.type().getCodec().parse(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE), element).getOrThrow();
            } catch (IllegalStateException e) {
                return e.getMessage();
            }
            return null;
        });
        return screen;
    }

}
