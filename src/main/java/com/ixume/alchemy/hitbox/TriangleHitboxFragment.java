package com.ixume.alchemy.hitbox;

import com.ixume.alchemy.Alchemy;
import org.bukkit.*;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3d;

import java.util.List;

public class TriangleHitboxFragment implements HitboxFragmentImpl {
    private final HitboxFragment fragment;

    public TriangleHitboxFragment(Vector3d[] vertices, Alchemy plugin) {
        Vector3d[] edges = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            edges[i] = new Vector3d(vertices[(i + 1) % 3]).sub(vertices[i]);
        }

        fragment = new HitboxFragment(vertices, edges, new Vector3d(edges[0]).cross(edges[1]).normalize());

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
    }

    public List<Vector3d> intersect(HitboxFragmentImpl hitboxFragment) {
       return fragment.intersect(hitboxFragment);
    }

    @Override
    public List<Vector3d> intersect(BoundingBox box) {
        return fragment.intersect(box, this);
    }

    public void render() {
        fragment.render();
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
        Vector3d p = new Vector3d(point);
        Vector3d a = new Vector3d(fragment.getVertices()[0]);
        Vector3d b = new Vector3d(fragment.getVertices()[1]);
        Vector3d c = new Vector3d(fragment.getVertices()[2]);

        a.sub(p);
        b.sub(p);
        c.sub(p);

        Vector3d u = new Vector3d(b).cross(c);
        Vector3d v = c.cross(a);
        Vector3d w = a.cross(b);

        if (v.dot(u) < 0.001f) return false;
        if (w.dot(u) < 0.001f) return false;

        return true;
    }
}
