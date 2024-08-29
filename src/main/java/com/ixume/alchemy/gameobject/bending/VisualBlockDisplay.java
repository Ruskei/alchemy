package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.gameobject.bending.directionadjuster.DirectionAdjuster;
import org.bukkit.block.data.BlockData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class VisualBlockDisplay {
    private final Vector3f origin;
    private final Matrix4f transformationMatrix;
    private final BlockData displayData;
    private final DirectionAdjuster directionAdjuster;

    public VisualBlockDisplay(Vector3f origin, Matrix4f transformationMatrix, BlockData displayData, DirectionAdjuster directionAdjuster) {
        this.origin = origin;
        this.transformationMatrix = transformationMatrix;
        this.displayData = displayData;
        this.directionAdjuster = directionAdjuster;
    }

    public Vector3f origin() {
        return origin;
    }

    public Matrix4f transformationMatrix() {
        return transformationMatrix;
    }

    public BlockData displayData() {
        return displayData;
    }

    public Matrix4f adjust(Vector3f dir) {
        return directionAdjuster.adjust(transformationMatrix, dir);
    }
}
