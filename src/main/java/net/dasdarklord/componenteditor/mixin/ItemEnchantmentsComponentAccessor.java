package net.dasdarklord.componenteditor.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEnchantmentsComponent.class)
public interface ItemEnchantmentsComponentAccessor {

    @Accessor("showInTooltip")
    boolean getShowInTooltip();

}
