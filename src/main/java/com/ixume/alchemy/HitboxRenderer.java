package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitboxRenderer {
    private Alchemy plugin;
    private volatile List<GameObject> objects;
    private volatile List<Location> intersections;

    private final Particle.DustOptions intersectionDust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 0.5F);;

    private static HitboxRenderer INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) {
            INSTANCE = new HitboxRenderer(plugin);
        }
    }

    public static HitboxRenderer getInstance() {return INSTANCE;}

    private HitboxRenderer(Alchemy plugin) {
        objects = new CopyOnWriteArrayList<>();
        intersections = new CopyOnWriteArrayList<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            HitboxRenderer.INSTANCE.objects.forEach(o -> o.getHitboxes().forEach(Hitbox::render));
            World world = Bukkit.getWorld("world");
            intersections = findIntersections().stream().toList();

            for (Location intersection : HitboxRenderer.INSTANCE.intersections) {
                world.spawnParticle(Particle.DUST, intersection, 5, intersectionDust);
            }
        }, 10, 1);
    }

    private Set<Location> findIntersections() {
        World world = Bukkit.getWorld("world");
        Set<Location> intersections = new HashSet<>();
        for (GameObject object : objects) {
            if (object instanceof BoundingBoxGameObject g) {
                g.update();
            }

            for (GameObject object2 : objects) {
                if (object == object2) continue;
                for (Hitbox hitbox : object.getHitboxes()) {
                    for (Hitbox hitbox2 : object2.getHitboxes()) {

                        intersections.addAll(hitbox.intersect(hitbox2).stream()
                                .map((v) -> new Location(world, v.x, v.y, v.z))
                                .toList());
                    }
                }
            }

            for (Entity entity : world.getEntities()) {
                if (object instanceof TriangleTestGameObject triObject) {
                    intersections.addAll(((TriangleHitbox) triObject.getHitboxes().get(0)).intersectEntity(entity).stream().map(v -> new Location(world, v.x, v.y, v.z)).toList());
                }
            }
        }

        return intersections;
    }

    public void addHitbox(GameObject gameObject) {
        objects.add(gameObject);
    }
}
