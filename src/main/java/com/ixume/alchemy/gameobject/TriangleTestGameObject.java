package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import com.ixume.alchemy.hitbox.TriangleHitboxFragment;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class TriangleTestGameObject implements GameObject, Hitbox {
    private final List<HitboxFragmentImpl> hitboxFragments;
    public TriangleTestGameObject(TriangleHitboxFragment singletonHitbox) {
        hitboxFragments = new ArrayList<>();
        hitboxFragments.add(singletonHitbox);
    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        List<Vector3d> intersections = new ArrayList<>();
        for (HitboxFragmentImpl frag1 : hitboxFragments) {
            for (HitboxFragmentImpl frag2 : hitbox.getFragments()) {
                intersections.addAll(frag1.intersect(frag2));
            }
        }

        return intersections;
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return hitboxFragments;
    }

    @Override
    public List<Vector3d> collide(Entity box) {
        List<Vector3d> collisions = new ArrayList<>();
        hitboxFragments.forEach(f -> collisions.addAll(f.intersect(box.getBoundingBox())));
        return collisions;
    }

    @Override
    public void tick() {

    }
}
