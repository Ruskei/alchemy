package com.ixume.alchemy;

import org.bukkit.*;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitboxRenderer {
    private Alchemy plugin;
    private volatile List<TriangleHitbox> hitboxes;
    private volatile List<Location> intersections;

    private final Particle.DustOptions intersectionDust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0F);;

    private static HitboxRenderer INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) {
            INSTANCE = new HitboxRenderer(plugin);
        }
    }

    public static HitboxRenderer getInstance() {return INSTANCE;}

    private HitboxRenderer(Alchemy plugin) {
        hitboxes = new CopyOnWriteArrayList<>();
        intersections = new CopyOnWriteArrayList<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            HitboxRenderer.INSTANCE.hitboxes.forEach(TriangleHitbox::render);
            World world = Bukkit.getWorld("world");

            for (Location intersection : HitboxRenderer.INSTANCE.intersections) {
                world.spawnParticle(Particle.DUST, intersection, 1, intersectionDust);
            }
        }, 10, 1);
    }

    private Set<Location> findIntersections() {
        World world = Bukkit.getWorld("world");
        Set<Location> intersections = new HashSet<>();
        for (TriangleHitbox hitbox : hitboxes) {
            for (TriangleHitbox hitbox2 : hitboxes) {
                if (hitbox == hitbox2) continue;

                Vector3d planePoint = hitbox2.vertices[1].toVector().toVector3d();
                for (int i = 0; i < hitbox.edges.length; i++) {
                    Vector3d edge = hitbox.edges[i];
                    Vector3d edgeUnitVector = new Vector3d(edge).normalize();
                    Vector3d edgePoint = hitbox.vertices[i].toVector().toVector3d();
                    if (new Vector3d(edgeUnitVector).dot(hitbox2.normal) == 0) continue;

                    Vector3d intersection = edgePoint.add(new Vector3d(edgeUnitVector).mul((new Vector3d(planePoint).sub(edgePoint)).dot(hitbox2.normal)/(new Vector3d(edgeUnitVector).dot(hitbox2.normal))));
                    intersections.add(new Location(world, intersection.x, intersection.y, intersection.z));
                }
            }
        }

        return intersections;
    }

    public void addHitbox(TriangleHitbox hitbox) {
        hitboxes.add(hitbox);
        intersections = findIntersections().stream().toList();
    }
}
