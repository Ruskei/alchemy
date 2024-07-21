package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameObjectTicker {
    private Alchemy plugin;
    private volatile List<GameObject> objects;
    private volatile List<Location> intersections;

    private final Particle.DustOptions intersectionDust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 0.5F);;

    private static GameObjectTicker INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) {
            INSTANCE = new GameObjectTicker(plugin);
        }
    }

    public static GameObjectTicker getInstance() {return INSTANCE;}

    private GameObjectTicker(Alchemy plugin) {
        objects = new CopyOnWriteArrayList<>();
        intersections = new CopyOnWriteArrayList<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            GameObjectTicker.INSTANCE.objects.stream().filter(o -> o instanceof Hitbox).map(o -> ((Hitbox) o)).forEach(o -> o.getFragments().forEach(HitboxFragmentImpl::render));
            World world = Bukkit.getWorld("world");
            intersections = findIntersections().stream().toList();

            for (Location intersection : GameObjectTicker.INSTANCE.intersections) {
                world.spawnParticle(Particle.DUST, intersection, 5, intersectionDust);
            }
        }, 10, 1);
    }

    private Set<Location> findIntersections() {
        World world = Bukkit.getWorld("world");
        Set<Location> intersections = new HashSet<>();
        for (GameObject object : objects) {
            object.tick();
            if (object instanceof Hitbox hitbox) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType().equals(EntityType.BLOCK_DISPLAY)) continue;
                    intersections.addAll(hitbox.collide(entity).stream().map(v -> new Location(world, v.x, v.y, v.z)).toList());
                }

                for (GameObject object2 : objects) {
                    if (object == object2) continue;
                    if (object2 instanceof Hitbox hitbox2) {
                        intersections.addAll(hitbox.collide(hitbox2).stream().map(v -> new Location(world, v.x, v.y, v.z)).toList());
                    }
                }
            }
        }

        return intersections;
    }

    public void addHitbox(GameObject gameObject) {
        objects.add(gameObject);
    }
}
