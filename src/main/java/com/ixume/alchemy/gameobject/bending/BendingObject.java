package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.collision.PhysicalHitbox;
import com.ixume.alchemy.gameobject.bending.damage.DamageHitbox;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

//bending objects consist of those with damage hitboxes, physical hitboxes, and *always* block displays
//can have one, the other, or both, probably not neither
//hitboxes are a singular box that moves along in some way
//this might be unpredictable; no fits-all just based on visual block information
//physical hitboxes are just simpler representations of visual block displays, not a fits-all either
//have singular provider that gives both of these
public class BendingObject implements GameObject, Hitbox {
    private int progress;
    private final int life;
    private final int linger;
    private final GameObjectTicker ticker;

    private final DamageHitbox damageHitbox;
    private final PhysicalHitbox physicalHitbox;

    private final EarthbendingDisplayImpl earthbendingDisplay;

    public BendingObject(Vector3f origin, Vector3f dir, int life, int linger, List<VisualBlockDisplay> blockDisplays, World world, PhysicalHitbox.CompletablePhysicalHitbox completablePhysicalHitbox, DamageHitbox.CompletableDamageHitbox uncompletedDamageHitbox) {
        progress = 0;
        this.life = life;
        this.linger = linger;
        ticker = TickersManager.getInstance().tickers.get(world.getName());

        blockDisplays.sort(new DescendingYSort());
        List<VisualBlockDisplay> adjustedBlockDisplays = new ArrayList<>();
        for (VisualBlockDisplay blockDisplay : blockDisplays) {
            adjustedBlockDisplays.add(new VisualBlockDisplay(world, blockDisplay.adjust(origin, dir), blockDisplay.relativeOrigin(), blockDisplay.adjust(dir), blockDisplay.displayData(), null));
        }

        physicalHitbox = completablePhysicalHitbox.complete(adjustedBlockDisplays);
        damageHitbox = uncompletedDamageHitbox.complete(adjustedBlockDisplays);
        earthbendingDisplay = new EarthbendingDisplayImpl(world, origin, dir, linger, life, 0f, adjustedBlockDisplays);
        ticker.addObject(this);
    }

    @Override
    public void tick() {
        if (progress > life + linger) kill();

        progress++;
    }

    @Override
    public void kill() {
        physicalHitbox.kill();
        damageHitbox.kill();
        earthbendingDisplay.kill();
        ticker.removeObject(this);
    }

    @Override
    public List<Vector3d> collide(Hitbox hitbox) {
        return this.damageHitbox.collide(hitbox);
    }

    @Override
    public List<HitboxFragmentImpl> getFragments() {
        return damageHitbox.getFragments();
    }

    @Override
    public List<Vector3d> collide(Entity entity) {
        return this.damageHitbox.collide(entity);
    }
}
