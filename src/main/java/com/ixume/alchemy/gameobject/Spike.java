package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Spike implements GameObject {
    private List<BlockDisplay> displays;
    private final float yaw;
    private int progress;
    private final World world;
    private final Vector3d origin;
    private final BlockData blockData;

    public Spike(Vector3d origin, BlockData blockData, Player player) {
        displays = new ArrayList<>();
        progress = 0;
        yaw = player.getYaw();
        world = player.getWorld();
        this.origin = origin;
        this.blockData = blockData;
    }

    private void spawnBlock() {
        BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x, origin.y, origin.z), BlockDisplay.class);
        blockDisplay.setBlock(blockData);

        DisplayTransformation displayTransformation = new DisplayTransformation();
        displayTransformation.leftRotation.rotateY((float) (-yaw * Math.PI / 180f));

        Vector3f v = new Vector3f(0.5f, 0.5f - progress, 0.5f);
        v.rotate(displayTransformation.leftRotation);
        v.mul(-1);
        displayTransformation.translation.set(v);

        Matrix4f matrix = displayTransformation.getMatrix();
        blockDisplay.setTransformationMatrix(matrix);
        displays.add(blockDisplay);
    }

    @Override
    public void tick() {
        final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
        world.spawnParticle(Particle.DUST, new Location(world, origin.x, origin.y, origin.z), 1, edgeDust);
        if (progress < 5) {
            spawnBlock();
        }

        progress++;
    }
}
