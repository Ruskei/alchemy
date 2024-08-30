package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.directionadjuster.RotatedDirectionAdjuster;
import com.ixume.alchemy.gameobject.virtualobjects.VirtualParallelepiped;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//bending objects consist of those with damage hitboxes, physical hitboxes, and *always* block displays
//can have one, the other, or both, probably not neither
//hitboxes are a singular box that moves along in some way
//this might be unpredictable; no fits-all just based on visual block information
//physical hitboxes are just simpler representations of visual block displays, not a fits-all either
//have singular provider that gives both of these
public class BendingObject/* implements GameObject, Hitbox */{
//    private static final int LINGER = 80;
//    private static final int SPEED = 5;
//    private static final int LIFE = 3;
//    private static final int SMOOTH_OFFSET = 1;
//    private static final int GROUND_PADDING = 5;
//
//    private int progress;
//    private final GameObjectTicker ticker;
//    private final Vector3d origin;
//    private final Vector3f dir;
//
//    private final DisplayHitbox hitbox;
//    private final Set<Integer> hitEntities;
//    private final VirtualParallelepiped physicalHitbox;
//
//    private final EarthbendingDisplayImpl earthbendingDisplay;
//
//    public BendingObject(Vector3f origin, Vector3f target, BlockData blockData, Player player) {
//        hitEntities = new HashSet<>();
//        progress = 0;
//        World world = player.getWorld();
//        ticker = TickersManager.getInstance().tickers.get(world.getName());
//        origin.sub(0, 0.5f, 0);
//
//        DisplayTransformation transformation = new DisplayTransformation();
//        Vector3f spikeDir = new Vector3f(target).sub(origin);
//        Vector3f IDENTITY = new Vector3f(0, 1f, 0);
//        transformation.leftRotation.rotateTo(IDENTITY, spikeDir);
//
//        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
//        v.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
//        v.mul(-1);
//        transformation.translation.set(v);
//
//        dir = new Vector3f(0f, -1f, 0f);
//        dir.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
//        dir.mul(-1);
//
//        this.origin = new Vector3d(origin.x - dir.x * (SMOOTH_OFFSET), origin.y - dir.y * (SMOOTH_OFFSET) + GROUND_PADDING, origin.z - dir.z * (SMOOTH_OFFSET));
//
//        Matrix4f defaultTransformationMatrix = transformation.getMatrix();
//
//        Matrix4f hitboxMatrix = new Matrix4f(defaultTransformationMatrix).translate(0.25f, 0, 0.25f).scale(0.5f, SPEED, 0.5f).translateLocal(0, -GROUND_PADDING, 0);
//        hitbox = new DisplayHitbox(this.origin, hitboxMatrix, world);
//
//        float factor = (float) (progress) / (SPEED * LIFE * 2) + 0.5f;
//        float offset = (1f - factor) / 2f;
//        physicalHitbox = new VirtualParallelepiped(this.origin, new Matrix4f(defaultTransformationMatrix).translate(offset, 0, (offset)).translateLocal(0f, -GROUND_PADDING, 0f).scale(0.5f,  LIFE * SPEED - SPEED, 0.5f), world, true);
//
//        List<VisualBlockDisplay> blockDisplays = new ArrayList<>();
//        //generate the mesh
//
//        for (int i = 0; i < SPEED * LIFE - SPEED; i++) {
//            float sizeFactor = ((float) (SPEED * LIFE - SPEED - i) / (SPEED * LIFE - SPEED)) + 0.5f;
//            blockDisplays.add(new VisualBlockDisplay(new Vector3f(0, i, 0), new Matrix4f().translate((1f - sizeFactor) / 2f - 0.5f, 0, (1f - sizeFactor) / 2f - 0.5f).scale(sizeFactor, 1, sizeFactor), blockData, RotatedDirectionAdjuster.getInstance()));
//        }
//
//        earthbendingDisplay = new EarthbendingDisplayImpl(world, origin, dir, LINGER, LIFE, 1.5f, blockDisplays);
//    }
//
//    @Override
//    public void tick() {
//        if (progress < LIFE + 1) {
//            if (progress < LIFE) {
//                hitbox.setOrigin(new Vector3d(origin).add(new Vector3d(dir).mul(progress * SPEED)));
//            }
//        }
//
//        if (progress > LIFE + LINGER) kill();
//
//        progress++;
//        earthbendingDisplay.tick();
//    }
//
//    @Override
//    public void kill() {
//        physicalHitbox.kill();
//        ticker.removeObject(this);
//    }
//
//    @Override
//    public List<Vector3d> collide(Hitbox hitbox) {
//        return this.hitbox.collide(hitbox);
//    }
//
//    @Override
//    public List<HitboxFragmentImpl> getFragments() {
//        return hitbox.getFragments();
//    }
//
//    @Override
//    public List<Vector3d> collide(Entity entity) {
//        final List<Vector3d> collisions = this.hitbox.collide(entity);
//        if (!hitEntities.contains(entity.getEntityId()) && progress < SPEED * LIFE + 1) {
//            if (!collisions.isEmpty()) {
//                if (entity instanceof LivingEntity livingEntity) {
//                    livingEntity.damage(20);
//                    hitEntities.add(entity.getEntityId());
//                }
//            }
//        }
//
//        return collisions;
//    }
}
