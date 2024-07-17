package com.ixume.alchemy;

import org.joml.Vector3d;

import java.util.List;

public interface Hitbox {
    Vector3d planePoint();
    Vector3d normal();
    boolean inside(Vector3d point);

    List<Vector3d> intersect(Hitbox hitbox);

    void render();
}
