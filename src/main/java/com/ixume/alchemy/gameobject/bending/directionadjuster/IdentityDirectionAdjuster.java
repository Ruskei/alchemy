package com.ixume.alchemy.gameobject.bending.directionadjuster;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.ixume.alchemy.gameobject.bending.EarthbendingDisplayImpl.IDENTITY;

public class IdentityDirectionAdjuster implements DirectionAdjuster {
    private static IdentityDirectionAdjuster INSTANCE;
    public static IdentityDirectionAdjuster getInstance() {
        if (INSTANCE == null) INSTANCE = new IdentityDirectionAdjuster();
        return INSTANCE;
    }

    @Override
    public Matrix4f adjust(Matrix4f toAdjust, Vector3f dir) {
        return toAdjust;
    }

    @Override
    public Vector3f adjust(Vector3f toAdjust, Vector3f origin, Vector3f dir) {
        Quaternionf rotationQuaternion = new Quaternionf().rotateTo(IDENTITY, dir);
        return toAdjust.rotate(rotationQuaternion).add(origin);
    }
}
