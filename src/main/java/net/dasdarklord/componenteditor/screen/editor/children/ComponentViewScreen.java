package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.mixin.ScreenAccessor;
import net.dasdarklord.componenteditor.screen.editor.EditItemChild;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.ScaledTextWidget;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditorTextFieldWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.ItemComponentWidget;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.ComponentIDSuggestor;
import net.dasdarklord.componenteditor.screen.widgets.suggestor.TextWidgetSuggestor;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.equipment.trim.*;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComponentViewScreen extends EditItemChild {

    private ItemStack stack;

    private final TextRenderer font;

    private boolean isAddingComponent;
    private TextFieldWidget componentTextBox;
    private TextWidgetSuggestor componentSuggestor;
    private SimpleButtonWidget addComponentButton;
    private SimpleButtonWidget cancelButton;
    private SimpleButtonWidget addButton;

    private CheckboxWidget showDefaultComponents;

    public ComponentViewScreen(EditItemScreen parent) {
        super(parent);

        font = parent.font;
    }

    @Override
    protected void init() {
        super.init();

        float titleScale = 1.25f;
        addDrawable(new ScaledTextWidget(
                3, 3, font.getWidth("Components"), font.fontHeight,
                ColorUtil.translate("<gradient:#fc9af3:#9aaafc>Components"),
                font,
                titleScale
        ));

        ButtonWidget btnAddButton = ButtonWidget.builder(Text.literal("+").formatted(Formatting.GREEN),btn -> {
            if (!isAddingComponent) isAddingComponent = true;
        }).dimensions(6 + ((int) (font.getWidth("Components") * titleScale) + 1), 3, ((int) (9 * titleScale)) + 1, ((int) (9 * titleScale)) + 1).build();
        addButton = new SimpleButtonWidget(btnAddButton, true, 1f);
        addDrawableChild(addButton);

        componentTextBox = new EditorTextFieldWidget(font, width / 2 - 240 / 2, height / 2 - 14 / 2, 240, 14, Text.empty());
        componentTextBox.setMaxLength("minecraft:enchantment_glint_override".length()); // Longest component id
        componentSuggestor = new ComponentIDSuggestor(ComponentEditor.MC, this, componentTextBox, 8, 0, true, 0x88000000);

        componentTextBox.setChangedListener(s -> componentSuggestor.refresh());

        ButtonWidget btnFinishComponent = ButtonWidget.builder(Text.literal("Add Component"), btn -> {
            isAddingComponent = false;

            ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(Identifier.of(componentTextBox.getText()));
            componentTextBox.setText("");

            ComponentMap defaultComponents = parent.editing.getDefaultComponents();
            if (defaultComponents.contains(type)) {
                ComponentChanges.AddedRemovedPair pair = parent.editing.getComponentChanges().toAddedRemovedPair();
                if (pair.removed().contains(type)) {
                    parent.editing.set((ComponentType<Object>) type, getDefaultComponentTypeValue(type, parent.editing));
                } else parent.alwaysViewComponents.add(type.toString());
            } else {
                parent.editing.set((ComponentType<Object>) type, getDefaultComponentTypeValue(type, parent.editing));
            }

            parent.setChildScreen(null);
        }).dimensions(componentTextBox.getX(), componentTextBox.getY() + 18, 96, 14).build();
        addComponentButton = new SimpleButtonWidget(btnFinishComponent, true, 1f);

        ButtonWidget btnCancelComponent = ButtonWidget.builder(Text.literal("Cancel"), btn -> {
            isAddingComponent = false;
            componentTextBox.setText("");
        }).dimensions(componentTextBox.getX() + 96 + 2, componentTextBox.getY() + 18, 64, 14).build();
        cancelButton = new SimpleButtonWidget(btnCancelComponent, true, 1f);


        int x = 4;
        int y = 4 + ((int) (font.fontHeight * titleScale) + 1) + 4;

        int xSpacing = 69;
        int ySpacing = 69;

        ComponentMap defaultComponents = stack.getItem().getComponents();
        ComponentMap components = stack.getComponents();
        for (Component<?> component : components) {
            if (!parent.showDefaults && !parent.alwaysViewComponents.contains(component.type().toString())) {
                boolean match = defaultComponents.stream().anyMatch(c -> c.type().equals(component.type()) && c.value().equals(component.value()));
                if (match) continue;
            }

            ItemComponentWidget widget = new ItemComponentWidget(parent, font, x, y, component);
            addDrawableChild(widget);

            x += xSpacing;
            if (x + xSpacing > width) {
                y += ySpacing;
                x = 5;
            }
        }

        Text showDefaultText = Text.literal("Show Default Components");
        showDefaultComponents = CheckboxWidget.builder(showDefaultText, font)
                .callback((box, checked) -> {
                    parent.showDefaults = checked;
                    clearAndInit();
                })
                .checked(parent.showDefaults).build();
        showDefaultComponents.setDimensionsAndPosition(17 + font.getWidth(showDefaultText), 17, width - 3 - font.getWidth(showDefaultText) - CheckboxWidget.getCheckboxSize(font) - 4, 2);
        addDrawableChild(showDefaultComponents);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        for (Drawable drawable : ((ScreenAccessor)this).getDrawables()) {
            if (isAddingComponent && isAddComponent(drawable)) { continue; }
            if (isAddingComponent) drawable.render(context, -1, -1, delta);
            else drawable.render(context, mouseX, mouseY, delta);
        }

        if (isAddingComponent) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, 0, 1000);

            context.fillGradient(0, 0, this.width, this.height, 0x60101010, 0x70101010);

            componentTextBox.render(context, mouseX, mouseY, delta);
            addComponentButton.render(context, mouseX, mouseY, delta);
            cancelButton.render(context, mouseX, mouseY, delta);

            componentSuggestor.tryRenderWindow(context, mouseX, mouseY);

            matrices.pop();
        }

        if (componentTextBox != null) {
            if (!isAddingComponent && children().contains(componentTextBox)) {
                remove(componentTextBox);
                remove(addComponentButton);
                remove(cancelButton);
            } else if (isAddingComponent && !children().contains(componentTextBox)) {
                addDrawableChild(componentTextBox);
                addDrawableChild(addComponentButton);
                addDrawableChild(cancelButton);

                setFocused(componentTextBox);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isAddingComponent && componentSuggestor.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isAddingComponent) {
            if (componentSuggestor.keyPressed(keyCode, scanCode, modifiers)) return true;
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE:
                    isAddingComponent = false;
                    return true;
                case GLFW.GLFW_KEY_ENTER:
                    addComponentButton.onPress();
                    return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isAddingComponent && componentSuggestor.mouseClicked(mouseX, mouseY, button)) return true;

        for(Element element : this.children()) {
            boolean clicked = isAddingComponent ? element.mouseClicked(-1, -1, button) : element.mouseClicked(mouseX, mouseY, button);
            if (isAddingComponent && isAddComponent(element)) clicked = element.mouseClicked(mouseX, mouseY, button);
            if (clicked) {
                this.setFocused(element);
                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    private boolean isAddComponent(Object object) {
        return object == componentTextBox || object == addComponentButton || object == cancelButton || (componentSuggestor.getWindow() != null && object == componentSuggestor.getWindow());
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public static Object getDefaultComponentTypeValue(ComponentType<?> type) {
        return getDefaultComponentTypeValue(type, null);
    }

    public static Object getDefaultComponentTypeValue(ComponentType<?> type, ItemStack stack) {
        if (type == DataComponentTypes.CUSTOM_DATA) return NbtComponent.of(new NbtCompound());
        if (type == DataComponentTypes.MAX_STACK_SIZE) return 1;
        if (type == DataComponentTypes.MAX_DAMAGE) return 1;
        if (type == DataComponentTypes.DAMAGE) return 1;
        if (type == DataComponentTypes.UNBREAKABLE) return new UnbreakableComponent(true);
        if (type == DataComponentTypes.CUSTOM_NAME) return stack == null ? Text.literal("") : stack.getName();
        if (type == DataComponentTypes.ITEM_NAME) return stack == null ? Text.literal("") : stack.getItemName();
        if (type == DataComponentTypes.ITEM_MODEL) return stack == null ? Identifier.of("minecraft:stone") : Registries.ITEM.getId(stack.getItem());
        if (type == DataComponentTypes.LORE) return LoreComponent.DEFAULT;
        if (type == DataComponentTypes.RARITY) return Rarity.COMMON;
        if (type == DataComponentTypes.ENCHANTMENTS) return ItemEnchantmentsComponent.DEFAULT;
        if (type == DataComponentTypes.CAN_PLACE_ON) return new BlockPredicatesChecker(List.of(new BlockPredicate(Optional.empty(), Optional.empty(), Optional.empty())), true);
        if (type == DataComponentTypes.CAN_BREAK) return new BlockPredicatesChecker(List.of(new BlockPredicate(Optional.empty(), Optional.empty(), Optional.empty())), true);
        if (type == DataComponentTypes.ATTRIBUTE_MODIFIERS) return AttributeModifiersComponent.DEFAULT;
        if (type == DataComponentTypes.CUSTOM_MODEL_DATA) return new CustomModelDataComponent(0);
        if (type == DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP) return Unit.INSTANCE;
        if (type == DataComponentTypes.HIDE_TOOLTIP) return Unit.INSTANCE;
        if (type == DataComponentTypes.REPAIR_COST) return 0;
//        if (type == DataComponentTypes.CREATIVE_SLOT_LOCK)  This has a packetCodec only, meaning we cannot encode it in NBT
        if (type == DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) return true;
        if (type == DataComponentTypes.INTANGIBLE_PROJECTILE) return Unit.INSTANCE;
        if (type == DataComponentTypes.FOOD) return new FoodComponent(0, 0, false);
        if (type == DataComponentTypes.CONSUMABLE) return new ConsumableComponent(ConsumableComponent.DEFAULT_CONSUME_SECONDS, UseAction.EAT, SoundEvents.ENTITY_GENERIC_EAT, true, List.of());
        if (type == DataComponentTypes.USE_REMAINDER) return new UseRemainderComponent(new ItemStack(Items.STONE));
        if (type == DataComponentTypes.USE_COOLDOWN) return new UseCooldownComponent(1);
        if (type == DataComponentTypes.DAMAGE_RESISTANT) return new DamageResistantComponent(TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.ofVanilla("is_fire")));
        if (type == DataComponentTypes.TOOL) return new ToolComponent(List.of(), 1, 1);
        if (type == DataComponentTypes.ENCHANTABLE) return new EnchantableComponent(1);
        if (type == DataComponentTypes.EQUIPPABLE) return EquippableComponent.builder(EquipmentSlot.HEAD).build();
        if (type == DataComponentTypes.REPAIRABLE) return new RepairableComponent(RegistryEntryList.of());
        if (type == DataComponentTypes.GLIDER) return Unit.INSTANCE;
        if (type == DataComponentTypes.TOOLTIP_STYLE) return Identifier.of("minecraft", "");
        if (type == DataComponentTypes.DEATH_PROTECTION) return new DeathProtectionComponent(List.of());
        if (type == DataComponentTypes.STORED_ENCHANTMENTS) return ItemEnchantmentsComponent.DEFAULT;
        if (type == DataComponentTypes.DYED_COLOR) return new DyedColorComponent(DyedColorComponent.DEFAULT_COLOR, false);
        if (type == DataComponentTypes.MAP_COLOR) return MapColorComponent.DEFAULT;
        if (type == DataComponentTypes.MAP_ID) return new MapIdComponent(0);
        if (type == DataComponentTypes.MAP_DECORATIONS) return MapDecorationsComponent.DEFAULT;
//        if (type == DataComponentTypes.MAP_POST_PROCESSING) This has a packetCodec only, meaning we cannot encode it in NBT
        if (type == DataComponentTypes.CHARGED_PROJECTILES) return ChargedProjectilesComponent.DEFAULT;
        if (type == DataComponentTypes.BUNDLE_CONTENTS) return BundleContentsComponent.DEFAULT;
        if (type == DataComponentTypes.POTION_CONTENTS) return PotionContentsComponent.DEFAULT;
        if (type == DataComponentTypes.SUSPICIOUS_STEW_EFFECTS) return SuspiciousStewEffectsComponent.DEFAULT;
        if (type == DataComponentTypes.WRITABLE_BOOK_CONTENT) return WritableBookContentComponent.DEFAULT;
        if (type == DataComponentTypes.WRITTEN_BOOK_CONTENT) return WrittenBookContentComponent.DEFAULT;
        if (type == DataComponentTypes.TRIM) return getArmorTrimData();
        if (type == DataComponentTypes.DEBUG_STICK_STATE) return new DebugStickStateComponent(Map.of());
        if (type == DataComponentTypes.ENTITY_DATA) return getEntityData(stack);
        if (type == DataComponentTypes.BUCKET_ENTITY_DATA) return NbtComponent.of(new NbtCompound());
        if (type == DataComponentTypes.BLOCK_ENTITY_DATA) return getBlockEntityData(stack);
        if (type == DataComponentTypes.INSTRUMENT) return DataComponentTypes.INSTRUMENT.getCodec().parse(ComponentEditor.MC.world.getRegistryManager().getOps(NbtOps.INSTANCE), NbtString.of("minecraft:ponder_goat_horn")).getOrThrow();
        if (type == DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER) return new OminousBottleAmplifierComponent(0);
        if (type == DataComponentTypes.JUKEBOX_PLAYABLE) return Items.MUSIC_DISC_CHIRP.getComponents().get(DataComponentTypes.JUKEBOX_PLAYABLE);
        if (type == DataComponentTypes.RECIPES) return List.of(RegistryKey.of(RegistryKeys.RECIPE, Identifier.ofVanilla("stone_sword")));
        if (type == DataComponentTypes.LODESTONE_TRACKER) return new LodestoneTrackerComponent(Optional.empty(), false);
        if (type == DataComponentTypes.FIREWORK_EXPLOSION) return FireworkExplosionComponent.DEFAULT;
        if (type == DataComponentTypes.FIREWORKS) return new FireworksComponent(0, List.of());
        if (type == DataComponentTypes.PROFILE) return new ProfileComponent(ComponentEditor.MC.getGameProfile());
        if (type == DataComponentTypes.NOTE_BLOCK_SOUND) return NoteBlockInstrument.PLING.getSound().getKey().orElseThrow().getValue();
        if (type == DataComponentTypes.BANNER_PATTERNS) return BannerPatternsComponent.DEFAULT;
        if (type == DataComponentTypes.BASE_COLOR) return DyeColor.BROWN;
        if (type == DataComponentTypes.POT_DECORATIONS) return new Sherds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        if (type == DataComponentTypes.CONTAINER) return ContainerComponent.DEFAULT;
        if (type == DataComponentTypes.BLOCK_STATE) return BlockStateComponent.DEFAULT;
        if (type == DataComponentTypes.BEES) return List.<BeehiveBlockEntity.BeeData>of();
        if (type == DataComponentTypes.LOCK) return ContainerLock.EMPTY;
        if (type == DataComponentTypes.CONTAINER_LOOT) return new ContainerLootComponent(LootTables.DESERT_PYRAMID_CHEST, 0);

        return null;
    }

    private static ArmorTrim getArmorTrimData() {
        ArmorTrimMaterial material = ComponentEditor.MC.world.getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL).get(ArmorTrimMaterials.AMETHYST);
        RegistryEntry<ArmorTrimMaterial> materialEntry = RegistryEntry.of(material);

        ArmorTrimPattern pattern = ComponentEditor.MC.world.getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN).get(ArmorTrimPatterns.FLOW);
        RegistryEntry<ArmorTrimPattern> patternEntry = RegistryEntry.of(pattern);
        return new ArmorTrim(materialEntry, patternEntry);
    }

    private static NbtComponent getBlockEntityData(ItemStack stack) {
        NbtCompound compound = new NbtCompound();
        if (stack == null) compound.putString("id", "minecraft:stone");
        else compound.putString("id", Registries.ITEM.getId(stack.getItem()).toString());

        return NbtComponent.of(compound);
    }

    private static NbtComponent getEntityData(ItemStack stack) {
        NbtCompound compound = new NbtCompound();
        if (stack.getItem() instanceof SpawnEggItem spawnEggItem) {
            compound.putString("id", Registries.ENTITY_TYPE.getId(spawnEggItem.getEntityType(stack)).toString());
        } else compound.putString("id", Registries.ENTITY_TYPE.getId(EntityType.PIG).toString());

        return NbtComponent.of(compound);
    }

}
