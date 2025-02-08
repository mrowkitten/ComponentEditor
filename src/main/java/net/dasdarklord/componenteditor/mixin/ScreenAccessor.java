package net.dasdarklord.componenteditor.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Accessor(value = "title")
    void setTitle(Text title);

    @Accessor("drawables")
    List<Drawable> getDrawables();

}
