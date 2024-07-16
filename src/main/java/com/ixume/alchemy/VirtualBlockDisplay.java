package com.ixume.alchemy;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class VirtualBlockDisplay {
    private List<Vector3d> vertices;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);

    public VirtualBlockDisplay(Location origin, Alchemy plugin) {
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

    public VirtualBlockDisplay(BlockDisplay blockDisplay, Alchemy plugin) {
        Vector3d originVector = blockDisplay.getLocation().toVector().toVector3d();
        vertices = new ArrayList<>();
        vertices.add(new Vector3d(0, 0, 0));
        vertices.add(new Vector3d(1, 0, 0));
        vertices.add(new Vector3d(1, 0, 1));
        vertices.add(new Vector3d(0, 0, 1));

        vertices.add(new Vector3d(0, 1, 0));
        vertices.add(new Vector3d(1, 1, 0));
        vertices.add(new Vector3d(1, 1, 1));
        vertices.add(new Vector3d(0, 1, 1));

        Transformation transformation = blockDisplay.getTransformation();
        Matrix3d matrix = new Matrix3d();
        transformation.getLeftRotation().get(matrix);

        vertices = vertices.stream().map(k ->
                k.mul(matrix).add(transformation.getTranslation()).add(originVector)).toList();

        Bukkit.getScheduler().runTaskTimer(plugin, this::render, 1, 1);
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
        for (double d = 0; d < 1d; d += 0.02) {
            Vector3d n = new Vector3d(a).add(new Vector3d(diff).mul(d));
            Location particleLocation = new Location(world, n.x, n.y, n.z);
            world.spawnParticle(Particle.DUST, particleLocation, 1, edgeDust);
        }
    }
}
