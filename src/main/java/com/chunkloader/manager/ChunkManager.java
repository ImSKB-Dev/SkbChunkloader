package com.chunkloader.manager;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ChunkManager {

    private final Plugin plugin;

    private static Boolean HAS_FORCE_LOADED_API = null;
    private static Boolean HAS_PAPER_TICKETS = null;

    public ChunkManager(Plugin plugin) {
        this.plugin = plugin;
        initCapabilities();
    }

    private static void initCapabilities() {
        if (HAS_FORCE_LOADED_API == null) {
            try {
                World.class.getMethod("setChunkForceLoaded", int.class, int.class, boolean.class);
                HAS_FORCE_LOADED_API = true;
            } catch (NoSuchMethodException e) {
                HAS_FORCE_LOADED_API = false;
            }
        }

        if (HAS_PAPER_TICKETS == null) {
            try {
                World.class.getMethod("addPluginChunkTicket", int.class, int.class, Plugin.class);
                HAS_PAPER_TICKETS = true;
            } catch (NoSuchMethodException e) {
                HAS_PAPER_TICKETS = false;
            }
        }
    }

    /**
     * Loads chunks in radius around center Chunk.
     * Radius 1x1: radius = 0 (1 chunk)
     * Radius 3x3: radius = 1 (9 chunks)
     * Radius 5x5: radius = 2 (25 chunks)
     * Radius 7x7: radius = 3 (49 chunks)
     */
    public Set<ChunkCoord> loadChunks(World world, int centerChunkX, int centerChunkZ, int chunkRadius) {
        Set<ChunkCoord> loadedCoords = new HashSet<>();
        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                loadSingleChunk(world, x, z);
                loadedCoords.add(new ChunkCoord(world.getName(), x, z));
            }
        }
        return loadedCoords;
    }

    /**
     * Unloads chunks in radius around center Chunk.
     */
    public void unloadChunks(World world, int centerChunkX, int centerChunkZ, int chunkRadius) {
        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                unloadSingleChunk(world, x, z);
            }
        }
    }

    public void loadSingleChunk(World world, int x, int z) {
        if (world == null) return;

        if (Boolean.TRUE.equals(HAS_PAPER_TICKETS)) {
            try {
                Method addTicket = World.class.getMethod("addPluginChunkTicket", int.class, int.class, Plugin.class);
                addTicket.invoke(world, x, z, plugin);
                return;
            } catch (Throwable ignored) {
            }
        }

        if (Boolean.TRUE.equals(HAS_FORCE_LOADED_API)) {
            try {
                Method setForceLoaded = World.class.getMethod("setChunkForceLoaded", int.class, int.class, boolean.class);
                setForceLoaded.invoke(world, x, z, true);
                return;
            } catch (Throwable ignored) {
            }
        }

        // Legacy 1.12.2 Bukkit fallback
        if (!world.isChunkLoaded(x, z)) {
            world.loadChunk(x, z, true);
        }
    }

    public void unloadSingleChunk(World world, int x, int z) {
        if (world == null) return;

        if (Boolean.TRUE.equals(HAS_PAPER_TICKETS)) {
            try {
                Method removeTicket = World.class.getMethod("removePluginChunkTicket", int.class, int.class, Plugin.class);
                removeTicket.invoke(world, x, z, plugin);
                return;
            } catch (Throwable ignored) {
            }
        }

        if (Boolean.TRUE.equals(HAS_FORCE_LOADED_API)) {
            try {
                Method setForceLoaded = World.class.getMethod("setChunkForceLoaded", int.class, int.class, boolean.class);
                setForceLoaded.invoke(world, x, z, false);
                return;
            } catch (Throwable ignored) {
            }
        }

        // Legacy 1.12.2 Bukkit chunk unload
        if (world.isChunkLoaded(x, z)) {
            world.unloadChunkRequest(x, z);
        }
    }

    public static class ChunkCoord {
        private final String worldName;
        private final int x;
        private final int z;

        public ChunkCoord(String worldName, int x, int z) {
            this.worldName = worldName;
            this.x = x;
            this.z = z;
        }

        public String getWorldName() {
            return worldName;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkCoord)) return false;
            ChunkCoord that = (ChunkCoord) o;
            return x == that.x && z == that.z && worldName.equals(that.worldName);
        }

        @Override
        public int hashCode() {
            int result = worldName.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }
}
