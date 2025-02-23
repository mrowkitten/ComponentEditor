package net.dasdarklord.componenteditor.screen.editor.app;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.screen.widgets.SimpleButtonWidget;
import net.dasdarklord.componenteditor.screen.widgets.apps.ColorPickerWidget;
import net.dasdarklord.componenteditor.screen.widgets.apps.ColorPresetWidget;
import net.dasdarklord.componenteditor.screen.widgets.editor.EditorTextFieldWidget;
import net.dasdarklord.componenteditor.util.HSBColor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ColorPickerApp extends EditItemApp {

    public static List<Color> recentColors = new ArrayList<>();
    public static Color lastColorS;

    private final TextRenderer font;

    private boolean dontUpdateField;
    private HSBColor lastColor;
    private ColorPickerWidget colorPicker;

    private TextWidget title;

    private TextFieldWidget hexField;

    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;

    private SimpleButtonWidget copyButton;

    private final List<ColorPresetWidget> colorPresetWidgets = new ArrayList<>();

    public ColorPickerApp(EditItemScreen parent) {
        super(parent);
        this.font = parent.font;
    }

    @Override
    public int getWidth() {
        return 245;
    }

    @Override
    public int getHeight() {
        return 125;
    }

    @Override
    protected void init() {
        ButtonWidget btnCloseButton = ButtonWidget.builder(Text.literal("x"), btn -> {
            close();
        }).dimensions(width - 10 - 2, 2, 10, 10).build();
        SimpleButtonWidget closeButton = new SimpleButtonWidget(btnCloseButton, true, 1);
        addDrawableChild(closeButton);

        title = new TextWidget(2, 2, getWidth() - 2, 10, Text.literal("Color Picker"), font);
        title.alignLeft();
        addDrawableChild(title);

        HSBColor defaultColor = new HSBColor(0, 1, 1);
        if (lastColorS != null) defaultColor = HSBColor.of(lastColorS);

        colorPicker = new ColorPickerWidget(2, 16, defaultColor);
        addDrawableChild(colorPicker);

        int hexFieldWidth = font.getWidth("#FFFFFF") + 16;
        hexField = new EditorTextFieldWidget(font, colorPicker.getWidth() + 15, 14, hexFieldWidth, 14, Text.empty());
        addDrawableChild(hexField);

        hexField.setChangedListener(text -> {
            try {
                Color c = Color.decode(text);
                colorPicker.pickedColor = HSBColor.of(c);
                dontUpdateField = true;
            } catch (NumberFormatException ignored) {}
        });

        int colorFieldWidth = font.getWidth("255") + 8;
        redField = new EditorTextFieldWidget(font, colorPicker.getWidth() + 15, 14 + 16, colorFieldWidth, 14, Text.empty());
        greenField = new EditorTextFieldWidget(font, colorPicker.getWidth() + 15 + colorFieldWidth + 4, 14 + 16, colorFieldWidth, 14, Text.empty());
        blueField = new EditorTextFieldWidget(font, colorPicker.getWidth() + 15 + (colorFieldWidth + 4) * 2, 14 + 16, colorFieldWidth, 14, Text.empty());
        addDrawableChild(redField);
        addDrawableChild(greenField);
        addDrawableChild(blueField);

        redField.setChangedListener(text -> {
            try {
                Color c = colorPicker.pickedColor.toColor();
                Color c2 = new Color(Integer.parseInt(text), c.getGreen(), c.getBlue());
                colorPicker.pickedColor = HSBColor.of(c2);
                dontUpdateField = true;
            } catch (NumberFormatException ignored) {}
        });
        greenField.setChangedListener(text -> {
            try {
                Color c = colorPicker.pickedColor.toColor();
                Color c2 = new Color(c.getRed(), Integer.parseInt(text), c.getBlue());
                colorPicker.pickedColor = HSBColor.of(c2);
                dontUpdateField = true;
            } catch (NumberFormatException ignored) {}
        });
        blueField.setChangedListener(text -> {
            try {
                Color c = colorPicker.pickedColor.toColor();
                Color c2 = new Color(c.getRed(), c.getGreen(), Integer.parseInt(text));
                colorPicker.pickedColor = HSBColor.of(c2);
                dontUpdateField = true;
            } catch (NumberFormatException ignored) {}
        });

        int copyButtonWidth = font.getWidth("Copy") + 16;
        ButtonWidget btnCopyButton = ButtonWidget.builder(Text.literal("Copy"), btn -> {
            Color color = lastColor.toColor();
            String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

            ComponentEditor.MC.keyboard.setClipboard(hex);

            if (recentColors.contains(color)) {
                recentColors.remove(color);
                recentColors.addFirst(color);
            } else {
                recentColors.addFirst(color);
                if (recentColors.size() > 15) recentColors.removeLast();
            }

            updateRecentColors();
        }).dimensions(colorPicker.getWidth() + 15, 46, copyButtonWidth, 16).build();
        copyButton = new SimpleButtonWidget(btnCopyButton, true, 1);
        addDrawableChild(copyButton);

        updateRecentColors();
    }

    @Override
    public void close() {
        if (lastColor != null) lastColorS = lastColor.toColor();
        super.close();
    }

    @Override
    public void tick() {
        if (!colorPicker.pickedColor.equals(lastColor)) {
            lastColor = new HSBColor(colorPicker.pickedColor.getHue(), colorPicker.pickedColor.getSaturation(), colorPicker.pickedColor.getBrightness());
            Color color = Color.getHSBColor(lastColor.getHue(), lastColor.getSaturation(), lastColor.getBrightness());

            title.setTextColor(color.getRGB());

            if (dontUpdateField) {
                dontUpdateField = false;
            } else {
                String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                hexField.setText(hex);

                redField.setText(Integer.toString(color.getRed()));
                greenField.setText(Integer.toString(color.getGreen()));
                blueField.setText(Integer.toString(color.getBlue()));
            }

        }
    }

    private void updateRecentColorsNoCall() {
        for (ColorPresetWidget widget : colorPresetWidgets) {
            remove(widget);
        }

        int row = 0;
        int column = 0;
        for (Color c : recentColors) {
            int x = column * 18 + colorPicker.getWidth() + 15;
            int y = row * 18 + 64;
            ColorPresetWidget widget = new ColorPresetWidget(x, y, c, w -> {
                colorPicker.pickedColor = HSBColor.of(c);
                String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                ComponentEditor.MC.keyboard.setClipboard(hex);
            });

            addDrawableChild(widget);
            colorPresetWidgets.add(widget);

            column++;
            if (column > 4) {
                column = 0;
                row++;
            }
        }
    }

    public void updateRecentColors() {
        for (ColorPickerApp app : parent.getApps(ColorPickerApp.class)) {
            if (app == this) continue;
            app.updateRecentColorsNoCall();
        }

        updateRecentColorsNoCall();
        saveColors();
    }

    private static void saveColors() {
        try {
            List<Color> recentColors = ColorPickerApp.recentColors;
            NbtList nbtList = new NbtList();
            for (Color color : recentColors) {
                String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                nbtList.add(0, NbtString.of(hex));
            }

            Color savedColor = ColorPickerApp.lastColorS;
            if (savedColor == null) savedColor = Color.getHSBColor(0, 1, 1);
            String savedHex = String.format("#%02x%02x%02x", savedColor.getRed(), savedColor.getGreen(), savedColor.getBlue());

            NbtCompound tag = new NbtCompound();
            tag.put("recentColors", nbtList);
            tag.putString("savedColor", savedHex);

            if (!saveFile.exists()) {
                saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
            }
            NbtIo.write(tag, saveFile.toPath());
        } catch (Exception exc) {
            ComponentEditor.LOGGER.error("Couldn't save ColorPicker colors", exc);
        }
    }

    @Override
    protected void refreshWidgetPositions() { }

    private static final File saveFile = new File("./componenteditor/colorpicker.nbt");
    public static void clientStopping() {
        saveColors();
    }

    public static void clientStarting() {
        try {
            if (!saveFile.exists()) return;

            NbtCompound tag = NbtIo.read(saveFile.toPath());

            String savedColor = tag.getString("savedColor");
            NbtList recentColorsNbt = tag.getList("recentColors", NbtList.STRING_TYPE);

            for (NbtElement color : recentColorsNbt) {
                if (color.getType() != NbtList.STRING_TYPE) continue;
                NbtString string = (NbtString) color;
                String hex = string.asString();
                Color c = Color.decode(hex);
                recentColors.add(c);
            }

            lastColorS = Color.decode(savedColor);
        } catch (Exception exc) {
            ComponentEditor.LOGGER.error("Couldn't load ColorPicker colors", exc);
        }
    }

}
