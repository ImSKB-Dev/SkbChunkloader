package com.chunkloader;

import com.chunkloader.manager.ChunkManager;
import com.chunkloader.model.ChunkLoaderData;
import com.chunkloader.util.MaterialAdapter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkLoaderTest {

    @Test
    public void testChunkLoaderData() {
        UUID owner = UUID.randomUUID();
        ChunkLoaderData data = new ChunkLoaderData("world;100;64;200", owner, "TestPlayer", "world", 100, 64, 200, 0, true);

        assertEquals("world;100;64;200", data.getId());
        assertEquals(owner, data.getOwnerUUID());
        assertEquals("TestPlayer", data.getOwnerName());
        assertEquals("world", data.getWorldName());
        assertEquals(100, data.getX());
        assertEquals(64, data.getY());
        assertEquals(200, data.getZ());
        assertEquals(6, data.getChunkX());
        assertEquals(12, data.getChunkZ());
        assertEquals(0, data.getRadius());
        assertEquals("1x1", data.getRadiusString());
        assertTrue(data.isActive());

        data.setRadius(2);
        assertEquals("5x5", data.getRadiusString());

        data.setActive(false);
        assertFalse(data.isActive());
    }

    @Test
    public void testChunkCoord() {
        ChunkManager.ChunkCoord c1 = new ChunkManager.ChunkCoord("world", 10, 20);
        ChunkManager.ChunkCoord c2 = new ChunkManager.ChunkCoord("world", 10, 20);
        ChunkManager.ChunkCoord c3 = new ChunkManager.ChunkCoord("world_nether", 10, 20);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testMaterialAdapterParsing() {
        assertNotNull(MaterialAdapter.parseMaterial("REDSTONE_BLOCK", "STONE"));
        assertNotNull(MaterialAdapter.parseMaterial("NON_EXISTENT_MAT", "STONE"));
    }
}
