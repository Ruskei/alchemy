package com.ixume.alchemy.gameobject.physical;

import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3f;

public interface Physical {
    long isInside(Vector3f isInside);
    Pair<Vector3f, Vector3f> getBoundingBox();
}
