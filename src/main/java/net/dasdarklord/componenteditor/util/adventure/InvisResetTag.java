package net.dasdarklord.componenteditor.util.adventure;

import net.dasdarklord.componenteditor.screen.widgets.editor.EditItemEditBoxWidget;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * An invisible {@literal <reset>} tag, used for {@link net.dasdarklord.componenteditor.screen.widgets.CEWidgetUtil#setMiniMessageProvider(EditItemEditBoxWidget)}
 */
public final class InvisResetTag {
    private static final String RESET = "ce_invis_reset";

    public static final TagResolver RESOLVER = TagResolver.resolver(RESET, ParserDirective.RESET); // Not serializable -- we don't reeealy want to encourage its use

    private InvisResetTag() {
    }
}
