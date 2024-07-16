package com.ixume.alchemy;

import org.joml.Vector3d;

public interface HitboxFragment {
    Vector3d planePoint();
    Vector3d normal();
    boolean inside(Vector3d point);
}
