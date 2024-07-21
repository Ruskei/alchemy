package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Spike implements GameObject, Hitbox {
    private static final int LINGER = 5;
//    private static final int LENGTH = 8;
    private static final int SPEED = 2;
    private static final int LIFE = 4;
    private static final int SMOOTH_OFFSET = 2;
    private static final int GROUND_PADDING = 5;

    private final List<BlockDisplay> displays;
    private int progress;
    private final World world;
    private final Vector3d origin;
    private final BlockData blockData;
    private final Vector3f dir;
    private final Matrix4f defaultTransformationMatrix;

    private final DisplayHitbox hitbox;
    private final Set<Integer> hitEntities;

    public Spike(Vector3f spikeOrigin, Vector3f target, BlockData blockData, Player player) {
        hitEntities = new HashSet<>();
        displays = new ArrayList<>();
        progress = 0;
        world = player.getWorld();
        this.blockData = blockData;
        spikeOrigin.sub(0, 0.5f, 0);

        DisplayTransformation transformation = new DisplayTransformation();
        Vector3f spikeDir = new Vector3f(target).sub(spikeOrigin);
        Vector3f IDENTITY = new Vector3f(0, 1f, 0);
        transformation.leftRotation.rotateTo(IDENTITY, spikeDir);

        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
        v.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        v.mul(-1);
        transformation.translation.set(v);

        dir = new Vector3f(0f, -1f, 0f);
        dir.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        dir.mul(-1);

        this.origin = new Vector3d(spikeOrigin.x - dir.x * SMOOTH_OFFSET, spikeOrigin.y - dir.y * SMOOTH_OFFSET + GROUND_PADDING, spikeOrigin.z - dir.z * SMOOTH_OFFSET);

        defaultTransformationMatrix = transformation.getMatrix();

        Matrix4f hitboxMatrix = new Matrix4f(this.defaultTransformationMatrix).translate(0.25f, 0, 0.25f).scale(0.5f, SPEED, 0.5f).translateLocal(0, -GROUND_PADDING, 0);
        hitbox = new DisplayHitbox(origin, hitboxMatrix);
    }

    private void spawn() {
        for (int i = 0; i < SPEED; i++) {
            BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x, origin.y, origin.z), BlockDisplay.class);
            blockDisplay.setBlock(blockData);
            blockDisplay.setInterpolationDuration(LIFE - 1 - progress);
            blockDisplay.setInterpolationDelay(-1);

            float factor = (float) (progress) / (SPEED * LIFE * 2) + 0.5f;
            float offset = (1f - factor) / 2f;
            blockDisplay.setTransformationMatrix(new Matrix4f(defaultTransformationMatrix).translate(offset, -i, (offset)).translateLocal(0, -GROUND_PADDING, 0).scale(factor, 1, factor));
            displays.add(blockDisplay);
        }
    }

    private void updateBlocks() {
        for (int i = 0; i < SPEED; i++) {
            BlockDisplay latest = displays.get(progress * SPEED - SPEED + i);
            latest.setInterpolationDelay(-1);
            Transformation transformation = latest.getTransformation();
            transformation.getTranslation().add(new Vector3f(dir).mul(SPEED * LIFE - progress));
            latest.setTransformation(transformation);
        }
    }

    @Override
    public void tick() {
        if (progress < LIFE + 1) {
            System.out.println("progress: " + progress + " max: " + (LIFE + 1));
            if (progress < LIFE) {
                spawn();
                hitbox.setOrigin(new Vector3d(origin).add(new Vector3d(dir).mul(progress * SPEED)));
            }

            if (progress > 0) updateBlocks();
        }

        if (progress > LIFE + LINGER) kill();

        progress++;
    }

    @Override
    public void kill() {
        displays.forEach(BlockDisplay::remove);
        GameObjectTicker.getInstance().removeObject(this);
    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        return this.hitbox.collide(hitbox);
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return hitbox.getFragments();
    }

    @Override
    public List<Vector3d> collide(Entity entity) {
        final List<Vector3d> collisions = this.hitbox.collide(entity);
        if (!hitEntities.contains(entity.getEntityId()) && progress < SPEED * LIFE + 1) {
            if (!collisions.isEmpty()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.damage(20);
                    hitEntities.add(entity.getEntityId());
                }
            }
        }

        return collisions;
    }
}
