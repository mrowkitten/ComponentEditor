package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.mixin.ItemEnchantmentsComponentAccessor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HideFlagsScreen extends FactoryScreen {

    private static ComponentType<ItemEnchantmentsComponent> getEnchantType(ItemStack stack) {
        return stack.isOf(Items.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    private static final Map<String, ComponentHider> hideflags;
    static {
        hideflags = new LinkedHashMap<>();

        hideflags.put("Tooltip", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                if (parent.editing.contains(DataComponentTypes.HIDE_TOOLTIP)) {
                    consumer.accept(Component.of(DataComponentTypes.HIDE_TOOLTIP, null));
                    return false;
                }
                consumer.accept(Component.of(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE));
                return true;
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.HIDE_TOOLTIP);
            }
        });

        hideflags.put("Enchantments", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                ItemEnchantmentsComponent enchantments = parent.editing.get(getEnchantType(parent.editing));
                boolean isShowing = !hidden(parent, enchantments);

                ItemEnchantmentsComponent updated = enchantments.withShowInTooltip(!isShowing);
                consumer.accept(Component.of(getEnchantType(parent.editing), updated));

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

        hideflags.put("Unbreakable", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                UnbreakableComponent component = parent.editing.get(DataComponentTypes.UNBREAKABLE);
                if (component == null) return false;
                consumer.accept(Component.of(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(!component.showInTooltip())));
                return component.showInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                UnbreakableComponent component = parent.editing.getOrDefault(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
                return !component.showInTooltip();
            }

            @Override
            public boolean canToggle(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.UNBREAKABLE);
            }
        });

        hideflags.put("Can Break", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                BlockPredicatesChecker checker = parent.editing.get(DataComponentTypes.CAN_BREAK);
                if (checker == null) return false;
                consumer.accept(Component.of(DataComponentTypes.CAN_BREAK, checker.withShowInTooltip(!checker.showInTooltip())));
                return checker.showInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                BlockPredicatesChecker checker = parent.editing.get(DataComponentTypes.CAN_BREAK);
                if (checker == null) return false;
                return !checker.showInTooltip();
            }

            @Override
            public boolean canToggle(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.CAN_BREAK);
            }
        });

        hideflags.put("Can Place On", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                BlockPredicatesChecker checker = parent.editing.get(DataComponentTypes.CAN_PLACE_ON);
                if (checker == null) return false;
                consumer.accept(Component.of(DataComponentTypes.CAN_PLACE_ON, checker.withShowInTooltip(!checker.showInTooltip())));
                return checker.showInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                BlockPredicatesChecker checker = parent.editing.get(DataComponentTypes.CAN_PLACE_ON);
                if (checker == null) return false;
                return !checker.showInTooltip();
            }

            @Override
            public boolean canToggle(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.CAN_PLACE_ON);
            }
        });

        hideflags.put("Additional Tooltip", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                if (parent.editing.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
                    consumer.accept(Component.of(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, null));
                    return false;
                }
                consumer.accept(Component.of(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
                return true;
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                return parent.editing.get(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP) != null;
            }
        });

        hideflags.put("Armor Trim", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                ArmorTrim trim = parent.editing.get(DataComponentTypes.TRIM);
                if (trim == null) return false;
                consumer.accept(Component.of(DataComponentTypes.TRIM, trim.withShowInTooltip(!trim.showInTooltip())));
                return trim.showInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                ArmorTrim trim = parent.editing.get(DataComponentTypes.TRIM);
                if (trim == null) return false;
                return !trim.showInTooltip();
            }

            @Override
            public boolean canToggle(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.TRIM);
            }
        });

        hideflags.put("Dyed Color", new ComponentHider() {
            @Override
            public boolean toggleHidden(EditItemScreen parent, Consumer<Component<?>> consumer) {
                DyedColorComponent dyed = parent.editing.get(DataComponentTypes.DYED_COLOR);
                if (dyed == null) return false;
                consumer.accept(Component.of(DataComponentTypes.DYED_COLOR, dyed.withShowInTooltip(!dyed.showInTooltip())));
                return dyed.showInTooltip();
            }

            @Override
            public boolean isHidden(EditItemScreen parent) {
                DyedColorComponent dyed = parent.editing.get(DataComponentTypes.DYED_COLOR);
                if (dyed == null) return false;
                return !dyed.showInTooltip();
            }

            @Override
            public boolean canToggle(EditItemScreen parent) {
                return parent.editing.contains(DataComponentTypes.DYED_COLOR);
            }
        });
    }

    private final List<Component<?>> originalComponents = new ArrayList<>();
    private final List<Component<?>> updatedComponents = new ArrayList<>();

    private final HashMap<SimpleButtonWidget, ComponentHider> toggleFlags = new HashMap<>();

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
            Text text = Text.literal(isHidden ? "HIDDEN" : "SHOWN")
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

                Text hidingText = Text.literal(isHiding ? "HIDDEN" : "SHOWN")
                                .formatted(isHiding ? Formatting.RED : Formatting.GREEN);

                btn.setMessage(Text.literal(name).append(" ").append(hidingText));

                if (isOriginal) originalComponents.add(Component.of(component.type(), parent.editing.get(component.type())));
                setUpdatedComponents();
            }).dimensions(5, buttonY, 240, 20).build();
            SimpleButtonWidget hideFlagButton = new SimpleButtonWidget(btnHideFlagButton);

            toggleFlags.put(hideFlagButton, hider);
            addDrawableChild(hideFlagButton);
            buttonY += 22;
        }
    }

    @Override
    public void tick() {
        for (Map.Entry<SimpleButtonWidget, ComponentHider> button : toggleFlags.entrySet()) {
            button.getKey().active = button.getValue().canToggle(parent);
        }
    }

    @Override
    public void save() {
        setUpdatedComponents();
    }

    private void setUpdatedComponents() {
        for (Component<?> component : updatedComponents) {
            if (component.value() == null) {
                parent.editing.remove(component.type());
                continue;
            }
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

        default boolean canToggle(EditItemScreen parent) {
            return true;
        }
    }
}
