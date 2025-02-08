package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.screen.editor.EditItemChild;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.ScaledTextWidget;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FactorySelectionScreen extends EditItemChild {

    private static final HashMap<String, Function<EditItemScreen, FactoryScreen>> factories;
    static {
        factories = new HashMap<>(Map.of(
                "Lore", LoreFactoryScreen::new,
                "Attributes", AttributeFactoryScreen::new,
                "Enchantments", EnchantmentFactoryScreen::new,
                "Hide Flags", HideFlagsScreen::new
        ));
    }

    private final TextRenderer font;

    public FactorySelectionScreen(EditItemScreen parent) {
        super(parent);

        this.font = parent.font;
    }

    @Override
    protected void init() {
        super.init();

        float titleScale = 1.25f;
        addDrawable(new ScaledTextWidget(
                3, 3, font.getWidth("Edit Cool Things"), font.fontHeight,
                ColorUtil.translate("<gradient:#fc9af3:#9aaafc>Edit Cool Things"),
                font,
                titleScale
        ));

        ButtonWidget btnBackButton = ButtonWidget.builder(Text.literal("Back"), btn -> {
            parent.setChildScreen(null);
        }).dimensions(width - 45 - 2, 2, 45, 20).build();
        SimpleButtonWidget backButton = new SimpleButtonWidget(btnBackButton);
        addDrawableChild(backButton);

        int buttonY = 4 + ((int) (font.fontHeight * titleScale) + 1);

        for (Map.Entry<String, Function<EditItemScreen, FactoryScreen>> entry : factories.entrySet()) {
            String name = entry.getKey();
            Function<EditItemScreen, FactoryScreen> getter = entry.getValue();

            Text text = Text.literal(name);

            ButtonWidget btnFactoryButton = ButtonWidget.builder(text, btn -> {
                parent.setChildScreen(getter.apply(parent));
            }).dimensions(5, buttonY, 120, 20).build();
            SimpleButtonWidget factoryButton = new SimpleButtonWidget(btnFactoryButton);

            addDrawableChild(factoryButton);
            buttonY += 22;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}
