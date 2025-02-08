package net.dasdarklord.componenteditor.screen.widgets.editor;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.editor.children.RawNBTScreen;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.util.ChatUtil;
import net.dasdarklord.componenteditor.util.adventure.ComponentConverter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ItemComponentWidget extends ContainerWidget {
    private int x;
    private int y;

    private static final Map<Integer, String> nbtTypeColors;

    private static String intToHex(int rgb) {
        return String.format("#%06X", (0xFFFFFF & rgb));
    }

    static {
        nbtTypeColors = new HashMap<>();
        nbtTypeColors.put(1 , intToHex(Formatting.RED.getColorValue()));  // Byte
        nbtTypeColors.put(2 , intToHex(Formatting.RED.getColorValue()));  // Short
        nbtTypeColors.put(3 , intToHex(Formatting.RED.getColorValue()));  // Int
        nbtTypeColors.put(4 , intToHex(Formatting.RED.getColorValue()));  // Long
        nbtTypeColors.put(5 , intToHex(Formatting.RED.getColorValue()));  // Float
        nbtTypeColors.put(6 , intToHex(Formatting.RED.getColorValue()));  // Double
        nbtTypeColors.put(7 , intToHex(Formatting.DARK_GREEN.getColorValue()));  // Byte Array (List)
        nbtTypeColors.put(8 , intToHex(Formatting.AQUA.getColorValue())); // String
        nbtTypeColors.put(9 , intToHex(Formatting.DARK_GREEN.getColorValue()));  // List
        nbtTypeColors.put(10, "#55AAFF"); // Compound
        nbtTypeColors.put(11, intToHex(Formatting.DARK_GREEN.getColorValue())); // Int Array (List)
        nbtTypeColors.put(12, intToHex(Formatting.DARK_GREEN.getColorValue())); // Long Array (List)
    }

    private final List<Drawable> drawables;
    private final List<Element> children;

    private NbtElement componentNbt;

    private final boolean isDefaultComponent;
    private final Component<?> component;
    private final TextRenderer font;
    private final EditItemScreen parent;

    public ItemComponentWidget(EditItemScreen parent, TextRenderer renderer, int x, int y, Component<?> component) {
        super(x, y, 65, 65, Text.empty());
        drawables = new ArrayList<>();
        children = new ArrayList<>();
        this.parent = parent;
        this.font = renderer;
        this.component = component;
        this.x = x;
        this.y = y;

        ButtonWidget btnRemoveButton = ButtonWidget.builder(Text.literal("-").formatted(Formatting.RED), btn -> {
            parent.alwaysViewComponents.remove(component.type().toString());
            parent.editing.remove(component.type());
            parent.reloadEditing();
            parent.setChildScreen(null);
        }).dimensions(x + width - 11 - 2, y + width - 11 - 2, 11, 11).build();
        SimpleButtonWidget removeButton = new SimpleButtonWidget(btnRemoveButton, true, 1f);
        addDrawableChild(removeButton);

        ButtonWidget btnEditButton = ButtonWidget.builder(Text.literal("Edit").formatted(Formatting.YELLOW), btn -> {
            RawNBTScreen screen = RawNBTScreen.componentEditor(parent, component);
            parent.setChildScreen(screen);
        }).dimensions(x + width - 11 - 2 - 30 - 2, y + width - 11 - 2, 30, 11).build();
        SimpleButtonWidget editButton = new SimpleButtonWidget(btnEditButton, true, 0.8f);
        addDrawableChild(editButton);

        Component<?> defaultComponent = parent.editing.getDefaultComponents().copy(component.type());
        if (defaultComponent != null && !Objects.equals(defaultComponent.value(), component.value())) {
            isDefaultComponent = false;
            ButtonWidget btnResetButton = ButtonWidget.builder(Text.literal("↻").formatted(Formatting.BLUE), btn -> {
                parent.alwaysViewComponents.remove(component.type().toString());
                parent.editing.set((ComponentType<Object>) component.type(), defaultComponent.value());
                parent.reloadEditing();
                parent.setChildScreen(null);
            }).dimensions(x + 2, y + width - 11 - 2, 11, 11)
              .tooltip(Tooltip.of(Text.literal("Reset to " + Registries.ITEM.getId(parent.editing.getItem()) + "'s default component"))).build();
            SimpleButtonWidget resetButton = new SimpleButtonWidget(btnResetButton, true, 1.75f);
            addDrawableChild(resetButton);
        } else isDefaultComponent = defaultComponent != null;

        try {
            componentNbt = component.encode(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE)).getOrThrow();
        } catch (Exception e) {
            ComponentEditor.LOGGER.error("FAILED TO SAVE COMPONENT", e);
        }
    }

    private <T extends Drawable> void addDrawable(T child) {
        drawables.add(child);
    }

    private <T extends Drawable & Element> void addDrawableChild(T child) {
        drawables.add(child);
        children.add(child);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = 0x35000000;
        if (parent.showDefaults && !parent.alwaysViewComponents.contains(component.type().toString()) && isDefaultComponent) {
            context.fill(x, y, x + getWidth(), y + getHeight(), color);
            color = 0x15ffaa00;
        }

        if (componentNbt == null) {
            color = 0x15ff5555;
        }

        context.fill(x, y, x + getWidth(), y + getHeight(), color);

        Identifier id = Registries.DATA_COMPONENT_TYPE.getId(component.type());
        String text = (id.getNamespace().equals("minecraft") ? "" : id.getNamespace()) + id.getPath();

        float scale = 0.85f;
        int maxWidth = (int) Math.floor(scale * getWidth()) - 5;
        int width = (int) Math.floor(font.getWidth(text) * scale) + 5;
        if (width > maxWidth) {
            scale = ((float) maxWidth / width) + 0.011f; // I don't know why but drawText hates 0.5, so we use 0.5011 (0.5001 also doesn't work 🛜)
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + 3, y + 3, 0);
        matrices.scale(scale, scale, 1);

        context.drawText(font, text, 0, 0, 0xFAFFFAFA, false);

        matrices.pop();


        float previewScale = 0.5f;

        matrices.push();
        matrices.translate(x + 2, y + font.fontHeight + 2, 0);
        matrices.scale(previewScale, previewScale, 1);

        if (componentNbt == null) {
            context.drawText(font, Text.literal("Error reading component"), 0, 0, Formatting.RED.getColorValue(), false);

            Text fullText = Text.literal(component.type().getClass().getSimpleName() + "; " + component.type() + "; " + component);
            List<OrderedText> wrapped = ChatUtil.breakRenderedChatMessageLines(fullText, getWidth() * 2 - 5, font);

            int textY = 15;
            for (OrderedText line : wrapped) {
                context.drawText(font, line, 0, textY, Formatting.DARK_GRAY.getColorValue(), false);
                textY += 10;
            }

            matrices.pop();
            return;
        }

        int maxTextWidth = (int) (getWidth() / previewScale - 2);
        List<OrderedText> lines = font.wrapLines(Text.literal(componentNbt.toString()), maxTextWidth);
        if (lines.size() > 2) {
            context.drawText(font, getCutoff(false, previewScale), 0, 0, Formatting.DARK_GRAY.getColorValue(), false);
            context.drawText(font, getCutoff(true, previewScale), 0, 10, Formatting.DARK_GRAY.getColorValue(), false);
        } else {
            int textY = 0;
            for (OrderedText line : lines) {
                context.drawText(font, line, 0, textY, Formatting.DARK_GRAY.getColorValue(), false);
                textY += 10;
            }
        }

        matrices.pop();

        int typeColor = TextColor.parse(nbtTypeColors.get((int) componentNbt.getType())).getOrThrow().getRgb();
        context.drawText(font, componentNbt.getNbtType().getCrashReportName(), x + 3, y + 12 + 12, typeColor, false);

        for (Drawable drawable : drawables) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    private OrderedText getCutoff(boolean end, float scale) {
        String txt = componentNbt.toString();
        if (end) {
            txt = new StringBuilder(txt).reverse().toString();
        }

        Text previewText = Text.literal(txt);
        float textWidth = font.getWidth(previewText) / scale + 2;
        float maxTextWidth = getWidth() / scale - 2;

        OrderedText ordered = previewText.asOrderedText();
        if (textWidth > maxTextWidth) {
            float ellipsisWidth = font.getWidth("...") / scale;

            AtomicReference<OrderedText> od = new AtomicReference<>(Text.empty().asOrderedText());
            font.trimToWidth(previewText, (int) (maxTextWidth - ellipsisWidth)).visit((style, message) -> {
                od.set(OrderedText.concat(od.get(), Text.literal(message).getWithStyle(style).getFirst().asOrderedText()));
                return Optional.empty();
            }, Style.EMPTY);
            ordered = OrderedText.concat(od.get(), Text.literal("...").asOrderedText());
        }

        if (end) {
            ordered = ComponentConverter.reverseOrderedText(ordered);
        }

        return ordered;
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
        for (Element child : children) {
            if (!(child instanceof ClickableWidget c)) continue;
            consumer.accept(c);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public List<? extends Element> children() {
        return children;
    }
}
