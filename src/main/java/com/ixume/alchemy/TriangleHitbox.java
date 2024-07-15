package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class TriangleHitbox {
    private Location[] vertices;
    private Vector3d[] edges;
    private Vector3d normal;

    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.6F);;

    public TriangleHitbox(Location[] vertices, Alchemy plugin) {
        this.vertices = vertices;
        edges = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            edges[i] = vertices[(i + 1) % 3].toVector().toVector3d().sub(vertices[i].toVector().toVector3d());
        }

        normal = new Vector3d(edges[0]).cross(edges[1]).normalize();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
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
                world.spawnParticle(Particle.DUST, particleLocation, 1, dustOptions);
            }
        }

        for (double d = 0; d < 1; d += INTERVAL) {
            Location particleLocation = vertices[1].clone();
            Vector3d fractionalEdge = new Vector3d(normal).mul(d);
            particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
            world.spawnParticle(Particle.DUST, particleLocation, 1, dustOptions);
        }
    }
}
