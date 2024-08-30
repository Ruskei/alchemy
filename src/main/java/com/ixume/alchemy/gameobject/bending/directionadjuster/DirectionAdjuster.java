package com.ixume.alchemy.gameobject.bending.directionadjuster;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface DirectionAdjuster {
    public Matrix4f adjust(Matrix4f toAdjust, Vector3f dir);
    public Vector3f adjust(Vector3f toAdjust, Vector3f origin, Vector3f dir);
}
