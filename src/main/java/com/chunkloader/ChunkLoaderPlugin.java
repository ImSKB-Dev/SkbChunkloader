package com.chunkloader;

import com.chunkloader.command.ChunkLoaderCommand;
import com.chunkloader.listener.ChunkLoaderListener;
import com.chunkloader.manager.ChunkManager;
import com.chunkloader.manager.DataManager;
import com.chunkloader.model.ChunkLoaderData;
import com.chunkloader.scheduler.PlatformScheduler;
import com.chunkloader.util.MaterialAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ChunkLoaderPlugin extends JavaPlugin {

    private static ChunkLoaderPlugin instance;
    private DataManager dataManager;
    private ChunkManager chunkManager;

    private ItemStack loaderBlockItem;
    private ItemStack activatorItem;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        dataManager = new DataManager(this);
        chunkManager = new ChunkManager(this);

        loadItemsFromConfig();

        ChunkLoaderCommand cmd = new ChunkLoaderCommand(this);
        if (getCommand("chunkloader") != null) {
            getCommand("chunkloader").setExecutor(cmd);
            getCommand("chunkloader").setTabCompleter(cmd);
        }

        getServer().getPluginManager().registerEvents(new ChunkLoaderListener(this), this);

        // Schedule delayed task to force load active chunks on startup
        PlatformScheduler.runTaskLater(this, null, this::loadAllActiveChunks, 20L);

        getLogger().info("ChunkLoader ativado com sucesso! Folia: " + PlatformScheduler.isFolia());
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("ChunkLoader desativado com sucesso!");
    }

    public static ChunkLoaderPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        loadItemsFromConfig();
        dataManager.loadData();
    }

    private void loadItemsFromConfig() {
        FileConfiguration cfg = getConfig();

        // Loader block
        String blockMat = cfg.getString("loader-block.material", "REDSTONE_BLOCK");
        short blockDamage = (short) cfg.getInt("loader-block.damage", 0);
        String blockName = colorize(cfg.getString("loader-block.name", "&a&lChunkLoader Block"));
        List<String> blockLore = colorizeList(cfg.getStringList("loader-block.lore"));

        loaderBlockItem = MaterialAdapter.createItemStack(blockMat, 1, blockDamage);
        loaderBlockItem = MaterialAdapter.setDisplayNameAndLore(loaderBlockItem, blockName, blockLore);

        // Activator item
        String actMat = cfg.getString("activator-item.material", "NETHER_STAR");
        short actDamage = (short) cfg.getInt("activator-item.damage", 0);
        String actName = colorize(cfg.getString("activator-item.name", "&e&lAtivador de ChunkLoader"));
        List<String> actLore = colorizeList(cfg.getStringList("activator-item.lore"));

        activatorItem = MaterialAdapter.createItemStack(actMat, 1, actDamage);
        activatorItem = MaterialAdapter.setDisplayNameAndLore(activatorItem, actName, actLore);
    }

    public void setLoaderBlockItem(ItemStack item) {
        if (item == null) return;
        loaderBlockItem = item.clone();
        loaderBlockItem.setAmount(1);

        getConfig().set("loader-block.material", item.getType().name());
        try {
            getConfig().set("loader-block.damage", item.getDurability());
        } catch (Throwable ignored) {
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            getConfig().set("loader-block.name", item.getItemMeta().getDisplayName().replace('§', '&'));
        } else {
            getConfig().set("loader-block.name", null);
        }
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : item.getItemMeta().getLore()) {
                lore.add(line.replace('§', '&'));
            }
            getConfig().set("loader-block.lore", lore);
        } else {
            getConfig().set("loader-block.lore", null);
        }
        saveConfig();
    }

    public void setActivatorItem(ItemStack item) {
        if (item == null) return;
        activatorItem = item.clone();
        activatorItem.setAmount(1);

        getConfig().set("activator-item.material", item.getType().name());
        try {
            getConfig().set("activator-item.damage", item.getDurability());
        } catch (Throwable ignored) {
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            getConfig().set("activator-item.name", item.getItemMeta().getDisplayName().replace('§', '&'));
        } else {
            getConfig().set("activator-item.name", null);
        }
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : item.getItemMeta().getLore()) {
                lore.add(line.replace('§', '&'));
            }
            getConfig().set("activator-item.lore", lore);
        } else {
            getConfig().set("activator-item.lore", null);
        }
        saveConfig();
    }

    public ItemStack getLoaderBlockItemStack() {
        return loaderBlockItem != null ? loaderBlockItem.clone() : new ItemStack(Material.REDSTONE_BLOCK);
    }

    public ItemStack getActivatorItemStack() {
        return activatorItem != null ? activatorItem.clone() : new ItemStack(Material.NETHER_STAR);
    }

    public boolean isLoaderBlockItem(ItemStack item) {
        return matchesItem(item, loaderBlockItem);
    }

    public boolean isActivatorItem(ItemStack item) {
        return matchesItem(item, activatorItem);
    }

    private boolean matchesItem(ItemStack item, ItemStack target) {
        if (item == null || target == null) return false;
        if (item.getType() != target.getType()) return false;

        boolean targetHasMeta = target.hasItemMeta();
        boolean itemHasMeta = item.hasItemMeta();

        if (targetHasMeta != itemHasMeta) {
            return false;
        }

        if (!targetHasMeta) {
            return true;
        }

        ItemMeta targetMeta = target.getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();

        boolean targetHasName = targetMeta.hasDisplayName();
        boolean itemHasName = itemMeta.hasDisplayName();
        if (targetHasName != itemHasName) {
            return false;
        }
        if (targetHasName && !targetMeta.getDisplayName().equals(itemMeta.getDisplayName())) {
            return false;
        }

        boolean targetHasLore = targetMeta.hasLore();
        boolean itemHasLore = itemMeta.hasLore();
        if (targetHasLore != itemHasLore) {
            return false;
        }
        if (targetHasLore && !targetMeta.getLore().equals(itemMeta.getLore())) {
            return false;
        }

        return true;
    }

    public void loadAllActiveChunks() {
        for (ChunkLoaderData data : dataManager.getAllLoaders()) {
            if (data.isActive()) {
                World world = Bukkit.getWorld(data.getWorldName());
                if (world != null) {
                    chunkManager.loadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
                }
            }
        }
    }

    public String getMessage(String key) {
        String msg = getConfig().getString("messages." + key, "&cMensagem não encontrada: " + key);
        return colorize(msg);
    }

    public String colorize(String str) {
        if (str == null) return "";
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public List<String> colorizeList(List<String> list) {
        List<String> res = new ArrayList<>();
        if (list == null) return res;
        for (String s : list) {
            res.add(colorize(s));
        }
        return res;
    }
}
