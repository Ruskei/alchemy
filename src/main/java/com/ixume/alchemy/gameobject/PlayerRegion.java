package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.gameobject.virtualobjects.VirtualShulker;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerRegion {
    private List<Chunk> chunks;
    private Set<VirtualShulker> shulkers;
    private Vector3i keyVector;

    public PlayerRegion(Vector3i keyVector) {
        this.chunks = new ArrayList<>();
        this.shulkers = new HashSet<>();
        this.keyVector = keyVector;
    }

    public Vector3i getKeyVector() {
        return keyVector;
    }

    public void setKeyVector(Vector3i keyVector) {
        this.keyVector = keyVector;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setShulkers(Set<VirtualShulker> shulkers) {
        this.shulkers = shulkers;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public Set<VirtualShulker> getShulkers() {
        return shulkers;
    }
}
