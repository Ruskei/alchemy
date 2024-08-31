package com.ixume.alchemy.gameobject.bending.damage;

import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import org.bukkit.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public record CompletableLinearDamageHitbox(World world, Vector3f origin, Vector3f dir, Matrix4f matrix, int speed, int life, int linger, int damage) implements DamageHitbox.CompletableDamageHitbox {
    @Override
    public DamageHitbox complete(List<VisualBlockDisplay> displays) {
        return new LinearDamageHitbox(world, origin, dir, matrix, speed, life, linger, damage);
    }
}
