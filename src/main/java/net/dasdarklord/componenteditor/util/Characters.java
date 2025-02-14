package net.dasdarklord.componenteditor.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.dasdarklord.componenteditor.ComponentEditor;
import net.dasdarklord.componenteditor.screen.editor.app.CharactersApp;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Characters {

    private static final List<String> characters = new ArrayList<>();

    public static List<String> getCharacters() {
        return characters;
    }

    public static void initialize() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of("componenteditor", "characters");
            }

            @Override
            public void reload(ResourceManager manager) {
                characters.clear();

                Map<Identifier, Resource> map = manager.findResources("characters", id -> id.getPath().endsWith(".json"));
                for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
                    Resource resource = entry.getValue();
                    try (InputStream stream = resource.getInputStream()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                        String line = reader.lines().collect(Collectors.joining("\n"));
                        characters.add(line);
                    } catch (Exception e) {
                        ComponentEditor.LOGGER.error("Error occurred while loading resource json {}", entry.getKey().getPath(), e);
                    }
                }

                CharactersApp.reloadChars();
            }
        });
    }

}
