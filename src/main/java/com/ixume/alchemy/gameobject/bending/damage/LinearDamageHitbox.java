package com.ixume.alchemy.gameobject.bending.damage;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

//defined by direction, dimensions, and life
//only really needs origin, world,  matrix and a direction to move
public class LinearDamageHitbox implements GameObject, Hitbox {
    private final DisplayHitbox hitbox;
    private final Vector3f dir;
    private final Vector3f origin;
    private final int life;
    private final int linger;
    private final int speed;
    private int progress;

    public LinearDamageHitbox(World world, Vector3f origin, Vector3f dir, Matrix4f matrix, int speed, int life, int linger) {
        this.origin = origin;
        progress = 0;
        this.dir = dir;
        this.speed = speed;
        this.life = life;
        this.linger = linger;
        hitbox = new DisplayHitbox(new Vector3d(origin), matrix, world);
    }

    @Override
    public void tick() {
        if (progress < life - 1) {
//            System.out.println("hitbox origin: " + hitbox.getOrigin());
            hitbox.setOrigin(new Vector3d(origin).add(new Vector3f(dir).mul(progress * speed)));
        }

        if (progress == life + linger - 1) kill();

        hitbox.tick();
        progress++;
    }

    @Override
    public void kill() {
        hitbox.kill();
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
        return this.hitbox.collide(entity);
    }
}
