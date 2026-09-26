package com.chunkloader.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class ChunkLoaderData {

    private final String id;
    private final UUID ownerUUID;
    private final String ownerName;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private int radius; // 0 = 1x1, 1 = 3x3, 2 = 5x5, 3 = 7x7
    private boolean active;

    public ChunkLoaderData(String id, UUID ownerUUID, String ownerName, String worldName, int x, int y, int z, int radius, boolean active) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.active = active;
    }

    public static String buildId(String world, int x, int y, int z) {
        return world + ";" + x + ";" + y + ";" + z;
    }

    public String getId() {
        return id;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public String getRadiusString() {
        int side = (radius * 2) + 1;
        return side + "x" + side;
    }
}
