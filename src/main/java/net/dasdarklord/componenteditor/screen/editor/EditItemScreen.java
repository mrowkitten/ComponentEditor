package net.dasdarklord.componenteditor.screen.editor;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.children.ComponentViewScreen;
import net.dasdarklord.componenteditor.screen.editor.children.FactoryScreen;
import net.dasdarklord.componenteditor.screen.editor.children.FactorySelectionScreen;
import net.dasdarklord.componenteditor.screen.editor.children.RawNBTScreen;
import net.dasdarklord.componenteditor.screen.widgets.CEWidgetUtil;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditorTextFieldWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.HoverableItemWidget;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.ItemIDSuggestor;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.TextWidgetSuggestor;
import net.dasdarklord.componenteditor.mixin.ScreenAccessor;
import net.dasdarklord.componenteditor.mixin.TextFieldWidgetAccessor;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.dasdarklord.componenteditor.util.ItemUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditItemScreen extends Screen {

    public List<String> alwaysViewComponents;
    public boolean showDefaults;

    public static final int CHILD_OFFSET_X = 5;
    public static final int CHILD_OFFSET_Y = 98;

    private Screen childScreen;

    private boolean loadFromEditing;

    public final TextRenderer font;

    public HoverableItemWidget itemWidget;
    public TextWidget countXText;
    public TextFieldWidget itemCountField;
    public TextFieldWidget itemNameField;
    public TextFieldWidget itemIdField;
    public TextWidgetSuggestor suggestor;

    public SimpleButtonWidget saveButton;
    public SimpleButtonWidget editNBTButton;
    public SimpleButtonWidget cancelButton;

    public SimpleButtonWidget factoryButton;

    private final Consumer<ItemStack> finishConsumer;

    private ItemStack original;
    public ItemStack editing;

    public EditItemScreen(ItemStack stack, Consumer<ItemStack> finish) {
        super(Text.literal("Edit Menu"));
        finishConsumer = finish;
        original = stack;
        editing = original.copy();

        font = ComponentEditor.MC.textRenderer;

//         This is only used so that I can check each component that needs to be added after a minecraft update

//        for (ComponentType<?> type : Registries.DATA_COMPONENT_TYPE) {
//            if (type.getCodec() == null) continue; // We don't care about non-encodable types
//            ComponentEditor.LOGGER.info("Checking {}", type);
//            try {
//                Object t = ComponentViewScreen.getDefaultComponentTypeValue(type, editing);
//                t.getClass();
//                ComponentEditor.LOGGER.info("Valid Class");
//                Component<?> component = Component.of(type, t);
//                ComponentEditor.LOGGER.info("Valid Component");
//                NbtElement encoded = component.encode(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE)).getOrThrow();
//                ComponentEditor.LOGGER.info("Valid encode");
//                component.type().getCodec().parse(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE), encoded).getOrThrow();
//                ComponentEditor.LOGGER.info("Valid parse");
//            } catch (Exception e) {
//                ComponentEditor.LOGGER.error("Invalid: {} with error message: {}", type, e.getMessage());
//            }
//        }

    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String countText = itemCountField.getText();
        String nameText = itemNameField.getText();
        String idText = itemIdField.getText();
        super.resize(client, width, height);
        itemCountField.setText(countText);
        itemNameField.setText(nameText);
        itemIdField.setText(idText);

        if (childScreen != null) {
            childScreen.resize(client, width - CHILD_OFFSET_X * 2, height - CHILD_OFFSET_Y - 2);
        }
    }

    @Override
    protected void init() {
        super.init();

        if (alwaysViewComponents == null) alwaysViewComponents = new ArrayList<>();

        itemWidget = new HoverableItemWidget(editing, 8, 8, 64, 64, 1000);
        addDrawableChild(itemWidget);

        String itemCountFieldValue = loadFromEditing ? Integer.toString(editing.getCount()) : Integer.toString(original.getCount());
        int itemCountFieldWidth = font.getWidth("99") + 2;
        itemCountField = new EditorTextFieldWidget(font, 8 + 64 - itemCountFieldWidth, 8 + 64 - 11, itemCountFieldWidth, 11, Text.empty());
        itemCountField.setText(itemCountFieldValue);
        itemCountField.setTextPredicate(s -> {
            if (s.isEmpty()) return true;
            boolean valid = false;
            try {
                Integer.parseInt(s);
                valid = true;
            } catch (Exception ignored) { }
            return valid;
        });
        itemCountField.setChangedListener(s -> {
            if (!s.isEmpty()) {
                int count = Integer.parseInt(s);
                if (count < 1) {
                    count = 1;
                    itemCountField.setText(Integer.toString(count));
                }
                if (count > 99) {
                    count = 99;
                    itemCountField.setText(Integer.toString(count));
                }
                ((TextFieldWidgetAccessor)itemCountField).invokeUpdateFirstCharacterIndex(0);
                editing.setCount(count);
            } else editing.setCount(1);
        });
        itemCountField.setDrawsBackground(false);
        ((TextFieldWidgetAccessor)itemCountField).invokeUpdateFirstCharacterIndex(0);
        addDrawableChild(itemCountField);

        countXText = new TextWidget(Text.literal("x"), font);
        countXText.setDimensions(font.getWidth("x"), 9);
        countXText.setPosition(8 + 64 - itemCountFieldWidth - countXText.getWidth(), 8 + 64 - 11);
        addDrawable(countXText);

        String itemNameFieldText = loadFromEditing ? ColorUtil.translateBack(editing.getName(), true) : ColorUtil.translateBack(original.getName(), true);
        int itemNameFieldWidth = ComponentEditor.MC.getWindow().getScaledWidth() - 48 - 8 - 4 - 8 - 64 - 4; // 8 - 64 - 4 = spacing; 48 - 8 - 4 = player entity + spacing
        itemNameField = new EditorTextFieldWidget(font, 8 + 64 + 4, 6, itemNameFieldWidth, 15, Text.empty());
        itemNameField.setMaxLength(4096);
        itemNameField.setText(itemNameFieldText);
        CEWidgetUtil.setMiniMessageProvider(itemNameField);
        itemNameField.setChangedListener(s -> {
            Text name = ColorUtil.translate(s, true);
            Style style = name.getStyle();
            Style noItalic = name.getStyle().withItalic(null);
            if (!style.isItalic() && noItalic.isEmpty() && name.getSiblings().isEmpty() && name.getContent() instanceof TranslatableTextContent translatable) {
                if (editing.getItemName().getContent().equals(name.getContent())) {
                    editing.remove(DataComponentTypes.CUSTOM_NAME);
                    return;
                }
            }
            editing.set(DataComponentTypes.CUSTOM_NAME, name);
        });
        addDrawableChild(itemNameField);

        int idFieldWidth = font.getWidth("  minecraft:waxed_weathered_cut_copper_stairs  "); // This is a great way to get the width
        itemIdField = new EditorTextFieldWidget(font, 8 + 64 + 4, 23, idFieldWidth, 15, Text.empty());
        suggestor = new ItemIDSuggestor(ComponentEditor.MC, this, itemIdField, 6, 0, false, 0x88000000);

        String id = loadFromEditing ? Registries.ITEM.getId(editing.getItem()).toString() : Registries.ITEM.getId(original.getItem()).toString();
        itemIdField.setMaxLength(128);
        itemIdField.setText(id);
        itemIdField.setChangedListener(s -> {
            suggestor.refresh();

            Identifier identifier = Identifier.tryParse(s);
            if (identifier != null && Registries.ITEM.containsId(identifier)) {
                Item item = Registries.ITEM.get(identifier);
                ItemStack n = editing.copyComponentsToNewStack(item, editing.getCount());
                if (n != null && !n.isEmpty()) {
                    editing = n;
                }
                itemWidget.setStack(editing);
            }
        });
        addDrawableChild(itemIdField);

        int factoryButtonWidth = 51;
        ButtonWidget btnFactoryButton = ButtonWidget.builder(Text.literal("Edit Cool Things"), btn -> setChildScreen(new FactorySelectionScreen(this))).dimensions(width - factoryButtonWidth - 5, 6 + 64 + 6, factoryButtonWidth, 20).build();
        factoryButton = new SimpleButtonWidget(btnFactoryButton);
        addDrawableChild(factoryButton);

        ButtonWidget btnSaveButton = ButtonWidget.builder(Text.literal("Save"), btn -> {
            ItemStack cpy = editing.copy();
            ComponentEditor.MC.player.getInventory().setStack(ComponentEditor.MC.player.getInventory().selectedSlot, cpy);
            finishConsumer.accept(cpy);
            original = editing.copy();
            saveButton.active = !ItemStack.areEqual(editing, original);
            if (childScreen instanceof ComponentViewScreen) setChildScreen(null);
        }).dimensions(6, 6 + 64 + 6, 64 + 4, 20).build();
        saveButton = new SimpleButtonWidget(btnSaveButton);
        saveButton.active = !ItemStack.areEqual(editing, original);
        addDrawableChild(saveButton);

        ButtonWidget btnEditNBT = ButtonWidget.builder(Text.literal("Edit Raw NBT"), btn -> {
            if (childScreen instanceof RawNBTScreen) { // Save Raw NBT
                setChildScreen(null);
                return;
            }

            if (childScreen instanceof FactoryScreen factory) {
                factory.save();
                setChildScreen(new FactorySelectionScreen(this));
                return;
            }

            NbtCompound tag = (NbtCompound) editing.toNbt(ComponentEditor.MC.world.getRegistryManager());
            RawNBTScreen screen = new RawNBTScreen(this, tag);
            screen.setCallback(nbt -> {
                try {
                    editing = ItemStack.fromNbt(ComponentEditor.MC.world.getRegistryManager(), nbt).orElse(editing);
                    reloadEditing();
                } catch (Exception e) {
                    ComponentEditor.LOGGER.error("Couldn't save item", e);
                }
            });
            setChildScreen(screen);
        }).dimensions(6 + 64 + 4 + 2, 6 + 64 + 6, 70 + 4, 20).build();
        editNBTButton = new SimpleButtonWidget(btnEditNBT);
        addDrawableChild(editNBTButton);

        ButtonWidget btnCancel = ButtonWidget.builder(Text.literal("Cancel"), btn -> {
            if (childScreen instanceof RawNBTScreen raw) raw.setCallback(null);
            if (childScreen instanceof FactoryScreen factory) {
                factory.cancel();
                factory.close();
                return;
            }
            setChildScreen(null);
        }).dimensions(6 + 64 + 4 + 2 + 70 + 4 + 2, 6 + 64 + 6, 64 + 4, 20).build();
        cancelButton = new SimpleButtonWidget(btnCancel);
        cancelButton.visible = childScreen instanceof RawNBTScreen;
        addDrawableChild(cancelButton);

        suggestor.clearWindow();

        loadFromEditing = false;

        if (childScreen == null) setChildScreen(null);
    }

    public void loadWidgetItem() {
        itemWidget.setStack(editing);
    }

    public void reloadEditing() {
        loadWidgetItem();
        loadFromEditing = true;
        clearAndInit();
    }

    public void setChildScreen(Screen screen) {
        ComponentEditor.LOGGER.info("Setting child screen to {}", screen == null ? "ComponentViewScreen" : screen.getClass().getSimpleName());
        if (childScreen != null) {
            childScreen.removed();
        }
        this.childScreen = screen;
        if (childScreen == null) {
            childScreen = new ComponentViewScreen(this);
            ((ComponentViewScreen)childScreen).setStack(editing.copy());
        }
        childScreen.init(ComponentEditor.MC, width - CHILD_OFFSET_X * 2, height - CHILD_OFFSET_Y - 2);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (childScreen != null && childScreen.charTyped(chr, modifiers)) return true;

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handlesEscape = (childScreen instanceof EditItemChild c && c.handlesEscape());
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !handlesEscape) {
            if (!(childScreen instanceof ComponentViewScreen)) {
                if (childScreen instanceof EditItemChild child) child.onEscClose();
                setChildScreen(null);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && handlesEscape) {
            childScreen.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (childScreen != null && childScreen.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (suggestor.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (childScreen != null && childScreen.mouseClicked(mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y, button)) {
            return true;
        }

        if (suggestor.mouseClicked(mouseX, mouseY, button)) return true;

        Window window = ComponentEditor.MC.getWindow();

        // Over player entity
        if (mouseX >= window.getScaledWidth() - 48 - 8 && mouseX <= window.getScaledWidth() - 8 &&
            mouseY >= 6 && mouseY <= 80) {
            ComponentEditor.MC.player.swingHand(Hand.MAIN_HAND);

            return true;
        }

        itemCountField.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (childScreen != null) childScreen.mouseReleased(mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (childScreen != null) childScreen.mouseDragged(mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (childScreen != null) childScreen.mouseMoved(mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (childScreen != null && childScreen.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (childScreen != null && childScreen.mouseScrolled(mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y, horizontalAmount, verticalAmount)) return true;
        if (suggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        MatrixStack matrices = context.getMatrices();
        for(Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            if (drawable == itemCountField || drawable == countXText) {
                matrices.push();
                matrices.translate(0, 0, 200); // Above everything, but below the HoverableItemWidget tooltip
            }

            drawable.render(context, mouseX, mouseY, delta);

            if (drawable == itemCountField || drawable == countXText) matrices.pop();
        }

        factoryButton.active = childScreen instanceof ComponentViewScreen;

        boolean isRawNBT = childScreen instanceof RawNBTScreen || childScreen instanceof FactoryScreen;

        if (saveButton != null) saveButton.active = !ItemStack.areEqual(editing, original) && !isRawNBT;

        if (editNBTButton != null) {
            if (isRawNBT) {
                FactoryScreen factory = childScreen instanceof FactoryScreen ? (FactoryScreen)childScreen : null;
                String saveText = factory != null ? factory.saveButtonText() : "Save Raw NBT";

                int width = Math.max(font.getWidth(" Edit Raw NBT "), font.getWidth(saveText));
                editNBTButton.setWidth(width + 8);

                editNBTButton.setMessage(Text.literal(saveText));
                editNBTButton.active = childScreen instanceof RawNBTScreen nbt ? !nbt.hasError() : !factory.hasError();
            } else {
                editNBTButton.setWidth(font.getWidth(" Edit Raw NBT ") + 8);
                editNBTButton.setMessage(Text.literal("Edit Raw NBT"));
                editNBTButton.active = true;
            }
        }
        cancelButton.setX(editNBTButton.getX() + editNBTButton.getWidth() + 2);

        if (cancelButton != null) cancelButton.visible = isRawNBT;

        if (childScreen != null) {
            matrices.push();
            matrices.translate(5, 98, 200); // The only problem with this is that scissors don't work with translation, so we'll have to redo widgets that do use scissors

            childScreen.render(context, mouseX - CHILD_OFFSET_X, mouseY - CHILD_OFFSET_Y, delta);

            matrices.pop();

            if (!(childScreen instanceof ComponentViewScreen)) super.setFocused(null);
        }

        if (suggestor != null) suggestor.tryRenderWindow(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);

        context.fillGradient(6, 6, 74, 74, 0x77000000, 0x44000000); // Background color for item

        context.fillGradient(width - 48 - 8, 6, width - 5, 6 + 64 + 4, 0x77000000, 0x44000000); // Background color for player entity

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack prev = player.getInventory().getStack(player.getInventory().selectedSlot);
        player.getInventory().setStack(player.getInventory().selectedSlot, editing);

        InventoryScreen.drawEntity(context, width - 48 - 8, 6, width - 5, 6 + 64 + 4, 32, 0.0625f, mouseX, mouseY, player);

        player.getInventory().setStack(player.getInventory().selectedSlot, prev);
    }

    @Override
    public void tick() {
        super.tick();

        if (childScreen != null) {
            childScreen.tick();
        }
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680); // stolen from minecraft 🛜
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if (childScreen != null && !(childScreen instanceof ComponentViewScreen)) return;
        super.setFocused(focused);
    }

    @Override
    public @Nullable Element getFocused() {
        if (!(childScreen instanceof ComponentViewScreen)) return null;
        return super.getFocused();
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
