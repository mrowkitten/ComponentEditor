package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.mixin.ItemEnchantmentsComponentAccessor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HideFlagsScreen extends FactoryScreen {

    private static final Map<String, ComponentHider> hideflags;
    static {
        hideflags = new HashMap<>();

        hideflags.put("Enchantments", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                ItemEnchantmentsComponent enchantments = parent.editing.getEnchantments();
                boolean isShowing = !hidden(parent, enchantments);

                ItemEnchantmentsComponent updated = enchantments.withShowInTooltip(!isShowing);
                consumer.accept(Component.of(DataComponentTypes.ENCHANTMENTS, updated));

                return isShowing;
            }

            private boolean hidden(EditItemScreen parent, ItemEnchantmentsComponent enchantments) {
                return !((ItemEnchantmentsComponentAccessor)enchantments).getShowInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                ItemEnchantmentsComponent enchantments = parent.editing.getEnchantments();
                return hidden(parent, enchantments);
            }
        });

        hideflags.put("Attributes", new ComponentHider() {

            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                AttributeModifiersComponent attributes = parent.editing.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

                boolean isShowing = attributes.showInTooltip();

                AttributeModifiersComponent updated = attributes.withShowInTooltip(!isShowing);
                consumer.accept(Component.of(DataComponentTypes.ATTRIBUTE_MODIFIERS, updated));

                return isShowing;
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                AttributeModifiersComponent attributes = parent.editing.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
                return !attributes.showInTooltip();
            }
        });
    }

    private final List<Component<?>> originalComponents = new ArrayList<>();
    private final List<Component<?>> updatedComponents = new ArrayList<>();

    protected HideFlagsScreen(EditItemScreen parent) {
        super(parent);
    }

    @Override
    public String getFactoryTitle() {
        return "Editing hide flags";
    }

    @Override
    public String saveButtonText() {
        return "Save HideFlags";
    }

    @Override
    protected void init() {
        super.init();

        int buttonY = 3;

        for (Map.Entry<String, ComponentHider> entry : hideflags.entrySet()) {
            String name = entry.getKey();
            ComponentHider hider = entry.getValue();

            boolean isHidden = hider.isHidden(parent);
            Text text = Text.literal(isHidden ? "DISABLED" : "ENABLED")
                    .formatted(isHidden ? Formatting.RED : Formatting.GREEN);

            ButtonWidget btnHideFlagButton = ButtonWidget.builder(Text.literal(name).append(" ").append(text), btn -> {
                AtomicReference<Component<?>> componentReference = new AtomicReference<>();
                boolean isHiding = hider.toggleHidden(parent, componentReference::set);
                Component<?> component = componentReference.get();

                boolean isOriginal = updatedComponents.stream().noneMatch(x -> x.type().equals(component.type()));

                if (component != null) {
                    updatedComponents.removeIf(comp -> comp.type().equals(component.type()));
                    updatedComponents.add(component);
                }

                Text hidingText = Text.literal(isHiding ? "DISABLED" : "ENABLED")
                                .formatted(isHiding ? Formatting.RED : Formatting.GREEN);

                btn.setMessage(Text.literal(name).append(" ").append(hidingText));

                if (isOriginal) originalComponents.add(Component.of(component.type(), parent.editing.get(component.type())));
                setUpdatedComponents();
            }).dimensions(5, buttonY, 240, 20).build();
            SimpleButtonWidget hideFlagButton = new SimpleButtonWidget(btnHideFlagButton);

            addDrawableChild(hideFlagButton);
            buttonY += 22;
        }
    }

    @Override
    public void save() {
        setUpdatedComponents();
    }

    private void setUpdatedComponents() {
        for (Component<?> component : updatedComponents) {
            parent.editing.set((ComponentType<Object>) component.type(), component.value());
        }
    }

    @Override
    public void cancel() {
        for (Component<?> component : originalComponents) {
            parent.editing.set((ComponentType<Object>) component.type(), component.value());
        }
    }

    private interface ComponentHider {
        boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer);
        boolean isHidden(EditItemScreen parent);
    }
}
