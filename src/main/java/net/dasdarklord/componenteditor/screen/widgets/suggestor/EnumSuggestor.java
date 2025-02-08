package net.dasdarklord.componenteditor.screen.widgets.suggestor;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EnumSuggestor<T extends Enum<T>> extends TextWidgetSuggestor {
    private final Class<T> enumClass;

    public EnumSuggestor(MinecraftClient client, ParentElement owner, TextFieldWidget textField, int maxSuggestionSize, int inWindowIndexOffset, boolean windowAbove, int color, Class<T> enumClass) {
        super(client, owner, textField, maxSuggestionSize, inWindowIndexOffset, windowAbove, color);
        this.enumClass = enumClass;
    }

    @Override
    public void refresh() {
        if (window == null || !this.completingSuggestions) {
            try {
                for (T constant : enumClass.getEnumConstants()) {
                    String name = constant.name().toLowerCase();
                    String lowerText = textField.getText().toLowerCase();
                    if (name.equals(lowerText)) {
                        pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(new StringRange(0, 0), List.of()));
                        clearWindow();
                        textField.setSuggestion(null);
                        return; // Don't suggest anything
                    }
                }
            } catch (Exception ignored) { }

            String text = textField.getText();
            SuggestionsBuilder builder = new SuggestionsBuilder(text, 0);
            for (T constant : enumClass.getEnumConstants()) {
                String name = constant.name().toLowerCase();
                if (name.contains(text)) {
                    builder.suggest(name);
                }
            }
            pendingSuggestions = builder.buildFuture();
            pendingSuggestions.thenRun(this::show);
        }
    }

    @Override
    protected OrderedText provideRenderText(String original, int firstCharacterIndex) {
        valid = false;
        for (T constant : enumClass.getEnumConstants()) {
            String name = constant.name().toLowerCase();
            if (name.equals(textField.getText().toLowerCase())) {
                valid = true;
                return super.provideRenderText(original, firstCharacterIndex);
            }
        }

        return OrderedText.styledForwardsVisitedString(original, Style.EMPTY.withColor(Formatting.RED));
    }

    private boolean valid;
    public boolean isValid() {
        return valid;
    }
}
