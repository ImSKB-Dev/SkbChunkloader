package com.chunkloader.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class PlatformScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia = false;
        try {
            Class<?> regionizedClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            if (regionizedClass != null) {
                folia = true;
            }
        } catch (Throwable t) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runTask(Plugin plugin, Location location, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                if (location != null) {
                    Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                    Method execute = regionScheduler.getClass().getMethod("execute", Plugin.class, Location.class, Runnable.class);
                    execute.invoke(regionScheduler, plugin, location, runnable);
                } else {
                    Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                    Method execute = globalScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
                    execute.invoke(globalScheduler, plugin, runnable);
                }
                return;
            } catch (Throwable t) {
                // Fallback
            }
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runTaskLater(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            try {
                if (location != null) {
                    Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                    Method runDelayed = regionScheduler.getClass().getMethod("runDelayed", Plugin.class, Location.class, java.util.function.Consumer.class, long.class);
                    java.util.function.Consumer<Object> consumer = task -> runnable.run();
                    runDelayed.invoke(regionScheduler, plugin, location, consumer, Math.max(1L, delayTicks));
                } else {
                    Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                    Method runDelayed = globalScheduler.getClass().getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class);
                    java.util.function.Consumer<Object> consumer = task -> runnable.run();
                    runDelayed.invoke(globalScheduler, plugin, consumer, Math.max(1L, delayTicks));
                }
                return;
            } catch (Throwable t) {
                // Fallback
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public static void runTaskAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runNow = asyncScheduler.getClass().getMethod("runNow", Plugin.class, java.util.function.Consumer.class);
                java.util.function.Consumer<Object> consumer = task -> runnable.run();
                runNow.invoke(asyncScheduler, plugin, consumer);
                return;
            } catch (Throwable t) {
                // Fallback
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
