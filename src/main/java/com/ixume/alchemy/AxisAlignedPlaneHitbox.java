package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class AxisAlignedPlaneHitbox implements Hitbox {
    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);

    private int axis;
    private Vector3d[] vertices;
    private Vector3d[] edges;
    private Vector3d normal;

    public AxisAlignedPlaneHitbox(Vector3d min, Vector3d max, Alchemy plugin) {
        vertices = new Vector3d[4];
        if (min.y == max.y) {
            //top/bottom aligned
            axis = 0;
            vertices[0] = new Vector3d(min.x, min.y, min.z);
            vertices[1] = new Vector3d(max.x, min.y, min.z);
            vertices[2] = new Vector3d(max.x, min.y, max.z);
            vertices[3] = new Vector3d(min.x, min.y, max.z);
        } else if (min.x == max.x) {
            //side x aligned
            axis = 1;
            vertices[0] = new Vector3d(min.x, min.y, min.z);
            vertices[1] = new Vector3d(min.x, max.y, min.z);
            vertices[2] = new Vector3d(min.x, max.y, max.z);
            vertices[3] = new Vector3d(min.x, min.y, max.z);
        } else {
            //side z aligned
            axis = 2;
            vertices[0] = new Vector3d(min.x, min.y, min.z);
            vertices[1] = new Vector3d(max.x, min.y, min.z);
            vertices[2] = new Vector3d(max.x, max.y, min.z);
            vertices[3] = new Vector3d(min.x, max.y, min.z);
        }

        edges = new Vector3d[4];
        edges[0] = new Vector3d(vertices[1]).sub(vertices[0]);
        edges[1] = new Vector3d(vertices[2]).sub(vertices[1]);
        edges[2] = new Vector3d(vertices[3]).sub(vertices[2]);
        edges[3] = new Vector3d(vertices[0]).sub(vertices[3]);
        normal = new Vector3d(edges[0]).cross(edges[1]);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            render();
        }, 1, 1);
    }

    @Override
    public Vector3d planePoint() {
        return new Vector3d(vertices[0]);
    }

    @Override
    public Vector3d normal() {
        return normal;
    }

    @Override
    public boolean inside(Vector3d point) {
        switch(axis) {
            case 0 -> {
                return (Math.min(vertices[0].x, vertices[2].x) <= point.x && point.x <= Math.max(vertices[0].x, vertices[2].x)
                && Math.min(vertices[0].z, vertices[2].z) <= point.z && point.z <= Math.max(vertices[0].z, vertices[2].z));
            }
            case 1 -> {
                return (Math.min(vertices[0].y, vertices[2].y) <= point.y && point.y <= Math.max(vertices[0].y, vertices[2].y)
                        && Math.min(vertices[0].z, vertices[2].z) <= point.z && point.z <= Math.max(vertices[0].z, vertices[2].z));
            }
            default -> {
                return (Math.min(vertices[0].y, vertices[2].y) <= point.y && point.y <= Math.max(vertices[0].y, vertices[2].y)
                        && Math.min(vertices[0].x, vertices[2].x) <= point.x && point.x <= Math.max(vertices[0].x, vertices[2].x));
            }
        }
    }

    public List<Vector3d> intersect(Hitbox hitbox) {
        List<Vector3d> intersections = new ArrayList<>();
        Vector3d planePoint = hitbox.planePoint();
        for (int i = 0; i < edges.length; i++) {
            Vector3d edge = edges[i];
            Vector3d edgeUnitVector = new Vector3d(edge).normalize();
            Vector3d edgePoint = new Vector3d(vertices[i]);
            if (new Vector3d(edgeUnitVector).dot(hitbox.normal()) == 0) continue;

            double factor = (new Vector3d(planePoint).sub(edgePoint)).dot(hitbox.normal())/(new Vector3d(edgeUnitVector).dot(hitbox.normal()));
            if (factor < 0d || factor > edge.length()) {
                continue;
            }

            Vector3d intersection = edgePoint.add(new Vector3d(edgeUnitVector).mul(factor));
            if (hitbox.inside(intersection)) {
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    public void render() {
        connect(vertices[0], vertices[1]);
        connect(vertices[1], vertices[2]);
        connect(vertices[2], vertices[3]);
        connect(vertices[3], vertices[0]);
    }

    private void connect(Vector3d a, Vector3d b) {
        World world = Bukkit.getServer().getWorld("world");
        Vector3d diff = new Vector3d(b).sub(a);
        for (double d = 0; d < 1d; d += 0.1) {
            Vector3d n = new Vector3d(a).add(new Vector3d(diff).mul(d));
            Location particleLocation = new Location(world, n.x, n.y, n.z);
            world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
        }
    }
}
