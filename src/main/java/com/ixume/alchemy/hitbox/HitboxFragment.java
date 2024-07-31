package com.ixume.alchemy.hitbox;

import org.bukkit.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class HitboxFragment {
    private final Vector3d[] vertices;
    private final Vector3d[] edges;
    private final Vector3d normal;
    private final World world;

    public HitboxFragment(Vector3d[] vertices, Vector3d[] edges, Vector3d normal, World world) {
        this.vertices = vertices;
        this.edges = edges;
        this.normal = normal;
        this.world = world;
    }

    public Vector3d[] getVertices() {
        return vertices;
    }

    public Vector3d[] getEdges() {
        return edges;
    }

    public Vector3d planePoint() {
        return new Vector3d(vertices[1]);
    }

    public Vector3d normal() {
        return new Vector3d(normal);
    }

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.1F);

    public void render() {
        double INTERVAL = 0.2;
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
            Location particleLocation = new Location(world, vertices[1].x, vertices[1].y, vertices[1].z);
            Vector3d fractionalEdge = new Vector3d(normal).mul(d);
            particleLocation.add(new Vector(fractionalEdge.x, fractionalEdge.y, fractionalEdge.z));
            world.spawnParticle(Particle.DUST, particleLocation, 1, normalDust);
        }
    }

    public List<Vector3d> intersect(HitboxFragmentImpl hitboxFragment) {
        List<Vector3d> intersections = new ArrayList<>();
        Vector3d planePoint = hitboxFragment.planePoint();
        for (int i = 0; i < edges.length; i++) {
            Vector3d edge = edges[i];
            Vector3d edgeUnitVector = new Vector3d(edge).normalize();
            Vector3d edgePoint = new Vector3d(vertices[i]);
            double factor = (new Vector3d(planePoint).sub(edgePoint)).dot(hitboxFragment.normal())/(new Vector3d(edgeUnitVector).dot(hitboxFragment.normal()));
            if (factor < 0d || factor > edge.length()) {
                continue;
            }

            Vector3d intersection = edgePoint.add(new Vector3d(edgeUnitVector).mul(factor));
            if (hitboxFragment.inside(intersection)) {
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    public List<Vector3d> intersect(BoundingBox boundingBox,
                                           HitboxFragmentImpl fragment) {
        List<Vector3d> intersections = new ArrayList<>();
        Vector3d min = new Vector3d(Math.min(boundingBox.getMinX(), boundingBox.getMaxX()), Math.min(boundingBox.getMinY(), boundingBox.getMaxY()), Math.min(boundingBox.getMinZ(), boundingBox.getMaxZ()));
        Vector3d max = new Vector3d(Math.max(boundingBox.getMinX(), boundingBox.getMaxX()), Math.max(boundingBox.getMinY(), boundingBox.getMaxY()), Math.max(boundingBox.getMinZ(), boundingBox.getMaxZ()));
        //first part is colliding the triangle with the bounding box
        //then collide the bounding box with the triangle
        //first part needs the normal vectors and plane points and calculated for each edge
        //first part is not unique in the slightest and just works per edge, this is the same for all hitbox fragments
        //second part differs only in the "isInside" methods
        for (int i = 0; i < edges.length; i++) {
            Vector3d vertex = new Vector3d(vertices[i]);
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
                    min.z <= westIntersection.z && westIntersection.z <= max.z &&
                    min.y <= westIntersection.y && westIntersection.y <= max.y) {
                intersections.add(westIntersection);
            }

            Vector3d eastIntersection = intersectVector(new Vector3d(1, 0, 0),
                    max,
                    edge,
                    vertex);
            if (eastIntersection != null &&
                    min.z <= eastIntersection.z && eastIntersection.z <= max.z &&
                    min.y <= eastIntersection.y && eastIntersection.y <= max.y) {
                intersections.add(eastIntersection);
            }
        }

        Vector3d trianglePlanePoint = fragment.planePoint();

        //bottom
        Vector3d bottomSouth = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, 0, max.z - min.z),
                min);
        if (bottomSouth != null && fragment.inside(bottomSouth)) {
            intersections.add(bottomSouth);
        }

        Vector3d bottomNorth = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, 0, min.z - max.z),
                new Vector3d(max.x, min.y, max.z));
        if (bottomNorth != null && fragment.inside(bottomNorth)) {
            intersections.add(bottomNorth);
        }

        Vector3d bottomEast = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(min.x - max.x, 0, 0),
                new Vector3d(max.x, min.y, max.z));
        if (bottomEast != null && fragment.inside(bottomEast)) {
            intersections.add(bottomEast);
        }

        Vector3d bottomWest = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(max.x - min.x, 0, 0),
                min);
        if (bottomWest != null && fragment.inside(bottomWest)) {
            intersections.add(bottomWest);
        }

        //top
        Vector3d topSouth = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, 0, max.z - min.z),
                new Vector3d(min.x, max.y, min.z));
        if (topSouth != null && fragment.inside(topSouth)) {
            intersections.add(topSouth);
        }

        Vector3d topNorth = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, 0, min.z - max.z),
                max);
        if (topNorth != null && fragment.inside(topNorth)) {
            intersections.add(topNorth);
        }

        Vector3d topEast = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(min.x - max.x, 0, 0),
                max);
        if (topEast != null && fragment.inside(topEast)) {
            intersections.add(topEast);
        }

        Vector3d topWest = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(max.x - min.x, 0, 0),
                new Vector3d(min.x, max.y, min.z));
        if (topWest != null && fragment.inside(topWest)) {
            intersections.add(topWest);
        }

        //sides
        Vector3d northWest = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, max.y - min.y, 0),
                min);
        if (northWest != null && fragment.inside(northWest)) {
            intersections.add(northWest);
        }

        Vector3d southWest = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, max.y - min.y, 0),
                new Vector3d(min.x, min.y, max.z));
        if (southWest != null && fragment.inside(southWest)) {
            intersections.add(southWest);
        }

        Vector3d southEast = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, max.y - min.y, 0),
                new Vector3d(max.x, min.y, max.z));
        if (southEast != null && fragment.inside(southEast)) {
            intersections.add(southEast);
        }

        Vector3d northEast = intersectVector(normal,
                trianglePlanePoint,
                new Vector3d(0, max.y - min.y, 0),
                new Vector3d(max.x, min.y, min.z));
        if (northEast != null && fragment.inside(northEast)) {
            intersections.add(northEast);
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
