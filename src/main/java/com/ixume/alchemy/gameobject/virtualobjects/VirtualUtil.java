package com.ixume.alchemy.gameobject.virtualobjects;

import org.joml.Vector3d;

import java.util.List;

public final class VirtualUtil {
    public static Vector3d getShulkerPos(List<VirtualPlane> planes) {
        VirtualPlane yPlane = planes.stream().filter(p -> p.axis == 0).findFirst().get();
        VirtualPlane xPlane = planes.stream().filter(p -> p.axis == 1).findFirst().get();
        VirtualPlane zPlane = planes.stream().filter(p -> p.axis == 2).findFirst().get();

        return new Vector3d(xPlane.vertices[0].x - xPlane.normal.x / 2f, yPlane.vertices[0].y - yPlane.normal.y / 2f, zPlane.vertices[0].z - zPlane.normal.z / 2f);
    }
}
