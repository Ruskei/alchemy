package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.bending.directionadjuster.DirectionAdjuster;
import com.ixume.alchemy.gameobject.physical.Physical;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class VisualBlockDisplay implements Physical {
    private final Vector3f relativeOrigin;
    private final Vector3f absoluteOrigin;
    private final Matrix4f transformationMatrix;
    private final BlockData displayData;
    private final DirectionAdjuster directionAdjuster;
    private final DisplayHitbox hitbox;

    public VisualBlockDisplay(World world, Vector3f absoluteOrigin, Vector3f relativeOrigin, Matrix4f transformationMatrix, BlockData displayData, DirectionAdjuster directionAdjuster) {
        this.absoluteOrigin = absoluteOrigin;
        this.relativeOrigin = relativeOrigin;
        this.transformationMatrix = transformationMatrix;
        this.displayData = displayData;
        this.directionAdjuster = directionAdjuster;
        hitbox = new DisplayHitbox(new Vector3d(absoluteOrigin), transformationMatrix, world);
    }

    public Vector3f absoluteOrigin() {
        return absoluteOrigin;
    }

    public Vector3f relativeOrigin() {
        return relativeOrigin;
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

    public Vector3f adjust(Vector3f origin, Vector3f dir) {
        return directionAdjuster.adjust(new Vector3f(relativeOrigin), origin, dir);
    }

    @Override
    public long isInside(Vector3f isInside) {
        return hitbox.isInside(isInside);
    }

    @Override
    public Pair<Vector3f, Vector3f> getBoundingBox() {
        return hitbox.getBoundingBox();
    }
}
