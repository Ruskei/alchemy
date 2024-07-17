package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.List;

//defined with 3 points, 4th is filled in
public class ParallelogramHitbox implements Hitbox {
    private Location[] vertices;
    private Vector3d[] edges;
    private Vector3d normal;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.4F);

    public ParallelogramHitbox(Location[] vertices, Alchemy plugin) {
        this.vertices = vertices;
        edges = new Vector3d[4];
        for (int i = 0; i < 4; i++) {
            edges[i] = vertices[(i + 1) % 4].toVector().toVector3d().sub(vertices[i].toVector().toVector3d());
        }

        normal = new Vector3d(edges[0]).cross(edges[1]).normalize();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
    }

    @Override
    public Vector3d planePoint() {
        return null;
    }

    @Override
    public Vector3d normal() {
        return null;
    }

    @Override
    public boolean inside(Vector3d point) {
        return false;
    }

    @Override
    public List<Vector3d> intersect(Hitbox hitbox) {
        return null;
    }

    public void render() {
        World world = Bukkit.getServer().getWorld("world");
        double INTERVAL = 0.2;
        for (int i = 0; i < 4; i++) {
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
}
