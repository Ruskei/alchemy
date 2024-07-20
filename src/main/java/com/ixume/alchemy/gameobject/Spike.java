package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.DisplayTransformation;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Spike implements GameObject {
    private static final int LENGTH = 8;
    private static final int SMOOTH_OFFSET = 2;
    private static final int GROUND_PADDING = 2;

    private final List<BlockDisplay> displays;
    private int progress;
    private final World world;
    private final Vector3d origin;
    private final BlockData blockData;
    private final Vector3f dir;
    private final Matrix4f defaultTransformationMatrix;

    public Spike(Vector3d origin, BlockData blockData, Player player) {
        displays = new ArrayList<>();
        progress = 0;
        float yaw = player.getYaw();
        world = player.getWorld();
        this.blockData = blockData;

        DisplayTransformation transformation = new DisplayTransformation();
        transformation.leftRotation.rotateY((float) (-yaw * Math.PI / 180f));
        transformation.leftRotation.rotateX((float) (50 * Math.PI / 180f));

        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
        v.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        v.mul(-1);
        transformation.translation.set(v);

        dir = new Vector3f(0f, -1f, 0f);
        dir.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        dir.mul(-1);

        this.origin = new Vector3d(origin.x - dir.x * SMOOTH_OFFSET, origin.y - dir.y * SMOOTH_OFFSET + GROUND_PADDING, origin.z - dir.z * SMOOTH_OFFSET);

        defaultTransformationMatrix = transformation.getMatrix();
    }

    private void spawnBlock() {
        BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x, origin.y, origin.z), BlockDisplay.class);
        blockDisplay.setBlock(blockData);
        blockDisplay.setInterpolationDuration(LENGTH - 1 - progress);
        blockDisplay.setInterpolationDelay(-1);

        blockDisplay.setTransformationMatrix(new Matrix4f(defaultTransformationMatrix).translateLocal(0, -GROUND_PADDING, 0));
        displays.add(blockDisplay);
    }

    private void updateBlocks() {
        BlockDisplay latest = displays.get(progress - 1);
        latest.setInterpolationDelay(-1);
        Transformation transformation = latest.getTransformation();
        transformation.getTranslation().add(new Vector3f(dir).mul(LENGTH - progress));
        latest.setTransformation(transformation);
    }

    @Override
    public void tick() {
        if (progress < LENGTH + 1) {
            if (progress < LENGTH) spawnBlock();
            if (progress > 0) updateBlocks();
        }

        progress++;
    }
}
