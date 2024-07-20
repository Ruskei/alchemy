package com.ixume.alchemy;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import com.ixume.alchemy.hitbox.ParallelogramHitboxFragment;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class VirtualBlockDisplay implements GameObject, Hitbox {
    private final List<HitboxFragmentImpl> fragments;
    private List<Vector3d> vertices;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);

    public VirtualBlockDisplay(Location origin, Alchemy plugin) {
        fragments = new ArrayList<>();
        Vector3d originVector = origin.clone().toVector().toVector3d();
        vertices = new ArrayList<>();
        vertices.add(new Vector3d(0, 0, 0));
        vertices.add(new Vector3d(1, 0, 0));
        vertices.add(new Vector3d(1, 0, 1));
        vertices.add(new Vector3d(0, 0, 1));

        vertices.add(new Vector3d(0, 1, 0));
        vertices.add(new Vector3d(1, 1, 0));
        vertices.add(new Vector3d(1, 1, 1));
        vertices.add(new Vector3d(0, 1, 1));

        Quaterniond q = new Quaterniond();
        q.rotateY(27d * Math.PI / 180d);
        Matrix3d matrix = new Matrix3d();
        q.get(matrix);
        vertices = vertices.stream().map(k ->
            k.mul(matrix).add(originVector)).toList();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
    }

    public VirtualBlockDisplay(Vector3d origin, Transformation transformation) {
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

        Matrix3d leftRotation = new Matrix3d();
        transformation.getLeftRotation().get(leftRotation);
        Matrix3d rightRotation = new Matrix3d();
        transformation.getRightRotation().get(rightRotation);
        Vector3d scale = new Vector3d(transformation.getScale());

        vertices = vertices.stream().map(k ->
                k.mul(rightRotation).mul(scale).mul(leftRotation).add(transformation.getTranslation()).add(origin)).toList();

        fragments.add(new ParallelogramHitboxFragment(vertices.get(0), new Vector3d(vertices.get(1)).sub(vertices.get(0)), new Vector3d(vertices.get(3)).sub(vertices.get(0))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(1), new Vector3d(vertices.get(2)).sub(vertices.get(1)), new Vector3d(vertices.get(5)).sub(vertices.get(1))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(2), new Vector3d(vertices.get(3)).sub(vertices.get(2)), new Vector3d(vertices.get(6)).sub(vertices.get(2))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(7), new Vector3d(vertices.get(3)).sub(vertices.get(7)), new Vector3d(vertices.get(4)).sub(vertices.get(7))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(4), new Vector3d(vertices.get(0)).sub(vertices.get(4)), new Vector3d(vertices.get(5)).sub(vertices.get(4))));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(6), new Vector3d(vertices.get(5)).sub(vertices.get(6)), new Vector3d(vertices.get(7)).sub(vertices.get(6))));
    }

    private void render() {
        connect(vertices.get(0), vertices.get(1));
        connect(vertices.get(1), vertices.get(2));
        connect(vertices.get(2), vertices.get(3));
        connect(vertices.get(3), vertices.get(0));

        connect(vertices.get(0), vertices.get(4));
        connect(vertices.get(1), vertices.get(5));
        connect(vertices.get(2), vertices.get(6));
        connect(vertices.get(3), vertices.get(7));

        connect(vertices.get(4), vertices.get(5));
        connect(vertices.get(5), vertices.get(6));
        connect(vertices.get(6), vertices.get(7));
        connect(vertices.get(7), vertices.get(4));
    }

    private void connect(Vector3d a, Vector3d b) {
        World world = Bukkit.getServer().getWorld("world");
        Vector3d diff = new Vector3d(b).sub(a);
        for (double d = 0; d < 1d; d += 0.1) {
            Vector3d n = new Vector3d(a).add(new Vector3d(diff).mul(d));
            Location particleLocation = new Location(world, n.x, n.y, n.z);
            world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
        }
    }

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
    public List<Vector3d> collide(BoundingBox box) {
        List<Vector3d> collisions = new ArrayList<>();
        fragments.forEach(f -> collisions.addAll(f.intersect(box)));
        return collisions;
    }
}
