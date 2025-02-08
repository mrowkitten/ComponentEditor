package net.dasdarklord.componenteditor.screen.widgets.suggestor;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.*;
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

public class ComponentIDSuggestor extends TextWidgetSuggestor {

    private static final Logger log = LoggerFactory.getLogger(ComponentIDSuggestor.class);
    private final RegistryWrapper.WrapperLookup lookup;
    private final RegistryWrapper.Impl<ComponentType<?>> componentRegistry;

    public ComponentIDSuggestor(MinecraftClient client, ParentElement owner, TextFieldWidget textField, int maxSuggestionSize, int inWindowIndexOffset, boolean windowAbove, int color) {
        super(client, owner, textField, maxSuggestionSize, inWindowIndexOffset, windowAbove, color);

        lookup = BuiltinRegistries.createWrapperLookup();
        componentRegistry = lookup.getOrThrow(RegistryKeys.DATA_COMPONENT_TYPE);
    }

    @Override
    public void refresh() {
        if (window == null || !this.completingSuggestions) {
            try {
                Identifier id = Identifier.of(textField.getText());
                Optional<RegistryEntry.Reference<ComponentType<?>>> opt = componentRegistry.getOptional(RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, id));
                if (opt.isPresent()) if (opt.get().value().getCodec() == null) opt = Optional.empty(); // Filter out components that cannot be encoded to NBT
                if (opt.isPresent()) {
                    pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(new StringRange(0, 0), List.of()));
                    clearWindow();
                    textField.setSuggestion(null);
                    return; // Don't suggest anything if we have a valid component id
                }
            } catch (Exception ignored) { }

            Stream<RegistryKey<ComponentType<?>>> keys = componentRegistry.streamKeys()
                    .filter(key -> Registries.DATA_COMPONENT_TYPE.get(key).getCodec() != null); // Filter out components that cannot be encoded to NBT
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

        Optional<RegistryEntry.Reference<ComponentType<?>>> opt = id == null
                    ? Optional.empty()
                    : componentRegistry.getOptional(RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, id));
        if (opt.isPresent()) if (opt.get().value().getCodec() == null) opt = Optional.empty(); // Filter out components that cannot be encoded to NBT
        if (opt.isEmpty()) {
            return OrderedText.styledForwardsVisitedString(original, Style.EMPTY.withColor(Formatting.RED)); // Invalid ID
        }

        return super.provideRenderText(original, firstCharacterIndex);
    }

}
