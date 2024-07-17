package com.ixume.alchemy;

import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxGameObject implements GameObject {
    private final List<Hitbox> hitboxes;
    private Entity entity;
    private BoundingBox boundingBox;
    private Vector loc;

    public BoundingBoxGameObject(Entity entity, Alchemy plugin) {
        this.entity = entity;
        this.boundingBox = entity.getBoundingBox();
        loc = boundingBox.getCenter().clone();
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

        HitboxRenderer.getInstance().addHitbox(this);
    }

    @Override
    public List<Hitbox> getHitboxes() {
        return hitboxes;
    }

    public void update() {
        boundingBox = entity.getBoundingBox();
        Vector diff = boundingBox.getCenter().clone().subtract(loc);
        Vector3d diff2 = new Vector3d(diff.getX(), diff.getY(), diff.getZ());
        hitboxes.forEach(h -> ((AxisAlignedPlaneHitbox) h).translate(diff2));
        loc = boundingBox.getCenter();
    }
}
