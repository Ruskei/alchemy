package com.ixume.alchemy.gameobject.bending.damage;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.DisplayTransformation;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//defined by direction, dimensions, and life
//only really needs origin, world,  matrix and a direction to move
public class LinearDamageHitbox implements DamageHitbox {
    private final GameObjectTicker ticker;
    private final DisplayHitbox hitbox;
    private final Vector3f dir;
    private final Vector3f origin;
    private final int life;
    private final int linger;
    private final int speed;
    private final int damage;
    private int progress;
    private final Set<Integer> hitEntities;

    public LinearDamageHitbox(World world, Vector3f origin, Vector3f dir, Matrix4f matrix, int speed, int life, int linger, int damage) {
        this.origin = origin;
        progress = 0;
        this.dir = dir;
        this.speed = speed;
        this.life = life;
        this.linger = linger;

        DisplayTransformation transformation = new DisplayTransformation();
        Vector3f IDENTITY = new Vector3f(0, 1f, 0);
        transformation.leftRotation.rotateTo(IDENTITY, dir);

        Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
        v.rotate(transformation.rightRotation).rotate(transformation.leftRotation);
        v.mul(-1);
        transformation.translation.set(v);

        hitbox = new DisplayHitbox(new Vector3d(origin), transformation.getMatrix().mul(matrix), world);
        hitEntities = new HashSet<>();
        this.damage = damage;
        ticker = TickersManager.getInstance().tickers.get(world.getName());
        ticker.addObject(this);
    }

    @Override
    public void tick() {
        if (progress < life - 1) {
            hitbox.setOrigin(new Vector3d(origin).add(new Vector3f(dir).mul(progress * speed)));
        }

        if (progress == life + linger - 1) kill();

//        hitbox.tick();
        progress++;
    }

    @Override
    public void kill() {
        hitbox.kill();
        ticker.removeObject(this);
    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        return this.hitbox.collide(hitbox);
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return this.hitbox.getFragments();
    }

    @Override
    public List<Vector3d> collide(Entity entity) {
        final List<Vector3d> collisions = this.hitbox.collide(entity);
        if (!hitEntities.contains(entity.getEntityId()) && progress < life) {
            if (!collisions.isEmpty()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.damage(damage);
                    hitEntities.add(entity.getEntityId());
                }
            }
        }

        return this.hitbox.collide(entity);
    }
}
