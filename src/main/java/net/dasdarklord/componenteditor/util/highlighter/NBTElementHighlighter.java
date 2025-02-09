package net.dasdarklord.componenteditor.util.highlighter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Formatting;

public class NBTElementHighlighter implements Highlighter {
    private String cachedInput;
    private Component cachedOutput;

    @Override
    public Component highlight(String input) {
        if (input.equals(cachedInput)) {
            return cachedOutput;
        }

        Component component = Component.empty();

        StringReader reader = new StringReader(input);
        component = appendSkipWhitespace(component, reader);

        if (!reader.canRead()) return Component.empty();
        try {
            StringNbtReader nbtReader = new StringNbtReader(new StringReader(input));
            nbtReader.parseElement();

            Component comp = component.append(highlight(reader, input));
            comp = appendRemaining(comp, reader);

            cachedInput = input;
            cachedOutput = comp;

            return comp;
        } catch (Exception e) {
            return Component.text(input).color(TextColor.color(Formatting.RED.getColorValue()));
        }
    }

    private Component highlight(StringReader reader, String input) {
        if (!reader.canRead()) return Component.empty();
        char c = reader.peek();
        if (c == '{') {
            Component componentText = Component.empty();

            componentText = appendSkip(componentText, reader);
            componentText = appendSkipWhitespace(componentText, reader);

            if (reader.canRead() && reader.peek() == '}') {
                componentText = appendSkip(componentText, reader);
                return componentText;
            }

            while (reader.canRead() && reader.peek() != '}') {
                Component txt = highlight(reader, input);
                Component whitespace = appendSkipWhitespace(Component.empty(), reader);
                if (reader.canRead() && (reader.peek() == ':' || reader.peek() == ',')) {
                    if (reader.peek() == ':') {
                        componentText = componentText.append(txt.color(TextColor.color(Formatting.AQUA.getColorValue())))
                                                     .append(whitespace);
                    } else {
                        componentText = componentText.append(txt).append(whitespace);
                    }
                    componentText = appendSkip(componentText, reader);
                    componentText = appendSkipWhitespace(componentText, reader);
                } else {
                    componentText = componentText.append(txt).append(whitespace);
                }
            }
            componentText = appendSkipWhitespace(componentText, reader);
            if (reader.canRead()) componentText = appendSkip(componentText, reader);

            return componentText;
        }

        if (c == '[') {
            Component listText = Component.empty();

            listText = appendSkip(listText, reader);
            listText = appendSkipWhitespace(listText, reader);

            if (reader.canRead() && reader.peek() == ']') {
                listText = appendSkip(listText, reader);
                return listText;
            }

            while (reader.canRead() && reader.peek() != ']') {
                listText = listText.append(highlight(reader, input));
                listText = appendSkipWhitespace(listText, reader);
                if (reader.canRead() && reader.peek() == ',') {
                    listText = appendSkip(listText, reader);
                    listText = appendSkipWhitespace(listText, reader);
                }
            }
            listText = appendSkipWhitespace(listText, reader);
            if (reader.canRead()) listText = appendSkip(listText, reader);

            return listText;
        }

        if (Character.isAlphabetic(c) || c == '\"' || c == '\'') {
            Character start = null;
            if (c == '\"' || c == '\'') start = c;
            boolean full = true;
            String s;
            try {
                if (c == '\"' || c == '\'') s = reader.readQuotedString();
                else s = reader.readUnquotedString();
            } catch (CommandSyntaxException ignored) {
                reader.skip();
                s = reader.readUnquotedString();
                full = false;
            }
            if (start != null && full) {
                return text(start).color(TextColor.color(Formatting.GREEN.getColorValue())).append(Component.text(s)).append(Component.text(start));
            } else if (start != null) {
                return text(start).color(TextColor.color(Formatting.GREEN.getColorValue())).append(Component.text(s));
            }
            return text(s).color(TextColor.color(Formatting.GREEN.getColorValue()));
        }

        if (Character.isDigit(c) || c == '.' || c == '-' || c == '+') {
            StringBuilder s = new StringBuilder();
            while (reader.canRead()) {
                c = reader.peek();
                if (!(Character.isDigit(c) || c == '.' || c == '-' || c == '+')) break;
                reader.skip();
                s.append(c);
            }
            Component numberText = text(s.toString()).color(TextColor.color(Formatting.GOLD.getColorValue()));
            if (reader.canRead() && Character.isAlphabetic(reader.peek())) {
                numberText = numberText.append(text(reader.read()).color(TextColor.color(Formatting.RED.getColorValue())));
            }
            return numberText;
        }

        return text(input);
    }

    private Component appendRemaining(Component component, StringReader reader) {
        return component.append(text(reader.getRemaining()));
    }

    private Component appendSkip(Component component, StringReader reader) {
        char skipped = reader.peek();
        reader.skip();
        return component.append(text(Character.toString(skipped)));
    }

    private Component appendSkipWhitespace(Component component, StringReader reader) {
        int whitespace = skipWhitespace(reader);
        if (whitespace > 0) return component.append(text(" ".repeat(whitespace)));
        return component;
    }

    private int skipWhitespace(StringReader reader) {
        int count = 0;
        while (reader.canRead() && Character.isWhitespace(reader.peek())) {
            reader.skip();
            count++;
        }
        return count;
    }

    private Component text(String text) {
        return Component.text(text).color(TextColor.color(Formatting.WHITE.getColorValue()));
    }

    private Component text(char c) {
        return Component.text(c).color(TextColor.color(Formatting.WHITE.getColorValue()));
    }

}
