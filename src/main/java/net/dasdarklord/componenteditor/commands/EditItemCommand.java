package net.dasdarklord.componenteditor.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.EditItemScreen;
import net.dasdarklord.componenteditor.util.ColorUtil;
import net.dasdarklord.componenteditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EditItemCommand implements ClientCommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        commandDispatcher.register(literal("edititem").executes(x -> {
            PlayerEntity player = MinecraftClient.getInstance().player;

            int slot = player.getInventory().selectedSlot;
            ItemStack holding = player.getInventory().getMainHandStack();

            if (holding.isEmpty()) {
                player.sendMessage(ColorUtil.translate("<dark_red><bold>!!</bold> <red>Cannot edit nothing"), false);

                return 0;
            }

            EditItemScreen screen = new EditItemScreen(holding, edited -> ItemUtil.changeHotBarItem(edited, slot));

            ComponentEditor.executor.submit(() -> {
                ComponentEditor.threadSleep(100);
                ComponentEditor.setScreen(screen);
            });

            return 1;
        }));
    }

}
