package net.dasdarklord.componenteditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dasdarklord.componenteditor.commands.EditItemCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            new EditItemCommand().register(dispatcher, access);
        });
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
