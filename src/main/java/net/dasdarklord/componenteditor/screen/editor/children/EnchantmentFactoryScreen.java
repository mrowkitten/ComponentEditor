package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.mixin.TextFieldWidgetAccessor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditorTextFieldWidget;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.EnchantmentIDSuggestor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class EnchantmentFactoryScreen extends ListFactoryScreen {

    protected EnchantmentFactoryScreen(EditItemScreen parent) {
        super(parent, false);

        ItemEnchantmentsComponent component = parent.editing.get(DataComponentTypes.ENCHANTMENTS);
        Set<RegistryEntry<Enchantment>> enchantments = component.getEnchantments();

        for (RegistryEntry<Enchantment> ench : enchantments) {
            if (ench.getKey().isEmpty()) continue;
            String key = ench.getKey().get().getValue().toString();
            int level = component.getLevel(ench);

            addElement(new EnchantmentElement(this, key, level));
        }
    }

    @Override
    public String getFactoryTitle() {
        return "Editing minecraft:enchantments";
    }

    @Override
    public String saveButtonText() {
        return "Save Enchantments";
    }

    @Override
    public void save() {
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        for (ListElement element : getListElements()) {
            if (!(element instanceof EnchantmentElement enchant)) continue;
            String id = enchant.idField.getText();
            int level = Integer.parseInt(enchant.countField.getText());

            RegistryWrapper<Enchantment> enchantmentWrapper = ComponentEditor.MC.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            RegistryEntry.Reference<Enchantment> entry = enchantmentWrapper.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(id))).orElseThrow();

            builder.add(entry, level);
        }

        EnchantmentHelper.set(parent.editing, builder.build());
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onAddPressed() {
        EnchantmentElement element = new EnchantmentElement(this, "", 1);
        addElement(element);
    }

    @Override
    public boolean hasError() {
        for (EnchantmentElement element : getListElements().stream().map(x -> (EnchantmentElement) x).toList()) {
            boolean invalid = !element.suggestor.isValid();
            if (invalid) {
                return true;
            }
        }
        return super.hasError();
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        for (EnchantmentElement element : getListElements().stream().map(x -> (EnchantmentElement) x).toList()) {
            element.suggestor.clearWindow();
        }
        super.setFocused(focused);
    }

    private static class EnchantmentElement extends ListElement {
        @Override
        public void setFocused(@Nullable Element focused) {
            suggestor.clearWindow();
            super.setFocused(focused);
        }

        private TextFieldWidget idField;
        private TextFieldWidget countField;
        private EnchantmentIDSuggestor suggestor;

        private final String enchant;
        private final int count;

        public EnchantmentElement(ListFactoryScreen parent, String enchant, int count) {
            super(parent);
            this.enchant = enchant;
            this.count = count;
        }

        @Override
        protected void init() {
            idField = new EditorTextFieldWidget(font, x + 2, y + 2, 250, 14, Text.empty());
            suggestor = new EnchantmentIDSuggestor(ComponentEditor.MC, this, idField, 5, 0, getIndex() != 0, 0xBB000000);
            if (enchant != null) idField.setText(enchant);
            idField.setChangedListener(s -> suggestor.refresh());

            countField = new EditorTextFieldWidget(font, x + 2, y + 2 + 14 + 2, 45, 14, Text.empty());
            countField.setText(Integer.toString(count));
            countField.setMaxLength(3);
            countField.setTextPredicate(s -> {
                if (s.isEmpty()) return true;
                boolean valid = false;
                try {
                    Integer.parseInt(s);
                    valid = true;
                } catch (Exception ignored) { }
                return valid;
            });
            countField.setChangedListener(s -> {
                if (!s.isEmpty()) {
                    int c = Integer.parseInt(s);
                    if (c < 1) {
                        c = 1;
                        countField.setText(Integer.toString(c));
                    }
                    if (c > 255) {
                        c = 255;
                        countField.setText(Integer.toString(c));
                    }
                    ((TextFieldWidgetAccessor)countField).invokeUpdateFirstCharacterIndex(0);
                }
            });
            ((TextFieldWidgetAccessor)countField).invokeUpdateFirstCharacterIndex(0);

            addDrawableChild(idField);
            addDrawableChild(countField);
        }

        @Override
        protected void repositionWidgets() {
            idField.setPosition(x + 2, y + 2);
            countField.setPosition(x + 2, y + 2 + 14 + 2);
            suggestor.clearWindow();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (suggestor.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (suggestor.keyPressed(keyCode, scanCode, modifiers)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (suggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, 1000); // Just push the suggestors infront of the text fields

            suggestor.tryRenderWindow(context, mouseX, mouseY);

            matrices.pop();
        }

        @Override
        public int getHeight() {
            return 2 + 14 + 2 + 14 + 2; // padding field padding field padding
        }

        @Override
        public ListElement copy() {
            return new EnchantmentElement(parent, enchant, count);
        }
    }

}
