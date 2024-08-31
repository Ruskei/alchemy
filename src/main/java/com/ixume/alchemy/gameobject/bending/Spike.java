package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.collision.GeneralVisualBlockCollisionHitbox;
import com.ixume.alchemy.gameobject.bending.damage.LinearDamageHitbox;
import com.ixume.alchemy.gameobject.bending.directionadjuster.RotatedDirectionAdjuster;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

public class Spike implements GameObject, Hitbox {
    private static final int LINGER = 800;
    private static final int SPEED = 4;
    private static final int LIFE = 4;
    private static final int SMOOTH_OFFSET = 0;

    private int progress;
    private final GameObjectTicker ticker;

    private final LinearDamageHitbox hitbox;
    private final GeneralVisualBlockCollisionHitbox physicalHitbox;

    private final EarthbendingDisplayImpl earthbendingDisplay;

    public Spike(Vector3f spikeOrigin, Vector3f target, BlockData blockData, Player player) {
        progress = 0;
        World world = player.getWorld();
        ticker = TickersManager.getInstance().tickers.get(world.getName());
        spikeOrigin.sub(0, 0.5f, 0);

        DisplayTransformation transformation = new DisplayTransformation();
        Vector3f spikeDir = new Vector3f(target).sub(spikeOrigin);
        Vector3f IDENTITY = new Vector3f(0, 1f, 0);
        transformation.leftRotation.rotateTo(IDENTITY, spikeDir);

        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
        v.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        v.mul(-1);
        transformation.translation.set(v);

        Vector3f dir = new Vector3f(0f, -1f, 0f);
        dir.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        dir.mul(-1);

        Vector3f origin = new Vector3f(spikeOrigin.x - dir.x * (SMOOTH_OFFSET), spikeOrigin.y - dir.y * (SMOOTH_OFFSET), spikeOrigin.z - dir.z * (SMOOTH_OFFSET));

        Matrix4f defaultTransformationMatrix = transformation.getMatrix();

        Matrix4f hitboxMatrix = new Matrix4f(defaultTransformationMatrix).scale(1f, SPEED, 1f);
        hitbox = new LinearDamageHitbox(world, new Vector3f(origin), new Vector3f(dir), hitboxMatrix, SPEED, LIFE, LINGER, 20);

        List<VisualBlockDisplay> blockDisplays = new ArrayList<>();
        //generate the mesh
        for (int i = 0; i < SPEED * LIFE - SPEED; i++) {
            float sizeFactor = ((float) (SPEED * LIFE - SPEED - i) / (SPEED * LIFE - SPEED)) + 0.5f;
            blockDisplays.add(new VisualBlockDisplay(world, new Vector3f(), new Vector3f(new Random().nextFloat(-2, 2), i, new Random().nextFloat(-2, 2)), new Matrix4f().translate((1f - sizeFactor) / 2f - 0.5f, -0.5f, (1f - sizeFactor) / 2f - 0.5f).scale(sizeFactor, 1, sizeFactor), blockData, RotatedDirectionAdjuster.getInstance()));
        }

        blockDisplays.sort(new DescendingYSort());
        List<VisualBlockDisplay> adjustedBlockDisplays = new ArrayList<>();
        for (VisualBlockDisplay blockDisplay : blockDisplays) {
            adjustedBlockDisplays.add(new VisualBlockDisplay(world, blockDisplay.adjust(origin, dir), blockDisplay.relativeOrigin(), blockDisplay.adjust(dir), blockData, null));
        }

        physicalHitbox = new GeneralVisualBlockCollisionHitbox(world, origin, LIFE, LINGER, adjustedBlockDisplays);
        earthbendingDisplay = new EarthbendingDisplayImpl(world, spikeOrigin, dir, LINGER, LIFE, 0f, adjustedBlockDisplays);
    }

    @Override
    public void tick() {
        if (progress > LIFE + LINGER) kill();

        progress++;

        hitbox.tick();
        physicalHitbox.tick();
        earthbendingDisplay.tick();
    }

    @Override
    public void kill() {
        physicalHitbox.kill();
        hitbox.kill();
        earthbendingDisplay.kill();
        ticker.removeObject(this);
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
        return this.hitbox.collide(entity);
    }
}
