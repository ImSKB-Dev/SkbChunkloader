package com.chunkloader.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter {

    private static Boolean IS_LEGACY_VERSION = null;

    public static boolean isLegacy() {
        if (IS_LEGACY_VERSION == null) {
            try {
                Material.class.getMethod("getMaterial", String.class);
                // Check if Material.REDSTONE_BLOCK vs Material.matchMaterial exists
                Material.valueOf("REDSTONE_BLOCK");
                IS_LEGACY_VERSION = (Material.getMaterial("LEGACY_REDSTONE_BLOCK") != null);
            } catch (Throwable t) {
                IS_LEGACY_VERSION = false;
            }
        }
        return IS_LEGACY_VERSION;
    }

    public static Material parseMaterial(String name, String fallbackName) {
        if (name == null || name.trim().isEmpty()) {
            return getFallback(fallbackName);
        }
        name = name.toUpperCase().trim();
        try {
            Material mat = Material.matchMaterial(name);
            if (mat != null) return mat;
        } catch (Throwable ignored) {
        }

        try {
            Material mat = Material.valueOf(name);
            if (mat != null) return mat;
        } catch (Throwable ignored) {
        }

        return getFallback(fallbackName);
    }

    private static Material getFallback(String fallbackName) {
        try {
            Material mat = Material.matchMaterial(fallbackName);
            if (mat != null) return mat;
        } catch (Throwable ignored) {
        }
        try {
            return Material.valueOf(fallbackName);
        } catch (Throwable ignored) {
            return Material.values()[0];
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createItemStack(String materialName, int amount, short damage) {
        Material mat = parseMaterial(materialName, "REDSTONE_BLOCK");
        try {
            return new ItemStack(mat, amount, damage);
        } catch (Throwable t) {
            return new ItemStack(mat, amount);
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean matches(ItemStack item, String matName, short damage) {
        if (item == null) return false;
        Material targetMat = parseMaterial(matName, "REDSTONE_BLOCK");
        if (item.getType() != targetMat) {
            return false;
        }
        if (damage > 0) {
            try {
                return item.getDurability() == damage;
            } catch (Throwable ignored) {
            }
        }
        return true;
    }

    public static ItemStack setDisplayNameAndLore(ItemStack item, String displayName, List<String> lore) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
