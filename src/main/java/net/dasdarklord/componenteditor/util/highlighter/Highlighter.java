package net.dasdarklord.componenteditor.util.highlighter;

import net.kyori.adventure.text.Component;

public interface Highlighter {
    Component highlight(String input);
}