package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class TriangleHitboxFragment implements HitboxFragment {
    private Location[] vertices;
    private Vector3d[] edges;
    private Vector3d normal;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.4F);

    public TriangleHitboxFragment(Location[] vertices, Alchemy plugin) {
        this.vertices = vertices;
        edges = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            edges[i] = vertices[(i + 1) % 3].toVector().toVector3d().sub(vertices[i].toVector().toVector3d());
        }

        normal = new Vector3d(edges[0]).cross(edges[1]).normalize();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
    }

    public List<Vector3d> intersect(HitboxFragment hitbox) {
        List<Vector3d> intersections = new ArrayList<>();
        Vector3d planePoint = hitbox.planePoint();
        for (int i = 0; i < edges.length; i++) {
            Vector3d edge = edges[i];
            Vector3d edgeUnitVector = new Vector3d(edge).normalize();
            Vector3d edgePoint = vertices[i].toVector().toVector3d();
            if (new Vector3d(edgeUnitVector).dot(hitbox.normal()) == 0) continue;

            Vector3d intersection = edgePoint.add(new Vector3d(edgeUnitVector).mul((new Vector3d(planePoint).sub(edgePoint)).dot(hitbox.normal())/(new Vector3d(edgeUnitVector).dot(hitbox.normal()))));
            if (hitbox.inside(intersection)) {
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    public void render() {
        World world = Bukkit.getServer().getWorld("world");
        double INTERVAL = 0.2;
        for (int i = 0; i < 3; i++) {
            Vector3d edge = edges[i];
            Vector3d normalizedEdge = new Vector3d(edge);
            normalizedEdge.normalize();
            for (double d = 0; d < edge.length(); d += INTERVAL) {
                Location particleLocation = vertices[i].clone();
                Vector3d fractionalEdge = new Vector3d(normalizedEdge).mul(d);
                particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
                world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
            }
        }

        for (double d = 0; d < 1; d += INTERVAL) {
            Location particleLocation = vertices[1].clone();
            Vector3d fractionalEdge = new Vector3d(normal).mul(d);
            particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
            world.spawnParticle(Particle.DUST, particleLocation, 1, normalDust);
        }
    }

    @Override
    public Vector3d planePoint() {
        return vertices[1].toVector().toVector3d();
    }

    @Override
    public Vector3d normal() {
        return normal;
    }

    @Override
    public boolean inside(Vector3d point) {
        Vector3d p = new Vector3d(point);
        Vector3d a = new Vector3d(vertices[0].toVector().toVector3d());
        Vector3d b = new Vector3d(vertices[1].toVector().toVector3d());
        Vector3d c = new Vector3d(vertices[2].toVector().toVector3d());

        a.sub(p);
        b.sub(p);
        c.sub(p);

        Vector3d u = new Vector3d(b).cross(c);
        Vector3d v = c.cross(a);
        Vector3d w = a.cross(b);

        if (v.dot(u) < 0.001f) return false;
        if (w.dot(u) < 0.001f) return false;

        return true;
    }
}
