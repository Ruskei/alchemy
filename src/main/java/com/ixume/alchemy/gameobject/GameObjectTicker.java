package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
                Chunk playerChunk = proximityList.get(p.getLocation().toVector().toVector3d());
                if (playerChunk != null) {
                    if (playerChunk.equals(proximityList.playerChunkMap.get(p.getEntityId()))) {
                        //player hasn't moved
                        for (Map.Entry<Vector4d, Pair<ArmorStand, Shulker>> entry : playerChunk.collidersToAdd.entrySet()) {
                            ArmorStand stand = entry.getValue().left();
                            Shulker shulker = entry.getValue().right();

                            Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
                            packets.add(stand.getAddEntityPacket());
                            packets.add(shulker.getAddEntityPacket());
                            packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
                            packets.add(new ClientboundSetPassengersPacket(stand));
                            packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
                            packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
                            serverPlayer.connection.send(new ClientboundBundlePacket(packets));
                        }
                    } else {
                        System.out.println("moved chunks");
                        //player has moved chunks
                        //playerChunk is current, the one in the list is outdated
                        //delete all the previous entities
                        if (GameObjectTicker.getInstance().proximityList.playerChunkMap.containsKey(p.getEntityId()) && GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId()) != null) {
                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId()).colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.left().getId()).toArray()));
                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId()).colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.right().getId()).toArray()));
                        }

                        //send new ones
                        for (Map.Entry<Vector4d, Pair<ArmorStand, Shulker>> entry : playerChunk.colliders.entrySet()) {
                            ArmorStand stand = entry.getValue().left();
                            Shulker shulker = entry.getValue().right();

                            Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
                            packets.add(stand.getAddEntityPacket());
                            packets.add(shulker.getAddEntityPacket());
                            packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
                            packets.add(new ClientboundSetPassengersPacket(stand));
                            packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
                            packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
                            serverPlayer.connection.send(new ClientboundBundlePacket(packets));
                        }

                        //update
                        proximityList.playerChunkMap.replace(p.getEntityId(), playerChunk);
                    }
                } else if (proximityList.playerChunkMap.containsKey(p.getEntityId()) && proximityList.playerChunkMap.get(p.getEntityId()) != null) {
                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId()).colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.left().getId()).toArray()));
                    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(p.getEntityId()).colliders.values().stream().mapToInt(armorStandShulkerPair -> armorStandShulkerPair.right().getId()).toArray()));
                }
//                List<ArmorStand> stands = GameObjectTicker.getInstance().proximityList.get(p.getLocation().toVector().toVector3d());
            }
        }

        proximityList.tick();

        time++;
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
