package com.ixume.alchemy.gameobject.bending.damage;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import com.ixume.alchemy.hitbox.Hitbox;

import java.util.List;

public interface DamageHitbox extends GameObject, Hitbox {
    interface UncompletedDamageHitbox {
        DamageHitbox complete(List<VisualBlockDisplay> displays);
    }
}
