package com.ixume.alchemy.gameobject;

import com.google.common.collect.ImmutableList;
import com.ixume.alchemy.EntityIDManager;
import com.ixume.alchemy.gameobject.virtualobjects.VirtualShulker;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityList {
    public final Map<Vector3d, Chunk> chunkMap;
    private final List<Chunk> toUpdate;
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
        VirtualShulker shulker = createShulker(toAdd, aID, sID);
        for (int x = (int) (key.x - 1); x <= key.x + 1; x++) {
            for (int y = (int) (key.y - 1); y <= key.y + 1; y++) {
                for (int z = (int) (key.z - 1); z <= key.z + 1; z++) {
                    Vector3d key2 = new Vector3d(x, y, z);
                    chunkMap.merge(key2, new Chunk(toAdd, shulker), (a, b) -> {
                        a.put(toAdd, shulker);
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
        return new Vector3d(Math.floor(pos.x / 2d), Math.floor(pos.y / 2d), Math.floor(pos.z / 2d));
    }

    private VirtualShulker createShulker(Vector4d v, int aID, int sID) {
        net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, v.x, v.y - 1.975, v.z);
        stand.setInvisible(true);
        stand.setId(aID);
        net.minecraft.world.entity.monster.Shulker shulker = new net.minecraft.world.entity.monster.Shulker(EntityType.SHULKER, level);
        shulker.setId(sID);
        shulker.setVariant(Optional.of(net.minecraft.world.item.DyeColor.RED));
//        shulker.setInvisible(true);
        Objects.requireNonNull(shulker.getAttribute(Attributes.SCALE)).setBaseValue(v.w);
        stand.passengers = ImmutableList.of(shulker);

        Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        packets.add(stand.getAddEntityPacket());
        packets.add(shulker.getAddEntityPacket());
        packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
        packets.add(new ClientboundSetPassengersPacket(stand));
        packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
        packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
        return new VirtualShulker(new ClientboundBundlePacket(packets), aID, sID);
    }
}
