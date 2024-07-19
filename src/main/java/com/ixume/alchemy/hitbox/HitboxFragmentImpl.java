package com.ixume.alchemy.hitbox;

import org.bukkit.util.BoundingBox;
import org.joml.Vector3d;

import java.util.List;

public interface HitboxFragmentImpl {
    Vector3d planePoint();
    Vector3d normal();
    boolean inside(Vector3d point);

    List<Vector3d> intersect(HitboxFragmentImpl hitboxFragment);
    List<Vector3d> intersect(BoundingBox box);

    void render();
}
