package com.ixume.alchemy;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import com.ixume.alchemy.hitbox.ParallelogramHitboxFragment;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Transformation;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class DisplayHitbox implements GameObject, Hitbox, com.ixume.alchemy.gameobject.physical.Physical {
    private final GameObjectTicker ticker;
    private final List<HitboxFragmentImpl> fragments;
    private List<Vector3d> vertices;
    private Vector3d origin;

    public DisplayHitbox(Vector3d origin, Transformation transformation, World world) {
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
        fragments.add(new ParallelogramHitboxFragment(vertices.get(0), new Vector3d(vertices.get(1)).sub(vertices.get(0)), new Vector3d(vertices.get(3)).sub(vertices.get(0)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(1), new Vector3d(vertices.get(2)).sub(vertices.get(1)), new Vector3d(vertices.get(5)).sub(vertices.get(1)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(2), new Vector3d(vertices.get(3)).sub(vertices.get(2)), new Vector3d(vertices.get(6)).sub(vertices.get(2)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(7), new Vector3d(vertices.get(3)).sub(vertices.get(7)), new Vector3d(vertices.get(4)).sub(vertices.get(7)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(4), new Vector3d(vertices.get(0)).sub(vertices.get(4)), new Vector3d(vertices.get(5)).sub(vertices.get(4)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(6), new Vector3d(vertices.get(5)).sub(vertices.get(6)), new Vector3d(vertices.get(7)).sub(vertices.get(6)), world));
        ticker = TickersManager.getInstance().tickers.get(world.getName());
    }

    public DisplayHitbox(Vector3d origin, Matrix4f matrix, World world) {
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

        fragments.add(new ParallelogramHitboxFragment(vertices.get(0), new Vector3d(vertices.get(1)).sub(vertices.get(0)), new Vector3d(vertices.get(3)).sub(vertices.get(0)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(1), new Vector3d(vertices.get(2)).sub(vertices.get(1)), new Vector3d(vertices.get(5)).sub(vertices.get(1)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(2), new Vector3d(vertices.get(3)).sub(vertices.get(2)), new Vector3d(vertices.get(6)).sub(vertices.get(2)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(7), new Vector3d(vertices.get(3)).sub(vertices.get(7)), new Vector3d(vertices.get(4)).sub(vertices.get(7)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(4), new Vector3d(vertices.get(0)).sub(vertices.get(4)), new Vector3d(vertices.get(5)).sub(vertices.get(4)), world));
        fragments.add(new ParallelogramHitboxFragment(vertices.get(6), new Vector3d(vertices.get(5)).sub(vertices.get(6)), new Vector3d(vertices.get(7)).sub(vertices.get(6)), world));
        ticker = TickersManager.getInstance().tickers.get(world.getName());
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public void setOrigin(Vector3d newOrigin) {
        Vector3d diff = new Vector3d(newOrigin).sub(origin);
        fragments.forEach(f -> ((ParallelogramHitboxFragment) f).setOrigin(((ParallelogramHitboxFragment) f).getOrigin().add(diff)));
        origin = newOrigin;
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
    public List<Vector3d> collide(Entity box) {
        List<Vector3d> collisions = new ArrayList<>();
        fragments.forEach(f -> collisions.addAll(f.intersect(box.getBoundingBox())));
        return collisions;
    }

    @Override
    public void tick() {
        fragments.forEach(HitboxFragmentImpl::render);
    }

    @Override
    public void kill() {

    }

    //paralleloepiped is defined by 4 points, X,A,B,C, with basis vectors XA, XB, XC
    //XP = u * XA + v * XB + w * XC
    //Px - Xx = u * (Ax - Xx) + v * (Bx - Xx) + w * (Cx - Xx)
    //Py - Xy = u * (Ay - Xy) + v * (By - Xy) + w * (Cy - Xy)
    //Pz - Xz = u * (Az - Xz) + v * (Bz - Xz) + w * (Cz - Xz)
    //use cramer's rule to find solutions
    public long isInside(Vector3f P) {
        Vector3d X = new Vector3d(vertices.get(0));
        Vector3d A = new Vector3d(vertices.get(1));
        Vector3d B = new Vector3d(vertices.get(3));
        Vector3d C = new Vector3d(vertices.get(4));

        double d = new Matrix3d(
                (A.x - X.x), (A.y - X.y), (A.z - X.z),
                (B.x - X.x), (B.y - X.y), (B.z - X.z),
                (C.x - X.x), (C.y - X.y), (C.z - X.z)).determinant();
        double u = new Matrix3d(
                (P.x - X.x), (P.y - X.y), (P.z - X.z),
                (B.x - X.x), (B.y - X.y), (B.z - X.z),
                (C.x - X.x), (C.y - X.y), (C.z - X.z)).determinant() / d;
        if (u < 0 || u > 1) return 0;
        double w = new Matrix3d(
                (A.x - X.x), (A.y - X.y), (A.z - X.z),
                (P.x - X.x), (P.y - X.y), (P.z - X.z),
                (C.x - X.x), (C.y - X.y), (C.z - X.z)).determinant() / d;
        if (w < 0 || w > 1) return 0;
        double v = new Matrix3d(
                (A.x - X.x), (A.y - X.y), (A.z - X.z),
                (B.x - X.x), (B.y - X.y), (B.z - X.z),
                (P.x - X.x), (P.y - X.y), (P.z - X.z)).determinant() / d;
        return !(v < 0) && !(v > 1) ? 1 : 0;
    }

    public Pair<Vector3f, Vector3f> getBoundingBox() {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (Vector3d vertex : vertices) {
            min = new Vector3f((float) Math.min(min.x, vertex.x), (float) Math.min(min.y, vertex.y), (float) Math.min(min.z, vertex.z));
            max = new Vector3f((float) Math.max(max.x, vertex.x), (float) Math.max(max.y, vertex.y), (float) Math.max(max.z, vertex.z));
        }
        
        return Pair.of(min, max);
    }
}