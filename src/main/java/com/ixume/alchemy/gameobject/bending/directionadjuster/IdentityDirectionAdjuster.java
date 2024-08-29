package com.ixume.alchemy.gameobject.bending.directionadjuster;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class IdentityDirectionAdjuster implements DirectionAdjuster {
    private static RotatedDirectionAdjuster INSTANCE;
    public static RotatedDirectionAdjuster getInstance() {
        if (INSTANCE == null) INSTANCE = new RotatedDirectionAdjuster();
        return INSTANCE;
    }

    @Override
    public Matrix4f adjust(Matrix4f toAdjust, Vector3f dir) {
        return toAdjust;
    }
}
