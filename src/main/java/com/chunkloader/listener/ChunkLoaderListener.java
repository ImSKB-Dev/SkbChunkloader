package com.chunkloader.listener;

import com.chunkloader.ChunkLoaderPlugin;
import com.chunkloader.gui.ChunkLoaderGUI;
import com.chunkloader.model.ChunkLoaderData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ChunkLoaderListener implements Listener {

    private final ChunkLoaderPlugin plugin;

    public ChunkLoaderListener(ChunkLoaderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!plugin.isLoaderBlockItem(item)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("chunkloader.use")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlockPlaced();
        Location loc = block.getLocation();
        String id = ChunkLoaderData.buildId(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        // Default radius is 0 (1x1)
        ChunkLoaderData data = new ChunkLoaderData(id, player.getUniqueId(), player.getName(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0, true);
        plugin.getDataManager().addLoader(data);

        // Force load chunks
        plugin.getChunkManager().loadChunks(loc.getWorld(), data.getChunkX(), data.getChunkZ(), data.getRadius());

        player.sendMessage(plugin.getMessage("loader-placed"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String id = ChunkLoaderData.buildId(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        ChunkLoaderData data = plugin.getDataManager().getLoader(id);
        if (data == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(data.getOwnerUUID()) && !player.hasPermission("chunkloader.admin")) {
            player.sendMessage(plugin.getMessage("not-owner").replace("{owner}", data.getOwnerName()));
            event.setCancelled(true);
            return;
        }

        // Unload chunks if active
        if (data.isActive()) {
            World world = loc.getWorld();
            if (world != null) {
                plugin.getChunkManager().unloadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
            }
        }

        plugin.getDataManager().removeLoader(id);
        event.setDropItems(false);
        loc.getWorld().dropItemNaturally(loc, plugin.getLoaderBlockItemStack());
        player.sendMessage(plugin.getMessage("loader-removed"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Handle dual hand interact events in 1.9+
        try {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
        } catch (Throwable ignored) {
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        String id = ChunkLoaderData.buildId(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        ChunkLoaderData data = plugin.getDataManager().getLoader(id);
        if (data == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        // Must click with activator item or administrator
        if (!plugin.isActivatorItem(inHand) && !player.hasPermission("chunkloader.admin")) {
            return;
        }

        event.setCancelled(true);

        if (!player.getUniqueId().equals(data.getOwnerUUID()) && !player.hasPermission("chunkloader.admin")) {
            player.sendMessage(plugin.getMessage("not-owner").replace("{owner}", data.getOwnerName()));
            return;
        }

        ChunkLoaderGUI.openGUI(player, data);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title == null || !title.startsWith(ChunkLoaderGUI.GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String subtitle = title.replace(ChunkLoaderGUI.GUI_TITLE + " #", "");
        ChunkLoaderData data = plugin.getDataManager().getLoader(subtitle);

        if (data == null) {
            player.closeInventory();
            return;
        }

        World loaderWorld = Bukkit.getWorld(data.getWorldName());
        if (loaderWorld == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 10) { // 1x1
            setRadiusIfPermitted(player, data, loaderWorld, 0, "1x1");
        } else if (slot == 12) { // 3x3
            setRadiusIfPermitted(player, data, loaderWorld, 1, "3x3");
        } else if (slot == 14) { // 5x5
            setRadiusIfPermitted(player, data, loaderWorld, 2, "5x5");
        } else if (slot == 16) { // 7x7
            setRadiusIfPermitted(player, data, loaderWorld, 3, "7x7");
        } else if (slot == 22) { // Toggle active status
            toggleActiveStatus(player, data, loaderWorld);
        } else if (slot == 26) { // Remove loader
            removeLoaderFromGUI(player, data, loaderWorld);
        }
    }

    private void setRadiusIfPermitted(Player player, ChunkLoaderData data, World world, int newRadius, String radiusStr) {
        String perm = "chunkloader.radius." + radiusStr;
        if (!player.hasPermission(perm) && !player.hasPermission("chunkloader.admin")) {
            player.sendMessage(plugin.getMessage("no-radius-permission").replace("{radius}", radiusStr));
            return;
        }

        if (data.getRadius() == newRadius) {
            return;
        }

        // Unload old radius chunks if active
        if (data.isActive()) {
            plugin.getChunkManager().unloadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
        }

        data.setRadius(newRadius);
        plugin.getDataManager().saveData();

        // Load new radius chunks if active
        if (data.isActive()) {
            plugin.getChunkManager().loadChunks(world, data.getChunkX(), data.getChunkZ(), newRadius);
        }

        player.sendMessage(plugin.getMessage("loader-activated").replace("{radius}", radiusStr));
        ChunkLoaderGUI.openGUI(player, data);
    }

    private void toggleActiveStatus(Player player, ChunkLoaderData data, World world) {
        boolean newState = !data.isActive();
        data.setActive(newState);
        plugin.getDataManager().saveData();

        if (newState) {
            plugin.getChunkManager().loadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
            player.sendMessage(plugin.getMessage("loader-activated").replace("{radius}", data.getRadiusString()));
        } else {
            plugin.getChunkManager().unloadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
            player.sendMessage(plugin.getMessage("loader-deactivated"));
        }

        ChunkLoaderGUI.openGUI(player, data);
    }

    private void removeLoaderFromGUI(Player player, ChunkLoaderData data, World world) {
        if (data.isActive()) {
            plugin.getChunkManager().unloadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
        }

        plugin.getDataManager().removeLoader(data.getId());
        Location loc = data.getLocation();
        if (loc != null && loc.getBlock() != null) {
            loc.getBlock().setType(org.bukkit.Material.AIR);
            world.dropItemNaturally(loc, plugin.getLoaderBlockItemStack());
        }

        player.closeInventory();
        player.sendMessage(plugin.getMessage("loader-removed"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        World world = event.getWorld();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();

        for (ChunkLoaderData data : plugin.getDataManager().getAllLoaders()) {
            if (data.isActive() && data.getWorldName().equals(world.getName())) {
                int r = data.getRadius();
                int cx = data.getChunkX();
                int cz = data.getChunkZ();

                if (chunkX >= cx - r && chunkX <= cx + r && chunkZ >= cz - r && chunkZ <= cz + r) {
                    // Reflector for Cancellable setCancelled on 1.12.2 ChunkUnloadEvent
                    try {
                        Method setCancelledMethod = event.getClass().getMethod("setCancelled", boolean.class);
                        setCancelledMethod.invoke(event, true);
                    } catch (Throwable ignored) {
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        for (ChunkLoaderData data : plugin.getDataManager().getAllLoaders()) {
            if (data.isActive() && data.getWorldName().equals(world.getName())) {
                plugin.getChunkManager().loadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
            }
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        for (ChunkLoaderData data : plugin.getDataManager().getAllLoaders()) {
            if (data.isActive() && data.getWorldName().equals(world.getName())) {
                plugin.getChunkManager().unloadChunks(world, data.getChunkX(), data.getChunkZ(), data.getRadius());
            }
        }
    }
}
