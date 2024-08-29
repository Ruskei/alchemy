package com.ixume.alchemy.gameobject.bending;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.ixume.alchemy.gameobject.bending.EarthbendingDisplayImpl.IDENTITY;

public class RotatedDirectionAdjuster implements DirectionAdjuster {
    private static RotatedDirectionAdjuster INSTANCE;
    public static RotatedDirectionAdjuster getInstance() {
        if (INSTANCE == null) INSTANCE = new RotatedDirectionAdjuster();
        return INSTANCE;
    }

    @Override
    public Matrix4f adjust(Matrix4f toAdjust, Vector3f dir) {
        Matrix4f rotatedMatrix = new Matrix4f(toAdjust);
        Quaternionf temp = new Quaternionf();
        temp.rotateTo(IDENTITY, dir);
        rotatedMatrix.rotate(temp);
        return temp.get(rotatedMatrix).mul(toAdjust);
    }
}
