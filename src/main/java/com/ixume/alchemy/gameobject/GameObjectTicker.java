package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.*;

public class GameObjectTicker {
    private volatile int time;
    private volatile List<GameObject> objects;
    private volatile List<GameObject> objectsToAdd;
    private volatile List<GameObject> objectsToRemove;
    public volatile ProximityList proximityList;

    private static GameObjectTicker INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) {
            INSTANCE = new GameObjectTicker(plugin);
        }
    }

    public static GameObjectTicker getInstance() {return INSTANCE;}

    private GameObjectTicker(Alchemy plugin) {
        time = 0;
        objects = new ArrayList<>();
        objectsToAdd = new ArrayList<>();
        objectsToRemove = new ArrayList<>();
        proximityList = new ProximityList(Bukkit.getWorld("world"));
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            GameObjectTicker.INSTANCE.objects.stream().filter(o -> o instanceof Hitbox).map(o -> ((Hitbox) o)).forEach(o -> o.getFragments().forEach(HitboxFragmentImpl::render));
            tick();
        }, 10, 1);
    }

    private void tick() {
        World world = Bukkit.getWorld("world");
        for (GameObject object : objects) {
            object.tick();
            if (object instanceof Hitbox hitbox) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType().equals(EntityType.BLOCK_DISPLAY)) continue;
                    hitbox.collide(entity);
                }

                for (GameObject object2 : objects) {
                    if (object == object2) continue;
                    if (object2 instanceof Hitbox hitbox2) {
                        hitbox.collide(hitbox2);
                    }
                }
            }
        }

        GameObjectTicker.getInstance().objects.removeAll(GameObjectTicker.getInstance().objectsToRemove);
        GameObjectTicker.getInstance().objectsToRemove.clear();
        GameObjectTicker.getInstance().objects.addAll(GameObjectTicker.getInstance().objectsToAdd);
        GameObjectTicker.getInstance().objectsToAdd.clear();

        if (time % 2 == 0) {
            for (Player p : world.getPlayers()) {
                ServerPlayer serverPlayer = ((CraftPlayer) p).getHandle();
                Vector3d playerVector = proximityList.getKeyFromRaw(p.getLocation().toVector().toVector3d());
                if (proximityList.chunkMap.containsKey(playerVector)) {
                    Chunk playerChunk = proximityList.chunkMap.get(playerVector);

                    if (proximityList.playerChunkMap.containsKey(p.getEntityId())) {
                        Chunk outdatedChunk = GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId());
                        if (playerChunk.equals(proximityList.playerChunkMap.get(p.getEntityId()))) {
                            //player hasn't moved
                            sendStands(playerChunk.collidersToAdd, serverPlayer.connection);
                        } else {
                            System.out.println("realized -> realized");
                            //player has moved chunks
                            //playerChunk is current, the one in the list is outdated
                            //delete all the previous entities
//                            if (GameObjectTicker.getInstance().proximityList.playerChunkMap.containsKey(p.getEntityId())) {
                            List<Pair<Integer, Integer>> toRemove = new ArrayList<>();
                            for (Map.Entry<Vector4d, Pair<ArmorStand, Shulker>> entry : outdatedChunk.colliders.entrySet()) {
                                if (!playerChunk.colliders.containsKey(entry.getKey())) {
                                    toRemove.add(Pair.of(entry.getValue().left().getId(), entry.getValue().right().getId()));
                                }
                            }

                            System.out.println("toRemove.size: " + toRemove.size());

                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(toRemove.stream().mapToInt(Pair::left).toArray()));
                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(toRemove.stream().mapToInt(Pair::right).toArray()));
//                            }

                            //send new ones
                            List<Pair<ArmorStand, Shulker>> toUpdate = new ArrayList<>();
                            for (Map.Entry<Vector4d, Pair<ArmorStand, Shulker>> entry : playerChunk.colliders.entrySet()) {
                                if (!outdatedChunk.colliders.containsKey(entry.getKey())) {
                                    toUpdate.add(entry.getValue());
                                }
                            }

                            System.out.println("toUpdate.size: " + toUpdate.size());

                            sendStands(toUpdate, serverPlayer.connection);

                            //update
                            proximityList.playerChunkMap.replace(p.getEntityId(), playerChunk);
                        }
                    } else {
                        //moved from null chunk to realized chunk
                        System.out.println("null -> realized");
                        sendStands(playerChunk.colliders, serverPlayer.connection);

                        proximityList.playerChunkMap.put(p.getEntityId(), playerChunk);
                    }
                } else if (proximityList.playerChunkMap.containsKey(p.getEntityId())) {
                    System.out.println("realized -> null");
                    Chunk outdatedChunk = GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId());
                    System.out.println("toRemove: " + Arrays.toString(outdatedChunk.colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.right().getId()).toArray()));
                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(outdatedChunk.colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.left().getId()).toArray()));
                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(outdatedChunk.colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.right().getId()).toArray()));
                    proximityList.playerChunkMap.remove(p.getEntityId());
                }
            }
        }

        proximityList.tick();

        time++;
    }

    private void sendStands(Map<Vector4d, Pair<ArmorStand, Shulker>> map, ServerGamePacketListenerImpl connection) {
        for (Map.Entry<Vector4d, Pair<ArmorStand, Shulker>> entry : map.entrySet()) {
            ArmorStand stand = entry.getValue().left();
            Shulker shulker = entry.getValue().right();

            Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(stand.getAddEntityPacket());
            packets.add(shulker.getAddEntityPacket());
            packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
            packets.add(new ClientboundSetPassengersPacket(stand));
            packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
            packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
            connection.send(new ClientboundBundlePacket(packets));
        }
    }

    private void sendStands(List<Pair<ArmorStand, Shulker>> list, ServerGamePacketListenerImpl connection) {
        for (Pair<ArmorStand, Shulker> entry : list) {
            ArmorStand stand = entry.left();
            Shulker shulker = entry.right();

            Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(stand.getAddEntityPacket());
            packets.add(shulker.getAddEntityPacket());
            packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
            packets.add(new ClientboundSetPassengersPacket(stand));
            packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
            packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
            connection.send(new ClientboundBundlePacket(packets));
        }
    }

    public void addObject(GameObject gameObject) {
        objectsToAdd.add(gameObject);
    }

    public void removeObject(GameObject gameObject) {
        objectsToRemove.add(gameObject);
    }

    public ProximityList getProximityList() {
        return proximityList;
    }
}
