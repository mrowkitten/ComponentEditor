package net.dasdarklord.componenteditor.screen.editor.children;

import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.CEWidgetUtil;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemEditBoxWidget;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.dasdarklord.componenteditor.util.ItemUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoreFactoryScreen extends FactoryScreen {

    private final List<MutableText> startingLore;
    public EditItemEditBoxWidget loreTextField;

    protected LoreFactoryScreen(EditItemScreen parent) {
        super(parent);
        startingLore = ItemUtil.getTextLore(parent.editing);
    }

    @Override
    public String getFactoryTitle() {
        return "Editing minecraft:lore";
    }

    @Override
    public String saveButtonText() {
        return "Save Lore";
    }

    @Override
    public void save() {
        setLore();
        parent.reloadEditing();
    }

    @Override
    public void cancel() {
        ItemUtil.setTextLore(parent.editing, startingLore);
        parent.loadWidgetItem();
    }

    private void setLore() {
        List<MutableText> lore = new ArrayList<>();

        String text = loreTextField.getText();
        List<String> mmText = Arrays.stream(text.split("\n")).toList();
        for (String mm : mmText) {
            MutableText txt = ColorUtil.translate(mm, true).copy();
            if (txt.getStyle().getColor() == null) {
                txt.styled(s -> s.withColor(Formatting.WHITE));
            }
            lore.add(txt);
        }

        ItemUtil.setTextLore(parent.editing, lore);
        parent.loadWidgetItem();
    }

    @Override
    protected void init() {
        super.init();

        List<String> mmLoreLines = ItemUtil.getTextLore(parent.editing).stream()
                .map(txt -> ColorUtil.translateBack(txt, true))
                .map(str -> str.startsWith("<white>") ? str.replaceFirst("<white>", "") : str)
                .toList();

        loreTextField = new EditItemEditBoxWidget(font, 2, 2, width - 4, height - 4, Text.empty(), Text.empty());
        loreTextField.setMoveScissors(true);
        loreTextField.setText(String.join("\n", mmLoreLines));
        loreTextField.setScrollY(0);
        loreTextField.setChangeListener(s -> setLore());
        CEWidgetUtil.setMiniMessageProvider(loreTextField);
        addDrawableChild(loreTextField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = loreTextField.getText();
        super.resize(client, width, height);
        loreTextField.setText(String.join("\n", text.split("\n"))); // Thank you JetBrains AI for suggesting this peak code. I will leave it like this
    }
}
