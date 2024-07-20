package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.Location;
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

import java.util.List;

public class Spike implements GameObject {
    public Spike(Vector3d origin, BlockData blockData, Player player) {
        World world = player.getWorld();
        BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x, origin.y, origin.z), BlockDisplay.class);
        blockDisplay.setBlock(blockData);

        DisplayTransformation displayTransformation = new DisplayTransformation();
        displayTransformation.leftRotation.rotateY((float) (-player.getYaw() * Math.PI / 180f));

        displayTransformation.scale = new Vector3f(1, 5, 1);

        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
        v.rotate(displayTransformation.leftRotation);
        v.mul(-1);
        displayTransformation.translation.set(v);

        Matrix4f matrix = displayTransformation.getMatrix();
        blockDisplay.setTransformationMatrix(matrix);
    }
}
