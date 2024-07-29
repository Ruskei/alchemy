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

public class Chunk {
    private final Level level;
    public Map<Vector4d, Pair<ArmorStand, Shulker>> colliders;
    public Map<Vector4d, Pair<ArmorStand, Shulker>> collidersToAdd;

    public Chunk(Level level) {
        this.level = level;
        colliders = new HashMap<>();
        collidersToAdd = new HashMap<>();
    }

    public Chunk (Level level, Vector4d... vectors) {
        this.level = level;
        colliders = new HashMap<>();
        collidersToAdd = new HashMap<>();
        Arrays.stream(vectors).forEach(v -> collidersToAdd.put(v, createStand(v)));
    }

    public void update() {
        colliders.putAll(collidersToAdd);
        collidersToAdd.clear();
    }

    public void put(Vector4d v) {
        collidersToAdd.put(v, createStand(v));
    }

    private Pair<ArmorStand, Shulker> createStand(Vector4d v) {
        net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, v.x, v.y - 1.975, v.z);
        stand.setInvisible(true);
        net.minecraft.world.entity.monster.Shulker shulker = new net.minecraft.world.entity.monster.Shulker(EntityType.SHULKER, level);
        shulker.setVariant(Optional.of(net.minecraft.world.item.DyeColor.RED));
        shulker.getAttribute(Attributes.SCALE).setBaseValue(v.w);
        stand.passengers = ImmutableList.of(shulker);
        return Pair.of(stand, shulker);
//        Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
//        packets.add(stand.getAddEntityPacket());
//        packets.add(shulker.getAddEntityPacket());
//        packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
//        packets.add(new ClientboundSetPassengersPacket(stand));
//        packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
//        packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
//        connection.send(new ClientboundBundlePacket(packets));
    }
}
