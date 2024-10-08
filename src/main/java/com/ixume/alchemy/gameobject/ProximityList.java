package com.ixume.alchemy.gameobject;

import com.google.common.collect.ImmutableList;
import com.ixume.alchemy.EntityIDManager;
import com.ixume.alchemy.gameobject.virtualobjects.VirtualShulker;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityList {
    private final static double CHUNK_SIZE = 3d;
    private final static int CHUNK_VISIBILITY_DISTANCE = 1;
    public final Map<Vector3i, Chunk> chunkMap;
    public final Map<UUID, PlayerRegion> playerPositionsMap;
    private final List<Chunk> toUpdate;
    private final Level level;

    public ProximityList(World world) {
        this.level = ((CraftWorld) world).getHandle();
        chunkMap = new ConcurrentHashMap<>();
        toUpdate = new CopyOnWriteArrayList<>();
        playerPositionsMap = new ConcurrentHashMap<>();
    }

    public void tick() {
        updatePlayerPositions();
        toUpdate.forEach(Chunk::update);
        toUpdate.clear();
    }

    private void updatePlayerPositions() {
        for (Map.Entry<UUID, PlayerRegion> entry : playerPositionsMap.entrySet()) {
            handlePlayer(entry);
        }
    }

    private void handlePlayer(Map.Entry<UUID, PlayerRegion> entry) {
        Player player = Bukkit.getPlayer(entry.getKey());
        PlayerRegion region = entry.getValue();
        Vector3i mapVector = region.getKeyVector();
        assert player != null;
        Vector3i currentVector = getKeyFromRaw(player.getLocation().toVector().toVector3d());
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl connection = serverPlayer.connection;
        if (mapVector.equals(currentVector)) {
            //player hasn't moved from their chunk
            //update from those chunks
            List<Chunk> updatedChunks = getChunksInVolume(player.getLocation().toVector().toVector3d());
            Set<VirtualShulker> currentEntities = getShulkersFromChunks(updatedChunks);
            Set<VirtualShulker> entitiesToAdd = new HashSet<>(currentEntities);
            entitiesToAdd.removeAll(region.getShulkers());
            Set<VirtualShulker> entitiesToRemove = new HashSet<>(region.getShulkers());
            entitiesToRemove.removeAll(currentEntities);

            removeShulkers(entitiesToRemove, connection);
            sendShulkers(entitiesToAdd, connection);

            playerPositionsMap.get(entry.getKey()).setShulkers(currentEntities);
            playerPositionsMap.get(entry.getKey()).setChunks(updatedChunks);
            playerPositionsMap.get(entry.getKey()).setKeyVector(currentVector);
        } else {
            //remove entities from old chunks and add entities to new chunks
            Set<Chunk> oldChunks = new HashSet<>(region.getChunks());
            List<Chunk> newChunks = getChunksInVolume(player.getLocation().toVector().toVector3d());
            Set<VirtualShulker> currentEntities = getShulkersFromChunks(newChunks);
            List<Chunk> chunksToRemove = new ArrayList<>(oldChunks);
            chunksToRemove.removeAll(newChunks);
            List<Chunk> chunksToAdd = new ArrayList<>(newChunks);
            chunksToAdd.removeAll(oldChunks);

            Set<VirtualShulker> entitiesToRemove = new HashSet<>();

            for (Chunk chunkToRemove : chunksToRemove) {
                entitiesToRemove.addAll(chunkToRemove.colliders.values());
            }

            entitiesToRemove.removeAll(currentEntities);
            removeShulkers(entitiesToRemove, connection);

            Set<VirtualShulker> entitiesToAdd = new HashSet<>(getShulkersFromChunks(chunksToAdd));

            entitiesToAdd.removeAll(region.getShulkers());
            sendShulkers(entitiesToAdd, connection);

            playerPositionsMap.get(entry.getKey()).setShulkers(currentEntities);
            playerPositionsMap.get(entry.getKey()).setChunks(newChunks);
            playerPositionsMap.get(entry.getKey()).setKeyVector(currentVector);
        }
    }

    private List<Chunk> getChunksInVolume(Vector3d origin) {
        final List<Chunk> chunks = new ArrayList<>();
        final Vector3i originKey = getKeyFromRaw(origin);
        for (int x = originKey.x - CHUNK_VISIBILITY_DISTANCE; x <= originKey.x + CHUNK_VISIBILITY_DISTANCE; x++) {
            for (int y = originKey.y - CHUNK_VISIBILITY_DISTANCE; y <= originKey.y + CHUNK_VISIBILITY_DISTANCE; y++) {
                for (int z = originKey.z - CHUNK_VISIBILITY_DISTANCE; z <= originKey.z + CHUNK_VISIBILITY_DISTANCE; z++) {
                    Chunk chunk = chunkMap.get(new Vector3i(x, y, z));
                    if (chunk != null) chunks.add(chunk);
                }
            }
        }

        return chunks;
    }

    private Set<VirtualShulker> getShulkersFromChunks(List<Chunk> chunks) {
        Set<VirtualShulker> shulkers = new HashSet<>();
        for (Chunk chunk : chunks) {
            shulkers.addAll(chunk.colliders.values());
        }

        return shulkers;
    }

    private void sendShulkers(Collection<VirtualShulker> shulkers, ServerGamePacketListenerImpl connection) {
        shulkers.forEach(v -> connection.send(v.getPacket()));
    }

    private void removeShulkers(Collection<VirtualShulker> shulkers, ServerGamePacketListenerImpl connection) {
        int[] integers = new int[shulkers.size() * 2];
        int i = 0;
        for (VirtualShulker shulker : shulkers) {
            integers[i * 2] = shulker.getArmorstand();
            integers[i * 2 + 1] = shulker.getShulkerbox();
            i++;
        }

        connection.send(new ClientboundRemoveEntitiesPacket(integers));
    }

    public void registerPlayer(Player player) {
        playerPositionsMap.put(player.getUniqueId(), new PlayerRegion(getKeyFromRaw(player.getLocation().toVector().toVector3d())));
    }

    public void add(Vector4d toAdd) {
        final Vector3i key = getKeyFromRaw(new Vector3d(toAdd.x, toAdd.y, toAdd.z));

        final int aID = EntityIDManager.getInstance().getID();
        final int sID = EntityIDManager.getInstance().getID();
        VirtualShulker shulker = createShulker(toAdd, aID, sID);
        final double chunkSpace = Math.ceil(toAdd.w / CHUNK_SIZE) - 1;
        for (int x = (int) (key.x - chunkSpace); x <= (key.x + chunkSpace); x++) {
            for (int y = (int) (key.y - chunkSpace); y <= (key.y + chunkSpace); y++) {
                for (int z = (int) (key.z - chunkSpace); z <= (key.z + chunkSpace); z++) {
                    Vector3i key2 = new Vector3i(x, y, z);
                    chunkMap.merge(key2, new Chunk(toAdd, shulker), (a, b) -> {
                        a.put(toAdd, shulker);
                        return a;
                    });
                    toUpdate.add(chunkMap.get(key2));
                }
            }
        }
    }

    public void remove(Vector4d toRemove) {
        final Vector3i key = getKeyFromRaw(new Vector3d(toRemove.x, toRemove.y, toRemove.z));

        final double chunkSpace = Math.ceil(toRemove.w / CHUNK_SIZE) - 1;
        for (int x = (int) (key.x - chunkSpace); x <= (key.x + chunkSpace); x++) {
            for (int y = (int) (key.y - chunkSpace); y <= (key.y + chunkSpace); y++) {
                for (int z = (int) (key.z - chunkSpace); z <= (key.z + chunkSpace); z++) {
                    Vector3i key2 = new Vector3i(x, y, z);
                    if (chunkMap.containsKey(key2)) {
                        chunkMap.get(key2).collidersToRemove.add(toRemove);
                    }

                    toUpdate.add(chunkMap.get(key2));
                }
            }
        }
    }

    public void addAll(List<Vector4d> toAdds) {
        toAdds.forEach(this::add);
    }

    public Vector3i getKeyFromRaw(Vector3d pos) {
        return new Vector3i((int) Math.floor(pos.x / CHUNK_SIZE), (int) Math.floor(pos.y / CHUNK_SIZE), (int) Math.floor(pos.z / CHUNK_SIZE));
    }

    private VirtualShulker createShulker(Vector4d v, int aID, int sID) {
        net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, v.x, v.y - 1.975, v.z);
        stand.setInvisible(true);
        stand.setId(aID);
        net.minecraft.world.entity.monster.Shulker shulker = new net.minecraft.world.entity.monster.Shulker(EntityType.SHULKER, level);
        shulker.setId(sID);
        shulker.setVariant(Optional.of(net.minecraft.world.item.DyeColor.RED));
        shulker.setPos(v.x, v.y, v.z);
//        shulker.setInvisible(true);
        Objects.requireNonNull(shulker.getAttribute(Attributes.SCALE)).setBaseValue(v.w);
        stand.passengers = ImmutableList.of(shulker);

        Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        packets.add(stand.getAddEntityPacket());
        packets.add(shulker.getAddEntityPacket());
        packets.add(new ClientboundSetEntityDataPacket(stand.getId(), Objects.requireNonNull(stand.getEntityData().packAll())));
        packets.add(new ClientboundSetPassengersPacket(stand));
        packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), Objects.requireNonNull(shulker.getEntityData().packAll())));
        packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
        return new VirtualShulker(new ClientboundBundlePacket(packets), aID, sID);
    }
}
