package com.ixume.alchemy;

import org.bukkit.util.BoundingBox;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxGameObject {
    private final List<AxisAlignedPlaneHitbox> hitboxes;
    public BoundingBoxGameObject(BoundingBox boundingBox, Alchemy plugin) {
        hitboxes = new ArrayList<>();
        //bottom
        hitboxes.add(new AxisAlignedPlaneHitbox(boundingBox.getMin().toVector3d(),
                new Vector3d(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ()),
                plugin));
        //top
        hitboxes.add(new AxisAlignedPlaneHitbox(new Vector3d(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ()),
                new Vector3d(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()),
                plugin));
        //0
        hitboxes.add(new AxisAlignedPlaneHitbox(boundingBox.getMin().toVector3d(),
                new Vector3d(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ()),
                plugin));
        //1
        hitboxes.add(new AxisAlignedPlaneHitbox(new Vector3d(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ()),
                new Vector3d(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()),
                plugin));
        //2
        hitboxes.add(new AxisAlignedPlaneHitbox(boundingBox.getMin().toVector3d(),
                new Vector3d(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ()),
                plugin));
        //3
        hitboxes.add(new AxisAlignedPlaneHitbox(new Vector3d(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ()),
                new Vector3d(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()),
                plugin));

        hitboxes.forEach(h -> HitboxRenderer.getInstance().addHitbox(h));
    }
}
