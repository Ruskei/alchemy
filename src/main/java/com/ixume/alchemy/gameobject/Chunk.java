package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.gameobject.virtualobjects.VirtualShulker;
import org.joml.Vector4d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Chunk {
    public final Map<Vector4d, VirtualShulker> colliders;
    public final Map<Vector4d, VirtualShulker> collidersToAdd;
    public final List<Vector4d> collidersToRemove;

    public Chunk (Vector4d v, VirtualShulker s) {
        colliders = new ConcurrentHashMap<>();
        collidersToAdd = new ConcurrentHashMap<>();
        collidersToRemove = new CopyOnWriteArrayList<>();
        collidersToAdd.put(v, s);
    }

    public void update() {
        colliders.putAll(collidersToAdd);
        collidersToAdd.clear();
        collidersToRemove.forEach(colliders::remove);
        collidersToRemove.clear();
    }

    public void put(Vector4d v, VirtualShulker s) {
        collidersToAdd.put(v, s);
    }
}
