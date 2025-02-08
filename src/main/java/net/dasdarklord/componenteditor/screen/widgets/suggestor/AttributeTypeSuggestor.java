package net.dasdarklord.componenteditor.screen.widgets.suggestor;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AttributeTypeSuggestor extends TextWidgetSuggestor {

    private static final Logger log = LoggerFactory.getLogger(AttributeTypeSuggestor.class);
    private final RegistryWrapper.WrapperLookup lookup;
    private final RegistryWrapper.Impl<EntityAttribute> attributeRegistry;

    public AttributeTypeSuggestor(MinecraftClient client, ParentElement owner, TextFieldWidget textField, int maxSuggestionSize, int inWindowIndexOffset, boolean windowAbove, int color) {
        super(client, owner, textField, maxSuggestionSize, inWindowIndexOffset, windowAbove, color);


        lookup = BuiltinRegistries.createWrapperLookup();
        attributeRegistry = lookup.getOrThrow(RegistryKeys.ATTRIBUTE);
    }

    @Override
    public void refresh() {
        if (window == null || !this.completingSuggestions) {
            try {
                Identifier id = Identifier.of(textField.getText());
                Optional<RegistryEntry.Reference<EntityAttribute>> opt = attributeRegistry.getOptional(RegistryKey.of(RegistryKeys.ATTRIBUTE, id));
                if (opt.isPresent()) {
                    pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(new StringRange(0, 0), List.of()));
                    clearWindow();
                    textField.setSuggestion(null);
                    return; // Don't suggest anything
                }
            } catch (Exception ignored) { }

            Stream<RegistryKey<EntityAttribute>> keys = attributeRegistry.streamKeys();
            SuggestionsBuilder builder = new SuggestionsBuilder(textField.getText(), 0);
            CommandSource.suggestIdentifiers(keys.map(RegistryKey::getValue), builder);
            pendingSuggestions = builder.buildFuture();
            pendingSuggestions.thenRun(this::show);
        }
    }

    @Override
    protected OrderedText provideRenderText(String original, int firstCharacterIndex) {
        Identifier id = null;
        try {
            id = Identifier.of(textField.getText());
        } catch (Exception ignored) { }

        Optional<RegistryEntry.Reference<EntityAttribute>> opt = id == null
                ? Optional.empty()
                : attributeRegistry.getOptional(RegistryKey.of(RegistryKeys.ATTRIBUTE, id));
        if (opt.isEmpty()) {
            valid = false;
            return OrderedText.styledForwardsVisitedString(original, Style.EMPTY.withColor(Formatting.RED)); // Invalid ID
        }

        valid = true;
        return super.provideRenderText(original, firstCharacterIndex);
    }

    private boolean valid;
    public boolean isValid() {
        return valid;
    }

}