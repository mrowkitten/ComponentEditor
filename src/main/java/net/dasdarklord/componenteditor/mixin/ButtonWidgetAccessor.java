package net.dasdarklord.componenteditor.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ButtonWidget.class)
public interface ButtonWidgetAccessor {

    @Accessor(value = "onPress")
    ButtonWidget.PressAction getOnPress();

    @Accessor(value = "narrationSupplier")
    ButtonWidget.NarrationSupplier getNarrationSupplier();

}
