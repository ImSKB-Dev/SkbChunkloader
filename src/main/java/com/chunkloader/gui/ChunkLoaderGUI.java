package com.chunkloader.gui;

import com.chunkloader.ChunkLoaderPlugin;
import com.chunkloader.model.ChunkLoaderData;
import com.chunkloader.util.MaterialAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ChunkLoaderGUI {

    public static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8ChunkLoader - Configurações");

    public static void openGUI(Player player, ChunkLoaderData loader) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE + " #" + loader.getId());

        // Slot 10: Radius 1x1 (Radius = 0)
        boolean has1x1 = player.hasPermission("chunkloader.radius.1x1") || player.hasPermission("chunkloader.admin");
        boolean is1x1Selected = loader.getRadius() == 0;
        ItemStack item1x1 = createRadiusItem("1x1", 0, is1x1Selected, has1x1);
        gui.setItem(10, item1x1);

        // Slot 12: Radius 3x3 (Radius = 1)
        boolean has3x3 = player.hasPermission("chunkloader.radius.3x3") || player.hasPermission("chunkloader.admin");
        boolean is3x3Selected = loader.getRadius() == 1;
        ItemStack item3x3 = createRadiusItem("3x3", 1, is3x3Selected, has3x3);
        gui.setItem(12, item3x3);

        // Slot 14: Radius 5x5 (Radius = 2)
        boolean has5x5 = player.hasPermission("chunkloader.radius.5x5") || player.hasPermission("chunkloader.admin");
        boolean is5x5Selected = loader.getRadius() == 2;
        ItemStack item5x5 = createRadiusItem("5x5", 2, is5x5Selected, has5x5);
        gui.setItem(14, item5x5);

        // Slot 16: Radius 7x7 (Radius = 3)
        boolean has7x7 = player.hasPermission("chunkloader.radius.7x7") || player.hasPermission("chunkloader.admin");
        boolean is7x7Selected = loader.getRadius() == 3;
        ItemStack item7x7 = createRadiusItem("7x7", 3, is7x7Selected, has7x7);
        gui.setItem(16, item7x7);

        // Slot 22: Toggle Active Status (Status Item)
        ItemStack statusItem;
        if (loader.isActive()) {
            statusItem = MaterialAdapter.isLegacy()
                    ? MaterialAdapter.createItemStack("WOOL", 1, (short) 5)
                    : MaterialAdapter.createItemStack("LIME_WOOL", 1, (short) 0);

            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "Status atual: " + ChatColor.GREEN + "ATIVADO",
                    ChatColor.YELLOW + "Clique para desativar este ChunkLoader."
            );
            statusItem = MaterialAdapter.setDisplayNameAndLore(statusItem, ChatColor.GREEN + "" + ChatColor.BOLD + "ChunkLoader Ativo", lore);
        } else {
            statusItem = MaterialAdapter.isLegacy()
                    ? MaterialAdapter.createItemStack("WOOL", 1, (short) 14)
                    : MaterialAdapter.createItemStack("RED_WOOL", 1, (short) 0);

            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "Status atual: " + ChatColor.RED + "DESATIVADO",
                    ChatColor.YELLOW + "Clique para ativar este ChunkLoader."
            );
            statusItem = MaterialAdapter.setDisplayNameAndLore(statusItem, ChatColor.RED + "" + ChatColor.BOLD + "ChunkLoader Desativado", lore);
        }
        gui.setItem(22, statusItem);

        // Slot 26: Remove / Destroy Loader
        ItemStack removeItem = MaterialAdapter.createItemStack("BARRIER", 1, (short) 0);
        List<String> removeLore = Arrays.asList(
                ChatColor.GRAY + "Clique para remover este ChunkLoader",
                ChatColor.GRAY + "e recuperar o bloco."
        );
        removeItem = MaterialAdapter.setDisplayNameAndLore(removeItem, ChatColor.RED + "" + ChatColor.BOLD + "Remover ChunkLoader", removeLore);
        gui.setItem(26, removeItem);

        player.openInventory(gui);
    }

    private static ItemStack createRadiusItem(String size, int radius, boolean selected, boolean hasPerm) {
        String matName;
        short damage = 0;

        if (selected) {
            matName = "EMERALD_BLOCK";
        } else if (!hasPerm) {
            matName = "BARRIER";
        } else {
            matName = MaterialAdapter.isLegacy() ? "DAYLIGHT_DETECTOR" : "DAYLIGHT_DETECTOR";
        }

        ItemStack item = MaterialAdapter.createItemStack(matName, 1, damage);
        String name = (selected ? ChatColor.GREEN : (hasPerm ? ChatColor.YELLOW : ChatColor.RED)) + "Área " + size;
        List<String> lore;
        if (selected) {
            lore = Arrays.asList(ChatColor.GREEN + "✔ Selecionado atualmente", ChatColor.GRAY + "Carrega " + size + " chunks.");
        } else if (hasPerm) {
            lore = Arrays.asList(ChatColor.YELLOW + "Clique para selecionar a área " + size + ".", ChatColor.GRAY + "Carrega " + size + " chunks.");
        } else {
            lore = Arrays.asList(ChatColor.RED + "✖ Sem permissão (chunkloader.radius." + size + ")", ChatColor.GRAY + "Requer permissão de administrador ou VIP.");
        }

        return MaterialAdapter.setDisplayNameAndLore(item, name, lore);
    }
}
