package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class TriangleHitbox implements Hitbox {
    private Location[] vertices;
    private Vector3d[] edges;
    private Vector3d normal;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.4F);

    public TriangleHitbox(Location[] vertices, Alchemy plugin) {
        this.vertices = vertices;
        edges = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            edges[i] = vertices[(i + 1) % 3].toVector().toVector3d().sub(vertices[i].toVector().toVector3d());
        }

        normal = new Vector3d(edges[0]).cross(edges[1]).normalize();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
    }

    public List<Vector3d> intersect(Hitbox hitbox) {
        List<Vector3d> intersections = new ArrayList<>();
        Vector3d planePoint = hitbox.planePoint();
        for (int i = 0; i < edges.length; i++) {
            Vector3d edge = edges[i];
            Vector3d edgeUnitVector = new Vector3d(edge).normalize();
            Vector3d edgePoint = vertices[i].toVector().toVector3d();
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

    public List<Vector3d> intersectEntity(Entity entity) {
        List<Vector3d> intersections = new ArrayList<>();
        BoundingBox boundingBox = entity.getBoundingBox();
        Vector3d min = new Vector3d(Math.min(boundingBox.getMinX(), boundingBox.getMaxX()), Math.min(boundingBox.getMinY(), boundingBox.getMaxY()), Math.min(boundingBox.getMinZ(), boundingBox.getMaxZ()));
        Vector3d max = new Vector3d(Math.max(boundingBox.getMinX(), boundingBox.getMaxX()), Math.max(boundingBox.getMinY(), boundingBox.getMaxY()), Math.max(boundingBox.getMinZ(), boundingBox.getMaxZ()));
        //first part is colliding the triangle with the bounding box
        //then collide the bounding box with the triangle
        //first part needs the normal vectors and plane points and calculated for each edge
        for (int i = 0; i < 3; i++) {
            Vector3d vertex = vertices[i].toVector().toVector3d();
            Vector3d edge = new Vector3d(edges[i]);

            Vector3d bottomIntersection = intersectVector(new Vector3d(0, -1, 0),
                    min,
                    edge,
                    vertex);
            if (bottomIntersection != null &&
                    min.x <= bottomIntersection.x && bottomIntersection.x <= max.x &&
                    min.z <= bottomIntersection.z && bottomIntersection.z <= max.z) {
                intersections.add(bottomIntersection);
            }

            Vector3d topIntersection = intersectVector(new Vector3d(0, 1, 0),
                    max,
                    edge,
                    vertex);
            if (topIntersection != null &&
                    min.x <= topIntersection.x && topIntersection.x <= max.x &&
                    min.z <= topIntersection.z && topIntersection.z <= max.z) {
                intersections.add(topIntersection);
            }

            Vector3d northIntersection = intersectVector(new Vector3d(0, 0, -1),
                    min,
                    edge,
                    vertex);
            if (northIntersection != null &&
                    min.x <= northIntersection.x && northIntersection.x <= max.x &&
                    min.y <= northIntersection.y && northIntersection.y <= max.y) {
                intersections.add(northIntersection);
            }

            Vector3d southIntersection = intersectVector(new Vector3d(0, 0, 1),
                    max,
                    edge,
                    vertex);
            if (southIntersection != null &&
                    min.x <= southIntersection.x && southIntersection.x <= max.x &&
                    min.y <= southIntersection.y && southIntersection.y <= max.y) {
                intersections.add(southIntersection);
            }

            Vector3d westIntersection = intersectVector(new Vector3d(-1, 0, 0),
                    min,
                    edge,
                    vertex);
            if (westIntersection != null &&
                    min.z <= westIntersection.z && westIntersection.x <= max.z &&
                    min.y <= westIntersection.y && westIntersection.y <= max.y) {
                intersections.add(westIntersection);
            }

            Vector3d eastIntersection = intersectVector(new Vector3d(1, 0, 0),
                    max,
                    edge,
                    vertex);
            if (eastIntersection != null &&
                    min.z <= eastIntersection.z && eastIntersection.x <= max.z &&
                    min.y <= eastIntersection.y && eastIntersection.y <= max.y) {
                intersections.add(eastIntersection);
            }
        }

        return intersections;
    }

    private Vector3d intersectVector(Vector3d planeNormal,
                                     Vector3d planePoint,
                                     Vector3d lineVector,
                                     Vector3d linePoint) {
        Vector3d normalizedLineVector = new Vector3d(lineVector).normalize();
        double denominator = new Vector3d(planeNormal).dot(normalizedLineVector);
        if (denominator == 0d) return null;
        double factor = new Vector3d(planePoint.x - linePoint.x, planePoint.y - linePoint.y, planePoint.z - linePoint.z).dot(planeNormal) / denominator;
        if (factor < 0d || factor > lineVector.length()) return null;
        return new Vector3d(linePoint).add(normalizedLineVector.mul(factor));
    }
}
