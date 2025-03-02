package net.dasdarklord.componenteditor.mixin;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiFunction;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {

    @Accessor("TEXTURES")
    public static ButtonTextures getButtonTextures() {
        throw new AssertionError();
    }

    @Accessor("suggestion")
    String getSuggestion();

    @Invoker("updateFirstCharacterIndex")
    void invokeUpdateFirstCharacterIndex(int index);

    @Accessor("editable")
    boolean getIsEditable();

    @Accessor("editableColor")
    int getEditableColor();

    @Accessor("uneditableColor")
    int getUneditableColor();

    @Accessor("selectionStart")
    int getSelectionStart();

    @Accessor("selectionEnd")
    int getSelectionEnd();

    @Accessor("firstCharacterIndex")
    int getFirstCharacterIndex();

    @Accessor("placeholder")
    Text getPlaceholder();

    @Accessor("renderTextProvider")
    BiFunction<String, Integer, OrderedText> getRenderTextProvider();

    @Accessor("lastSwitchFocusTime")
    long getLastSwitchFocusTime();

    @Invoker("getMaxLength")
    int invokeGetMaxTextLength();

    @Accessor("selectionStart")
    void setSelectionStart(int cursor);

    @Accessor("selectionEnd")
    void setSelectionEnd(int cursor);
}
