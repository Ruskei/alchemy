package com.ixume.alchemy.playerdata;

import org.joml.Vector3d;

public class Plane {
    public Vector3d normal;
    public Vector3d point;

    public Plane(Vector3d normal, Vector3d point) {
        this.normal = normal;
        this.point = point;
    }

    public Vector3d intersectWithVector(Vector3d lineVector,
                                     Vector3d linePoint) {
        Vector3d normalizedLineVector = new Vector3d(lineVector).normalize();
        double denominator = new Vector3d(normal).dot(normalizedLineVector);
        if (denominator == 0d) return null;
        double factor = new Vector3d(point.x - linePoint.x, point.y - linePoint.y, point.z - linePoint.z).dot(normal) / denominator;
        if (factor < 0d || factor > lineVector.length()) return null;
        return new Vector3d(linePoint).add(normalizedLineVector.mul(factor));
    }
}
