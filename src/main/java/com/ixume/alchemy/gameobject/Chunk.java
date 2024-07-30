package com.ixume.alchemy.gameobject;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.joml.Vector4d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Chunk {
    private final Level level;
    public Map<Vector4d, Pair<ArmorStand, Shulker>> colliders;
    public Map<Vector4d, Pair<ArmorStand, Shulker>> collidersToAdd;

    public Chunk(Level level) {
        this.level = level;
        colliders = new ConcurrentHashMap<>();
        collidersToAdd = new ConcurrentHashMap<>();
    }

    public Chunk (Level level, Vector4d v, int aID, int sID) {
        this.level = level;
        colliders = new HashMap<>();
        collidersToAdd = new HashMap<>();
        collidersToAdd.put(v, createStand(v, aID, sID));
    }

    public void update() {
        colliders.putAll(collidersToAdd);
        collidersToAdd.clear();
    }

    public void put(Vector4d v, int aID, int sID) {
        collidersToAdd.put(v, createStand(v, aID, sID));
    }

    private Pair<ArmorStand, Shulker> createStand(Vector4d v, int aID, int sID) {
        net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, v.x, v.y - 1.975, v.z);
        stand.setInvisible(true);
        stand.setId(aID);
        net.minecraft.world.entity.monster.Shulker shulker = new net.minecraft.world.entity.monster.Shulker(EntityType.SHULKER, level);
        shulker.setId(sID);
        shulker.setVariant(Optional.of(net.minecraft.world.item.DyeColor.RED));
        shulker.getAttribute(Attributes.SCALE).setBaseValue(v.w);
        stand.passengers = ImmutableList.of(shulker);
        return Pair.of(stand, shulker);
    }
}
