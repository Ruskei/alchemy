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

        proximityList.tick();

        time.addAndGet(1);
    }

    public void registerPlayer(Player player) {
        proximityList.registerPlayer(player);
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
