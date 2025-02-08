package net.dasdarklord.componenteditor.util;

import net.dasdarklord.componenteditor.ComponentEditor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtil {

    public static int getRemoteSlot(int slot) {
        if (slot == 40) return 45;

        if (0 <= slot && slot < 9) {
            return slot + 36;
        }

        return slot;
    }

    public static void changeHotBarItem(ItemStack stack, int slot) {
        changeItem(stack, 36 + slot);
    }

    public static void changeItem(ItemStack stack, int slot) {
        assert ComponentEditor.MC.interactionManager != null;
        ComponentEditor.MC.interactionManager.clickCreativeStack(stack, slot);
    }

    public static <T extends Text> void setTextLore(ItemStack itemStack, List<T> texts) {
        List<Text> loreLines = new ArrayList<>(texts);
        itemStack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
    }

    public static <T extends Text> List<T> getTextLore(ItemStack itemStack) {
        LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);
        if (loreComponent == null) return new ArrayList<>();
        List<Text> lines = loreComponent.lines();
        return lines.stream().map(t -> (T) t).collect(Collectors.toList());
    }

}
