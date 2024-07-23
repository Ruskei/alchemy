package com.ixume.alchemy.gameobject.virtualobjects;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class VirtualPlane {
    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.5F);

    public final int axis;
    public final Vector3d[] vertices;
    public final Vector3d[] edges;
    public final Vector3d normal;
    private final Player player;
    private final Vector3d min;
    private final Vector3d max;

    public VirtualPlane(Vector3d min, Vector3d max, int scalar, Player player) {
        this.min = min;
        this.max = max;
        this.player = player;
        if (min.y == max.y) {
            axis = 0;
            vertices = new Vector3d[]{
                    new Vector3d(min),
                    new Vector3d(max.x, min.y, min.z),
                    new Vector3d(max),
                    new Vector3d(min.x, min.y, max.z)};
        } else if (min.x == max.x) {
            axis = 1;
            vertices = new Vector3d[]{
                    new Vector3d(min),
                    new Vector3d(min.x, min.y, max.z),
                    new Vector3d(min.x, max.y, max.z),
                    new Vector3d(min.x, max.y, min.z)};
        } else {
            axis = 2;
            vertices = new Vector3d[]{
                    new Vector3d(min),
                    new Vector3d(max.x, min.y, min.z),
                    new Vector3d(max.x, max.y, min.z),
                    new Vector3d(min.x, max.y, min.z)};
        }

        edges = new Vector3d[4];
        for (int i = 0; i < 4; i++) {
            edges[i] = new Vector3d(vertices[(i + 1) % 4]).sub(vertices[i]);
        }
        normal = (new Vector3d(edges[0]).cross(edges[1])).normalize().mul(scalar);
    }

    public void render() {
        World world = Bukkit.getServer().getWorld("world");
        double INTERVAL = 0.3;
        for (int i = 0; i < edges.length; i++) {
            Vector3d edge = edges[i];
            Vector3d normalizedEdge = new Vector3d(edge);
            normalizedEdge.normalize();
            for (double d = 0; d < edge.length(); d += INTERVAL) {
                Location particleLocation = new Location(world, vertices[i].x, vertices[i].y, vertices[i].z);
                Vector3d fractionalEdge = new Vector3d(normalizedEdge).mul(d);
                particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
                world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
            }
        }

        for (double d = 0; d < 1; d += INTERVAL) {
            Location particleLocation = new Location(world, vertices[0].x, vertices[0].y, vertices[0].z);
            Vector3d fractionalEdge = new Vector3d(normal).mul(d);
            particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
            world.spawnParticle(Particle.DUST, particleLocation, 1, normalDust);
        }
    }

    public Pair<Vector3d, Vector3d> intersectNS() {
        World world = Bukkit.getServer().getWorld("world");
        BoundingBox box = player.getBoundingBox();

        Vector3d NORTH_SOUTH_VECTOR = new Vector3d(0, 0, -1);
        List<Vector3d> northIntersections = new ArrayList<>();
        List<Vector3d> southIntersections = new ArrayList<>();
        //NORTH
        Pair<Vector3d, Boolean> northBottomWest = intersectVector(
                normal,
                vertices[0],
                NORTH_SOUTH_VECTOR,
                box.getMin().toVector3d(),
                false);
        if (northBottomWest != null && isInside(northBottomWest.getLeft())) {
            if (northBottomWest.getRight()) {
                northIntersections.add(northBottomWest.getLeft());
            } else {
                southIntersections.add(northBottomWest.getLeft());
            }
        }

        Pair<Vector3d, Boolean> northBottomEast = intersectVector(
                normal,
                vertices[0],
                NORTH_SOUTH_VECTOR,
                new Vector3d(box.getMaxX(), box.getMinY(), box.getMinZ()),
                false);
        if (northBottomEast != null && isInside(northBottomEast.getLeft())) {
            if (northBottomEast.getRight()) {
                northIntersections.add(northBottomEast.getLeft());
            } else {
                southIntersections.add(northBottomEast.getLeft());
            }
        }

        Pair<Vector3d, Boolean> northTopWest = intersectVector(
                normal,
                vertices[0],
                NORTH_SOUTH_VECTOR,
                new Vector3d(box.getMinX(), box.getMaxY(), box.getMinZ()),
                false);
        if (northTopWest != null && isInside(northTopWest.getLeft())) {
            if (northTopWest.getRight()) {
                northIntersections.add(northTopWest.getLeft());
            } else {
                southIntersections.add(northTopWest.getLeft());
            }
        }

        Pair<Vector3d, Boolean> northTopEast = intersectVector(
                normal,
                vertices[0],
                NORTH_SOUTH_VECTOR,
                new Vector3d(box.getMaxX(), box.getMaxY(), box.getMinZ()),
                false);
        if (northTopEast != null && isInside(northTopEast.getLeft())) {
            if (northTopEast.getRight()) {
                northIntersections.add(northTopEast.getLeft());
            } else {
                southIntersections.add(northTopEast.getLeft());
            }
        }

        double northDistance = Double.NEGATIVE_INFINITY;
        Vector3d northIntersection = null;
        if (new Vector3d(NORTH_SOUTH_VECTOR).dot(normal) < 0) {
            for (Vector3d intersection : northIntersections) {
                if (intersection.z > northDistance) {
                    northDistance = intersection.z;
                    northIntersection = intersection;
                }
            }
        }

        double southDistance = Double.POSITIVE_INFINITY;
        Vector3d southIntersection = null;
        if (new Vector3d(NORTH_SOUTH_VECTOR).dot(normal) > 0) {
            for (Vector3d intersection : southIntersections) {
                if (intersection.z < southDistance) {
                    southDistance = intersection.z;
                    southIntersection = intersection;
                }
            }
        }

        if (southIntersection != null) {
            southIntersection.sub(new Vector3d(normal).mul(0.5));
            world.spawnParticle(Particle.DUST, new Location(world, southIntersection.x, southIntersection.y, southIntersection.z), 1, edgeDust);
        }

        if (northIntersection != null) {
            northIntersection.sub(new Vector3d(normal).mul(0.5));
            world.spawnParticle(Particle.DUST, new Location(world, northIntersection.x, northIntersection.y, northIntersection.z), 1, edgeDust);
        }

        return Pair.of(northIntersection, southIntersection);
    }

    private Pair<Vector3d, Boolean> intersectVector(Vector3d planeNormal,
                                                    Vector3d planePoint,
                                                    Vector3d lineVector,
                                                    Vector3d linePoint,
                                                    boolean limited) {
        Vector3d normalizedLineVector = new Vector3d(lineVector).normalize();
        double denominator = new Vector3d(planeNormal).dot(normalizedLineVector);
        if (denominator == 0d) return null;
        double factor = new Vector3d(planePoint.x - linePoint.x, planePoint.y - linePoint.y, planePoint.z - linePoint.z).dot(planeNormal) / denominator;
        if (limited && (factor > lineVector.length() || factor < 0d)) return null;
        return Pair.of(new Vector3d(linePoint).add(normalizedLineVector.mul(factor)), factor > 0);
    }

    private boolean isInside(Vector3d point) {
        if (axis == 0) {
            return (min.x <= point.x && point.x <= max.x
            && min.z <= point.z && point.z <= max.z);
        } else if (axis == 1) {
            return (min.y <= point.y && point.y <= max.y
                    && min.z <= point.z && point.z <= max.z);
        } else {
            return (min.x <= point.x && point.x <= max.x
                    && min.y <= point.y && point.y <= max.y);
        }
    }
}
