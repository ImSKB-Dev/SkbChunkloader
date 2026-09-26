package com.chunkloader.manager;

import com.chunkloader.ChunkLoaderPlugin;
import com.chunkloader.model.ChunkLoaderData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final ChunkLoaderPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, ChunkLoaderData> loaders = new ConcurrentHashMap<>();

    public DataManager(ChunkLoaderPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    public synchronized void loadData() {
        loaders.clear();
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("loaders");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection sec = section.getConfigurationSection(key);
                if (sec == null) continue;

                String ownerUuidStr = sec.getString("owner-uuid");
                UUID ownerUUID = ownerUuidStr != null ? UUID.fromString(ownerUuidStr) : UUID.randomUUID();
                String ownerName = sec.getString("owner-name", "Unknown");
                String worldName = sec.getString("world");
                int x = sec.getInt("x");
                int y = sec.getInt("y");
                int z = sec.getInt("z");
                int radius = sec.getInt("radius", 0);
                boolean active = sec.getBoolean("active", true);

                ChunkLoaderData loader = new ChunkLoaderData(key, ownerUUID, ownerName, worldName, x, y, z, radius, active);
                loaders.put(key, loader);
            }
        }
    }

    public synchronized void saveData() {
        config = new YamlConfiguration();
        for (ChunkLoaderData loader : loaders.values()) {
            String path = "loaders." + loader.getId();
            config.set(path + ".owner-uuid", loader.getOwnerUUID().toString());
            config.set(path + ".owner-name", loader.getOwnerName());
            config.set(path + ".world", loader.getWorldName());
            config.set(path + ".x", loader.getX());
            config.set(path + ".y", loader.getY());
            config.set(path + ".z", loader.getZ());
            config.set(path + ".radius", loader.getRadius());
            config.set(path + ".active", loader.isActive());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public void addLoader(ChunkLoaderData loader) {
        loaders.put(loader.getId(), loader);
        saveData();
    }

    public void removeLoader(String id) {
        loaders.remove(id);
        saveData();
    }

    public ChunkLoaderData getLoader(String id) {
        return loaders.get(id);
    }

    public Collection<ChunkLoaderData> getAllLoaders() {
        return loaders.values();
    }

    public List<ChunkLoaderData> getLoadersByOwner(UUID ownerUUID) {
        List<ChunkLoaderData> result = new ArrayList<>();
        for (ChunkLoaderData loader : loaders.values()) {
            if (loader.getOwnerUUID().equals(ownerUUID)) {
                result.add(loader);
            }
        }
        return result;
    }
}
