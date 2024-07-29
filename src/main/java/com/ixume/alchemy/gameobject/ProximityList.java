package com.ixume.alchemy.gameobject;

import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.*;

public class ProximityList {
    private final Map<Vector3d, Chunk> chunkMap;
    private List<Chunk> toUpdate;
    public final Map<Integer, Chunk> playerChunkMap;
    private final Level level;

    public ProximityList(World world) {
        this.level = ((CraftWorld) world).getHandle();
        chunkMap = new HashMap<>();
        toUpdate = new ArrayList<>();
        playerChunkMap = new HashMap<>();
    }

    public void tick() {
        toUpdate.forEach(Chunk::update);
        toUpdate.clear();
    }

    public void add(Vector4d toAdd) {
        Vector3d key = new Vector3d(Math.floor(toAdd.x / 4d), Math.floor(toAdd.y / 4d), Math.floor(toAdd.z / 4d));

        chunkMap.merge(key, new Chunk(level, toAdd), (a, b) -> {
            a.put(toAdd);
            return a;
        });

        toUpdate.add(chunkMap.get(key));
    }

    public void addAll(List<Vector4d> toAdds) {
        toAdds.forEach(this::add);
    }

    public Chunk get(Vector3d pos) {
        Vector3d key = new Vector3d(Math.floor(pos.x / 4d), Math.floor(pos.y / 4d), Math.floor(pos.z / 4d));
        return chunkMap.getOrDefault(key, null);
    }
}
