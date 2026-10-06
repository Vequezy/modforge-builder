package com.modgen.flowclient.config;

import com.modgen.flowclient.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FlowConfig {
    private static final Logger LOG = LoggerFactory.getLogger("Flow Client");
    private final Path directory = FabricLoader.getInstance().getConfigDir().resolve("flowclient");
    private final ModuleManager modules;
    public int theme;
    public boolean compact;
    public String category = "Combat";
    public FlowConfig(ModuleManager modules) { this.modules = modules; }
    public boolean save(String profile) {
        Properties properties = new Properties();
        properties.setProperty("theme", Integer.toString(theme));
        properties.setProperty("compact", Boolean.toString(compact));
        properties.setProperty("category", category);
        modules.modules.forEach(m -> properties.setProperty("module." + m.name, Boolean.toString(m.enabled)));
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(directory.resolve(profile + ".properties"))) {
                properties.store(writer, "Flow Client 1.0.0 — local interface and utility settings");
            }
            return true;
        } catch (Exception e) { LOG.error("Could not save Flow profile", e); return false; }
    }
    public boolean load(String profile) {
        Path path = directory.resolve(profile + ".properties");
        if (!Files.exists(path)) return false;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
            int loadedTheme = Integer.parseInt(properties.getProperty("theme", "0"));
            theme = Math.floorMod(loadedTheme, 3);
            compact = Boolean.parseBoolean(properties.getProperty("compact", "false"));
            String loadedCategory = properties.getProperty("category", "Combat");
            category = ModuleManager.CATEGORIES.contains(loadedCategory) ? loadedCategory : "Combat";
            modules.modules.forEach(m -> m.enabled = Boolean.parseBoolean(properties.getProperty("module." + m.name, "false")));
            return true;
        } catch (Exception e) { LOG.error("Could not load Flow profile", e); return false; }
    }
}
