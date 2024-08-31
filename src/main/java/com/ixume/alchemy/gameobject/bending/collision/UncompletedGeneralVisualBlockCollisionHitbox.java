package com.ixume.alchemy.gameobject.bending.collision;

import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import org.bukkit.World;
import org.joml.Vector3f;

import java.util.List;

public record UncompletedGeneralVisualBlockCollisionHitbox(World world, Vector3f origin, int life) implements PhysicalHitbox.UncompletedPhysicalHitbox {
    @Override
    public PhysicalHitbox complete(List<VisualBlockDisplay> displays) {
        return new GeneralVisualBlockCollisionHitbox(world, origin, life, displays);
    }
}
