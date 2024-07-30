package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.EntityIDManager;
import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityList {
    public final Map<Vector3d, Chunk> chunkMap;
    private List<Chunk> toUpdate;
    public final Map<Integer, Chunk> playerChunkMap;
    private final Level level;

    public ProximityList(World world) {
        this.level = ((CraftWorld) world).getHandle();
        chunkMap = new HashMap<>();
        toUpdate = new CopyOnWriteArrayList<>();
        playerChunkMap = new HashMap<>();
    }

    public void tick() {
        toUpdate.forEach(Chunk::update);
        toUpdate.clear();
    }

    public void add(Vector4d toAdd) {
        Vector3d key = getKeyFromRaw(new Vector3d(toAdd.x, toAdd.y, toAdd.z));

        int aID = EntityIDManager.getInstance().getID();
        int sID = EntityIDManager.getInstance().getID();
        for (int x = (int) (key.x - 1); x <= key.x + 1; x++) {
            for (int y = (int) (key.y - 1); y <= key.y + 1; y++) {
                for (int z = (int) (key.z - 1); z <= key.z + 1; z++) {
                    Vector3d key2 = new Vector3d(x, y, z);
                    chunkMap.merge(key2, new Chunk(level, toAdd, aID, sID), (a, b) -> {
                        a.put(toAdd, aID, sID);
                        return a;
                    });
                    toUpdate.add(chunkMap.get(key2));
                }
            }
        }
    }

    public void addAll(List<Vector4d> toAdds) {
        toAdds.forEach(this::add);
    }

    public Vector3d getKeyFromRaw(Vector3d pos) {
        return new Vector3d(Math.floor(pos.x / 3d), Math.floor(pos.y / 3d), Math.floor(pos.z / 3d));
    }
}
