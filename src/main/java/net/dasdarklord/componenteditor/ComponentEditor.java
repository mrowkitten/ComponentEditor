package net.dasdarklord.componenteditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dasdarklord.componenteditor.commands.EditItemCommand;
import net.dasdarklord.componenteditor.screen.editor.app.ColorPickerApp;
import net.dasdarklord.componenteditor.util.Characters;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.*;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComponentEditor implements ClientModInitializer {

    public static final ExecutorService executor = Executors.newCachedThreadPool();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Gson GSON = new GsonBuilder().setLenient().serializeNulls().disableHtmlEscaping().create();

    public static final String MOD_ID = "componenteditor";
    public static final String MOD_NAME = "Component Editor";
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ColorPickerApp.clientStarting();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            ColorPickerApp.clientStopping();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            new EditItemCommand().register(dispatcher, access);
        });

        Characters.initialize();
    }

    public static void setScreen(Screen screen) {
        if (MC.isOnThread()) {
            MC.setScreen(screen);
            return;
        }

        MC.executeSync(() -> MC.setScreen(screen));
    }

    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted {}", Thread.currentThread().getName());
        }
    }

}
