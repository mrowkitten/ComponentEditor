package net.dasdarklord.componenteditor.screen.editor.app;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.HoverableTextWidget;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.util.Characters;
import net.dasdarklord.componenteditor.util.ChatUtil;
import net.dasdarklord.componenteditor.util.adventure.ComponentConverter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class CharactersApp extends EditItemApp {

    public static void reloadChars() {
        characters = new ArrayList<>();

        List<String> charactersJson = Characters.getCharacters();
        for (String json : charactersJson) {
            characters.add(TextCodecs.CODEC.parse(JsonOps.INSTANCE, ComponentEditor.GSON.fromJson(json, JsonObject.class)).result().orElseThrow());
        }
    }

    private static List<Text> characters;

    private final TextRenderer font;

    private int openPage = -1;
    private Text pageCharacters = Text.empty();

    private final List<TextWidget> characterWidgets = new ArrayList<>();
    private TextWidget title;

    private boolean firstInit;

    public CharactersApp(EditItemScreen parent) {
        super(parent);
        font = parent.font;
        firstInit = true;
    }

    @Override
    protected void init() {
        if (firstInit) {
            firstInit = false;
            setPage(0);
            return;
        }

        ButtonWidget btnCloseButton = ButtonWidget.builder(Text.literal("x"), btn -> {
            close();
        }).dimensions(width - 10 - 2, 2, 10, 10).build();
        SimpleButtonWidget closeButton = new SimpleButtonWidget(btnCloseButton, true, 1);
        addDrawableChild(closeButton);

        title = new TextWidget(2, 2, getWidth() - 2, 10, Text.literal("Characters"), font);
        title.alignLeft();
        addDrawableChild(title);

        characterWidgets.clear();
        List<OrderedText> wrapped = ChatUtil.breakRenderedChatMessageLines(pageCharacters, getWidth() - 4, font);

        int y = 15;
        for (OrderedText line : wrapped) {
            Text converted = ComponentConverter.orderedToText(line);

            TextWidget widget = new HoverableTextWidget(2, y, getWidth() - 4, font.fontHeight + 1, client, converted, font);
            widget.alignLeft();

            addDrawableChild(widget);
            characterWidgets.add(widget);

            y += font.fontHeight + 1;
        }

        int titleWidth = font.getWidth(title.getMessage());
        int x = titleWidth + 8;
        for (int page = 0; page < characters.size(); page++) {
            Text pageText = Text.literal(Integer.toString(page + 1));
            if (page == openPage) pageText = Text.literal(Integer.toString(page + 1)).formatted(Formatting.AQUA);

            Tooltip tooltip = Tooltip.of(Text.literal("Page " + (page + 1)));

            int finalPage = page;
            ButtonWidget btnChangePageButton = ButtonWidget.builder(pageText, btn -> {
                if (finalPage != openPage) setPage(finalPage);
            }).dimensions(x, 2, 10, 10).tooltip(tooltip).build();
            SimpleButtonWidget changePageButton = new SimpleButtonWidget(btnChangePageButton, true, 1);
            addDrawableChild(changePageButton);

            x += 10;
        }
    }

    private void setPage(int page) {
        if (page > characters.size() - 1) return;
        pageCharacters = characters.get(page);
        openPage = page;
        clearAndInit();
    }

    @Override
    public int getWidth() {
        return 200;
    }

    @Override
    public int getHeight() {
        int height = characterWidgets.size() * font.fontHeight + characterWidgets.size();
        return Math.max(75, height + 17);
    }

}
