package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditorTextFieldWidget;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.AttributeTypeSuggestor;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.EnumSuggestor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AttributeFactoryScreen extends ListFactoryScreen {

    protected AttributeFactoryScreen(EditItemScreen parent) {
        super(parent, false);

        AttributeModifiersComponent component = parent.editing.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        List<AttributeModifiersComponent.Entry> attributes = component.modifiers();

        for (AttributeModifiersComponent.Entry attr : attributes) {
            RegistryEntry<EntityAttribute> entry = attr.attribute();
            if (entry.getKey().isEmpty()) continue;
            RegistryKey<EntityAttribute> key = entry.getKey().get();
            EntityAttributeModifier modifier = attr.modifier();
            AttributeModifierSlot slot = attr.slot();

            addElement(new AttributeElement(this, key.getValue().toString(), modifier, slot));
        }
    }

    @Override
    public String getFactoryTitle() {
        return "Editing minecraft:attributes";
    }

    @Override
    public String saveButtonText() {
        return "Save Attributes";
    }

    @Override
    public void save() {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeElement element : getListElements().stream().map(x -> (AttributeElement) x).toList()) {
            try {
                modifier(builder, element);
            } catch (Exception ignored) {
                ComponentEditor.LOGGER.warn("Failed to add attribute modifier: {}", element.typeField.getText());
            }
        }
        parent.editing.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private void modifier(AttributeModifiersComponent.Builder builder, AttributeElement element) {
        String typeText = element.typeField.getText();
        String idText = element.idField.getText();
        String valueText = element.valueField.getText();
        String operationText = element.operationField.getText();
        String slotText = element.slotField.getText();

        if (idText.isEmpty()) idText = UUID.randomUUID().toString();

        RegistryWrapper<EntityAttribute> attributeWrapper = ComponentEditor.MC.world.getRegistryManager().getOrThrow(RegistryKeys.ATTRIBUTE);
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, Identifier.of(typeText));
        RegistryEntry<EntityAttribute> entry = attributeWrapper.getOrThrow(key);

        double value = (valueText.equals("+") || valueText.equals("-") || valueText.isEmpty()) ? 0 : Double.parseDouble(valueText);
        EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(operationText.toUpperCase());
        AttributeModifierSlot slot = AttributeModifierSlot.valueOf(slotText.toUpperCase());

        builder.add(entry, new EntityAttributeModifier(
                Identifier.of(idText),
                value,
                operation
        ), slot);
    }

    @Override
    public void cancel() {

    }

    @Override
    protected int extraContentHeight() {
        return 42;
    }

    @Override
    public void onAddPressed() {
        Identifier id = Identifier.of("");
        AttributeElement element = new AttributeElement(this, "", new EntityAttributeModifier(id, 0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.ANY);
        addElement(element);
    }

    @Override
    public boolean hasError() {
        for (AttributeElement element : getListElements().stream().map(x -> (AttributeElement) x).toList()) {
            boolean invalid = !element.suggestor.isValid();
            if (invalid) {
                return true;
            }
        }
        return super.hasError();
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        for (AttributeElement element : getListElements().stream().map(x -> (AttributeElement) x).toList()) {
            element.suggestor.clearWindow();
        }
        super.setFocused(focused);
    }

    private static class AttributeElement extends ListElement {
        @Override
        public void setFocused(@Nullable Element focused) {
            suggestor.clearWindow();
            super.setFocused(focused);
        }

        private TextWidget typeText;
        private TextWidget idText;
        private TextWidget valueText;
        private TextWidget operationText;
        private TextWidget slotText;

        private TextFieldWidget typeField;
        private TextFieldWidget idField;
        private TextFieldWidget valueField;
        private TextFieldWidget operationField;
        private TextFieldWidget slotField;

        private AttributeTypeSuggestor suggestor;
        private EnumSuggestor<EntityAttributeModifier.Operation> operationSuggestor;
        private EnumSuggestor<AttributeModifierSlot> slotSuggestor;

        private final String key;
        private final EntityAttributeModifier modifier;
        private final AttributeModifierSlot slot;

        private final int xOff = font.getWidth(" Operation ");

        public AttributeElement(ListFactoryScreen parent, String key, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
            super(parent);
            this.key = key;
            this.modifier = modifier;
            this.slot = slot;
        }

        @Override
        protected void init() {
            typeText = new TextWidget(0, 0, xOff, 14, Text.literal("Type"), font);
            idText = new TextWidget(0, 0, xOff, 14, Text.literal("ID"), font);
            valueText = new TextWidget(0, 0, xOff, 14, Text.literal("Value"), font);
            operationText = new TextWidget(0, 0, xOff, 14, Text.literal("Operation"), font);
            slotText = new TextWidget(0, 0, xOff, 14, Text.literal("Slot"), font);

            typeText.alignLeft();
            idText.alignLeft();
            valueText.alignLeft();
            operationText.alignLeft();
            slotText.alignLeft();

            typeField = new EditorTextFieldWidget(font, 0, 0, 250, 14, Text.empty());
            suggestor = new AttributeTypeSuggestor(ComponentEditor.MC, this, typeField, 5, 0, getIndex() != 0, 0xBB000000);
            typeField.setMaxLength(64);
            if (key != null) typeField.setText(key);
            typeField.setChangedListener(s -> suggestor.refresh());

            idField = new EditorTextFieldWidget(font, 0, 0, 250, 14, Text.empty());
            idField.setMaxLength(128);
            idField.setPlaceholder(Text.literal("Leave empty to generate random UUID"));
            if (modifier != null && modifier.id() != null && !modifier.id().getPath().isEmpty()) idField.setText(modifier.id().toString());

            valueField = new EditorTextFieldWidget(font, 0, 0, 150, 14, Text.empty());
            valueField.setText(Double.toString(modifier.value()));
            valueField.setTextPredicate(text -> {
                if (text.equals("-") || text.equals("+") || text.isEmpty()) return true;
                try {
                    Double.parseDouble(text);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            });

            operationField = new EditorTextFieldWidget(font, 0, 0, 150, 14, Text.empty());
            operationField.setText(modifier.operation().toString().toLowerCase());
            operationSuggestor = new EnumSuggestor<>(ComponentEditor.MC, this, operationField, 5, 0, getIndex() != 0, 0xBB000000, EntityAttributeModifier.Operation.class);
            operationField.setChangedListener(s -> operationSuggestor.refresh());

            slotField = new EditorTextFieldWidget(font, 0, 0, 150, 14, Text.empty());
            slotField.setText(slot.asString());
            slotSuggestor = new EnumSuggestor<>(ComponentEditor.MC, this, slotField, 5, 0, getIndex() != 0, 0xBB000000, AttributeModifierSlot.class);
            slotField.setChangedListener(s -> slotSuggestor.refresh());

            repositionWidgets();

            addDrawableChild(typeText);
            addDrawableChild(idText);
            addDrawableChild(valueText);
            addDrawableChild(operationText);
            addDrawableChild(slotText);

            addDrawableChild(typeField);
            addDrawableChild(idField);
            addDrawableChild(valueField);
            addDrawableChild(operationField);
            addDrawableChild(slotField);
        }

        @Override
        protected void repositionWidgets() {
            typeField.setPosition(x + xOff, y + 2);
            typeText.setPosition(x + 2, y + 2);

            idField.setPosition(x + xOff, y + 2  + 14 + 2);
            idText.setPosition(x + 2, y + 2  + 14 + 2);

            valueField.setPosition(x + xOff, y + 2 + 14 + 2 + 14 + 2);
            valueText.setPosition(x + 2, y + 2 + 14 + 2 + 14 + 2);

            operationField.setPosition(x + xOff, y + 2 + 14 + 2 + 14 + 2 + 14 + 2);
            operationText.setPosition(x + 2, y + 2 + 14 + 2 + 14 + 2 + 14 + 2);

            slotField.setPosition(x + xOff, y + 2 + 14 + 2 + 14 + 2 + 14 + 2 + 14 + 2);
            slotText.setPosition(x + 2, y + 2 + 14 + 2 + 14 + 2 + 14 + 2 + 14 + 2);

            suggestor.clearWindow();
            operationSuggestor.clearWindow();
            slotSuggestor.clearWindow();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (suggestor.mouseClicked(mouseX, mouseY, button)) return true;
            if (operationSuggestor.mouseClicked(mouseX, mouseY, button)) return true;
            if (slotSuggestor.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (suggestor.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (operationSuggestor.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (slotSuggestor.keyPressed(keyCode, scanCode, modifiers)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (suggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
            if (operationSuggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
            if (slotSuggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, 1000); // Just push the suggestors infront of the text fields

            suggestor.tryRenderWindow(context, mouseX, mouseY);
            operationSuggestor.tryRenderWindow(context, mouseX, mouseY);
            slotSuggestor.tryRenderWindow(context, mouseX, mouseY);

            matrices.pop();
        }

        @Override
        public int getHeight() {
            return 2 + 14 + 2 + 14 + 2 + 14 + 2 + 14 + 2 + 14 + 2; // spacing field padding field padding field padding field padding field spacing
        }

        @Override
        public ListElement copy() {
            return new AttributeElement(parent, key, modifier, slot);
        }
    }

}