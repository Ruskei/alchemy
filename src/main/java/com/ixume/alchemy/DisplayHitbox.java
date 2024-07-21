package com.ixume.alchemy;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import com.ixume.alchemy.hitbox.ParallelogramHitboxFragment;
import com.ixume.alchemy.hitbox.TriangleHitboxFragment;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class DisplayHitbox implements GameObject, Hitbox {
    private final List<HitboxFragmentImpl> fragments;
    private List<Vector3d> vertices;
    private Vector3d origin;

//    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);

    public DisplayHitbox(Vector3d origin, Transformation transformation) {
        this.origin = origin;
        fragments = new ArrayList<>();
        vertices = new ArrayList<>();
        vertices.add(new Vector3d(0, 0, 0));
        vertices.add(new Vector3d(1, 0, 0));
        vertices.add(new Vector3d(1, 0, 1));
        vertices.add(new Vector3d(0, 0, 1));

        vertices.add(new Vector3d(0, 1, 0));
        vertices.add(new Vector3d(1, 1, 0));
        vertices.add(new Vector3d(1, 1, 1));
        vertices.add(new Vector3d(0, 1, 1));

        DisplayTransformation displayTransformation = new DisplayTransformation(transformation);
        Matrix4f finalMatrix = displayTransformation.getMatrix();
        vertices = vertices.stream().map(k -> finalMatrix.transform(new Vector4f((float) k.x, (float) k.y, (float) k.z, 1f))).map(k -> new Vector3d(origin.x + k.x, origin.y + k.y, origin.z + k.z)).toList();

        fragments.add(new ParallelogramHitboxFragment(vertices.get(0), new Vector3d(vertices.get(1)).sub(vertices.get(0)), new Vector3d(vertices.get(3)).sub(vertices.get(0))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(1), new Vector3d(vertices.get(2)).sub(vertices.get(1)), new Vector3d(vertices.get(5)).sub(vertices.get(1))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(2), new Vector3d(vertices.get(3)).sub(vertices.get(2)), new Vector3d(vertices.get(6)).sub(vertices.get(2))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(7), new Vector3d(vertices.get(3)).sub(vertices.get(7)), new Vector3d(vertices.get(4)).sub(vertices.get(7))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(4), new Vector3d(vertices.get(0)).sub(vertices.get(4)), new Vector3d(vertices.get(5)).sub(vertices.get(4))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(6), new Vector3d(vertices.get(5)).sub(vertices.get(6)), new Vector3d(vertices.get(7)).sub(vertices.get(6))));
    }

    public DisplayHitbox(Vector3d origin, Matrix4f matrix) {
        this.origin = origin;
        fragments = new ArrayList<>();
        vertices = new ArrayList<>();
        vertices.add(new Vector3d(0, 0, 0));
        vertices.add(new Vector3d(1, 0, 0));
        vertices.add(new Vector3d(1, 0, 1));
        vertices.add(new Vector3d(0, 0, 1));

        vertices.add(new Vector3d(0, 1, 0));
        vertices.add(new Vector3d(1, 1, 0));
        vertices.add(new Vector3d(1, 1, 1));
        vertices.add(new Vector3d(0, 1, 1));

        vertices = vertices.stream().map(k -> matrix.transform(new Vector4f((float) k.x, (float) k.y, (float) k.z, 1f))).map(k -> new Vector3d(origin.x + k.x, origin.y + k.y, origin.z + k.z)).toList();

        fragments.add(new ParallelogramHitboxFragment(vertices.get(0), new Vector3d(vertices.get(1)).sub(vertices.get(0)), new Vector3d(vertices.get(3)).sub(vertices.get(0))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(1), new Vector3d(vertices.get(2)).sub(vertices.get(1)), new Vector3d(vertices.get(5)).sub(vertices.get(1))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(2), new Vector3d(vertices.get(3)).sub(vertices.get(2)), new Vector3d(vertices.get(6)).sub(vertices.get(2))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(7), new Vector3d(vertices.get(3)).sub(vertices.get(7)), new Vector3d(vertices.get(4)).sub(vertices.get(7))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(4), new Vector3d(vertices.get(0)).sub(vertices.get(4)), new Vector3d(vertices.get(5)).sub(vertices.get(4))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(6), new Vector3d(vertices.get(5)).sub(vertices.get(6)), new Vector3d(vertices.get(7)).sub(vertices.get(6))));
    }

    public void setOrigin(Vector3d newOrigin) {
        Vector3d diff = new Vector3d(newOrigin).sub(origin);
        fragments.forEach(f -> ((ParallelogramHitboxFragment) f).setOrigin(((ParallelogramHitboxFragment) f).getOrigin().add(diff)));
        origin = newOrigin;
    }

    private void render() {
//        connect(vertices.get(0), vertices.get(1));
//        connect(vertices.get(1), vertices.get(2));
//        connect(vertices.get(2), vertices.get(3));
//        connect(vertices.get(3), vertices.get(0));
//
//        connect(vertices.get(0), vertices.get(4));
//        connect(vertices.get(1), vertices.get(5));
//        connect(vertices.get(2), vertices.get(6));
//        connect(vertices.get(3), vertices.get(7));
//
//        connect(vertices.get(4), vertices.get(5));
//        connect(vertices.get(5), vertices.get(6));
//        connect(vertices.get(6), vertices.get(7));
//        connect(vertices.get(7), vertices.get(4));
    }

//    private void connect(Vector3d a, Vector3d b) {
//        World world = Bukkit.getServer().getWorld("world");
//        Vector3d diff = new Vector3d(b).sub(a);
//        for (double d = 0; d < 1d; d += 0.1) {
//            Vector3d n = new Vector3d(a).add(new Vector3d(diff).mul(d));
//            Location particleLocation = new Location(world, n.x, n.y, n.z);
//            world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
//        }
//    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        List<Vector3d> intersections = new ArrayList<>();
        for (HitboxFragmentImpl frag1 : fragments) {
            for (HitboxFragmentImpl frag2 : hitbox.getFragments()) {
                intersections.addAll(frag1.intersect(frag2));
            }
        }

        return intersections;
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return fragments;
    }

    @Override
    public List<Vector3d> collide(Entity box) {
        List<Vector3d> collisions = new ArrayList<>();
        fragments.forEach(f -> collisions.addAll(f.intersect(box.getBoundingBox())));
        return collisions;
    }

    @Override
    public void tick() {

    }
}
