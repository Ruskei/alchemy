package com.ixume.alchemy.hitbox;

import com.ixume.alchemy.Alchemy;
import org.bukkit.util.BoundingBox;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.List;

public class ParallelogramHitboxFragment implements HitboxFragmentImpl {
    private final HitboxFragment fragment;
    private final Vector3d pointC;
    private final Vector3d pointB;
    private final Vector3d origin;
    private Vector3d testVector;
    public ParallelogramHitboxFragment(Vector3d origin, Vector3d edge1, Vector3d edge2) {
        testVector = new Vector3d();
        this.origin = new Vector3d(origin);
        this.pointC = new Vector3d(origin).add(edge1);
        this.pointB = new Vector3d(origin).add(edge2);
        Vector3d[] edges = new Vector3d[]{new Vector3d(edge1).mul(-1), edge2};
        Vector3d[] vertices = new Vector3d[]{new Vector3d(origin).add(edge1), new Vector3d(origin)};
        fragment = new HitboxFragment(vertices, edges, new Vector3d(edge1).cross(edge2));
    }

    @Override
    public Vector3d planePoint() {
        return fragment.planePoint();
    }

    @Override
    public Vector3d normal() {
        return fragment.normal();
    }

    @Override
    public boolean inside(Vector3d point) {
//        System.out.println("point: " + point);
//        System.out.println("origin: " + origin);
//        System.out.println("b: " + pointB);
//        System.out.println("c: " + pointC);
        Matrix3d a = new Matrix3d(
                pointB.x - origin.x, pointB.y - origin.y, pointB.z - origin.z,
                pointC.x - origin.x, pointC.y - origin.y, pointC.z - origin.z,
                (pointB.y - origin.y) * (pointC.z - origin.z) - (pointC.y - origin.y) * (pointB.z - origin.z), 1, (pointB.x - origin.x) * (pointC.y - origin.y) - (pointC.x - origin.x) * (pointB.y - origin.y));
        a.invert();
        Vector3d b = new Vector3d(point).sub(origin);

        b.mul(a);

        return (0 <= b.x && b.x <= 1 && 0 <= b.y && b.y <= 1);
    }

    @Override
    public List<Vector3d> intersect(HitboxFragmentImpl hitboxFragment) {
        return fragment.intersect(hitboxFragment);
    }

    @Override
    public List<Vector3d> intersect(BoundingBox box) {
        return fragment.intersect(box,this);
    }

    @Override
    public void render() {
        fragment.render();
    }
}
