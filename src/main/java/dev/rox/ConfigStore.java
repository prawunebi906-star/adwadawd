package dev.rox;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** config/rox.json: per module its enabled flag (if it persists) and settings. A bad file is ignored, never fatal. */
public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStore() { }

    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("rox.json"); }

    public static void save(List<Module> modules) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (Module m : modules) {
            Map<String, Object> e = new LinkedHashMap<>();
            if (m.persist) e.put("enabled", m.enabled());
            for (Setting s : m.settings) e.put(s.key, switch (s.kind) { case BOOL -> s.bool(); case NUM -> s.num(); case CHOICE -> s.option(); });
            root.put(m.name, e);
        }
        try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) { GSON.toJson(root, w); }
        catch (Exception e) { RoxClient.LOG.warn("could not save rox.json: {}", e.toString()); }
    }

    public static void load(List<Module> modules) {
        if (!Files.exists(file())) return;
        try (Reader r = Files.newBufferedReader(file(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (Module m : modules) {
                if (!root.has(m.name) || !root.get(m.name).isJsonObject()) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                for (Setting s : m.settings) {
                    if (!o.has(s.key)) continue;
                    try {
                        switch (s.kind) {
                            case BOOL -> s.setBool(o.get(s.key).getAsBoolean());
                            case NUM -> s.set(o.get(s.key).getAsDouble());
                            case CHOICE -> s.setOption(o.get(s.key).getAsString());
                        }
                    } catch (RuntimeException ignored) { }
                }
                if (m.persist && o.has("enabled") && o.get("enabled").getAsBoolean()) m.setEnabled(true);
            }
        } catch (Exception e) { RoxClient.LOG.warn("rox.json unreadable, using defaults: {}", e.toString()); }
    }
}
