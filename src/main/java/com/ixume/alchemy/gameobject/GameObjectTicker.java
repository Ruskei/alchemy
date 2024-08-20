package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.virtualobjects.VirtualShulker;
import com.ixume.alchemy.hitbox.Hitbox;
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
import java.util.concurrent.atomic.AtomicInteger;

public class GameObjectTicker {
    private final AtomicInteger time;
    private final List<GameObject> objects;
    private final List<GameObject> objectsToAdd;
    private final List<GameObject> objectsToRemove;
    public final ProximityList proximityList;
    private final World world;

    public GameObjectTicker(Alchemy plugin, World world) {
        this.world = world;
        time = new AtomicInteger(0);
        objects = new ArrayList<>();
        objectsToAdd = new ArrayList<>();
        objectsToRemove = new ArrayList<>();
        proximityList = new ProximityList(world);
//            GameObjectTicker.INSTANCE.objects.stream().filter(o -> o instanceof Hitbox).map(o -> ((Hitbox) o)).forEach(o -> o.getFragments().forEach(HitboxFragmentImpl::render));
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 10, 1);
    }

    private void tick() {
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

        this.objects.removeAll(this.objectsToRemove);
        this.objectsToRemove.clear();
        this.objects.addAll(this.objectsToAdd);
        this.objectsToAdd.clear();

//        if (time.intValue() % 2 == 0) {
//            for (Player p : world.getPlayers()) {
//                ServerPlayer serverPlayer = ((CraftPlayer) p).getHandle();
//                Vector3d playerVector = proximityList.getKeyFromRaw(p.getLocation().toVector().toVector3d());
//                if (proximityList.chunkMap.containsKey(playerVector)) {
//                    Chunk playerChunk = proximityList.chunkMap.get(playerVector);
//
//                    if (proximityList.playerChunkMap.containsKey(p.getEntityId())) {
//                        Chunk outdatedChunk = this.proximityList.playerChunkMap.get(p.getEntityId());
//                        if (playerChunk.equals(proximityList.playerChunkMap.get(p.getEntityId()))) {
//                            //player hasn't moved
//                            sendStands(playerChunk.collidersToAdd, serverPlayer.connection);
//                        } else {
//                            System.out.println("realized -> realized");
//                            //player has moved chunks
//                            //playerChunk is current, the one in the list is outdated
//                            //delete all the previous entities
////                            if (this..proximityList.playerChunkMap.containsKey(p.getEntityId())) {
//                            List<Pair<Integer, Integer>> toRemove = new ArrayList<>();
//                            for (Map.Entry<Vector4d, VirtualShulker> entry : outdatedChunk.colliders.entrySet()) {
//                                if (!playerChunk.colliders.containsKey(entry.getKey())) {
//                                    toRemove.add(Pair.of(entry.getValue().getArmorstand(), entry.getValue().getShulkerbox()));
//                                }
//                            }
//
//                            System.out.println("toRemove.size: " + toRemove.size());
//
//                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(toRemove.stream().mapToInt(Pair::left).toArray()));
//                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(toRemove.stream().mapToInt(Pair::right).toArray()));
////                            }
//
//                            //send new ones
//                            List<VirtualShulker> toUpdate = new ArrayList<>();
//                            for (Map.Entry<Vector4d, VirtualShulker> entry : playerChunk.colliders.entrySet()) {
//                                if (!outdatedChunk.colliders.containsKey(entry.getKey())) {
//                                    toUpdate.add(entry.getValue());
//                                }
//                            }
//
//                            System.out.println("toUpdate.size: " + toUpdate.size());
//
//                            sendStands(toUpdate, serverPlayer.connection);
//
//                            //update
//                            proximityList.playerChunkMap.replace(p.getEntityId(), playerChunk);
//                        }
//                    } else {
//                        //moved from null chunk to realized chunk
//                        System.out.println("null -> realized");
//                        sendStands(playerChunk.colliders, serverPlayer.connection);
//
//                        proximityList.playerChunkMap.put(p.getEntityId(), playerChunk);
//                    }
//                } else if (proximityList.playerChunkMap.containsKey(p.getEntityId())) {
//                    System.out.println("realized -> null");
//                    Chunk outdatedChunk = this.proximityList.playerChunkMap.get(p.getEntityId());
//                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(outdatedChunk.colliders.values().stream().mapToInt(VirtualShulker::getArmorstand).toArray()));
//                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(outdatedChunk.colliders.values().stream().mapToInt(VirtualShulker::getShulkerbox).toArray()));
//                    proximityList.playerChunkMap.remove(p.getEntityId());
//                }
//            }
//        }

        proximityList.tick();

        time.addAndGet(1);
    }

    public void registerPlayer(Player player) {
        proximityList.registerPlayer(player);
    }

    private void sendStands(Map<Vector4d, VirtualShulker> map, ServerGamePacketListenerImpl connection) {
        for (Map.Entry<Vector4d, VirtualShulker> entry : map.entrySet()) {
            connection.send(entry.getValue().getPacket());
        }
    }

    private void sendStands(List<VirtualShulker> list, ServerGamePacketListenerImpl connection) {
        for (VirtualShulker entry : list) {
            connection.send(entry.getPacket());
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
