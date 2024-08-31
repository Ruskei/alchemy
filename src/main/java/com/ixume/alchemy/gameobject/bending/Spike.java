package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.collision.GeneralVisualBlockCollisionHitbox;
import com.ixume.alchemy.gameobject.bending.collision.PhysicalHitbox;
import com.ixume.alchemy.gameobject.bending.collision.UncompletedGeneralVisualBlockCollisionHitbox;
import com.ixume.alchemy.gameobject.bending.damage.DamageHitbox;
import com.ixume.alchemy.gameobject.bending.damage.LinearDamageHitbox;
import com.ixume.alchemy.gameobject.bending.damage.UncompletedLinearDamageHitbox;
import com.ixume.alchemy.gameobject.bending.directionadjuster.RotatedDirectionAdjuster;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
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

    private final GameObjectTicker ticker;
    private final BendingObject bendingObject;

    public Spike(Vector3f spikeOrigin, Vector3f target, BlockData blockData, Player player) {
        World world = player.getWorld();
        ticker = TickersManager.getInstance().tickers.get(world.getName());
        spikeOrigin.sub(0, 0.5f, 0);

        Vector3f dir = new Vector3f(target).sub(spikeOrigin).normalize();
        Vector3f origin = new Vector3f(spikeOrigin.x - dir.x * (SMOOTH_OFFSET), spikeOrigin.y - dir.y * (SMOOTH_OFFSET), spikeOrigin.z - dir.z * (SMOOTH_OFFSET));

        Matrix4f hitboxMatrix = new Matrix4f().scale(1f, SPEED, 1f);
        DamageHitbox.UncompletedDamageHitbox hitbox = new UncompletedLinearDamageHitbox(world, new Vector3f(origin), new Vector3f(dir), hitboxMatrix, SPEED, LIFE, LINGER, 20);

        List<VisualBlockDisplay> blockDisplays = new ArrayList<>();

        for (int i = 0; i < SPEED * LIFE - SPEED; i++) {
            float sizeFactor = ((float) (SPEED * LIFE - SPEED - i) / (SPEED * LIFE - SPEED)) + 0.5f;
            blockDisplays.add(new VisualBlockDisplay(world, new Vector3f(), new Vector3f(0, i, 0), new Matrix4f().translate((1f - sizeFactor) / 2f - 0.5f, -0.5f, (1f - sizeFactor) / 2f - 0.5f).scale(sizeFactor, 1, sizeFactor), blockData, RotatedDirectionAdjuster.getInstance()));
        }

        blockDisplays.sort(new DescendingYSort());
        PhysicalHitbox.UncompletedPhysicalHitbox physicalHitbox = new UncompletedGeneralVisualBlockCollisionHitbox(world, origin, LIFE);
        bendingObject = new BendingObject(origin, dir, LIFE, LINGER, blockDisplays, world, physicalHitbox, hitbox);
    }

    @Override
    public void tick() {
//        bendingObject.tick();
    }

    @Override
    public void kill() {
        bendingObject.kill();
        ticker.removeObject(this);
    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        return bendingObject.collide(hitbox);
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return bendingObject.getFragments();
    }

    @Override
    public List<Vector3d> collide(Entity entity) {
        return bendingObject.collide(entity);
    }
}
