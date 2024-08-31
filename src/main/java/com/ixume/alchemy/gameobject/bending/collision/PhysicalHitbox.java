package com.ixume.alchemy.gameobject.bending.collision;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import com.ixume.alchemy.gameobject.physical.Physical;

import java.util.List;

public interface PhysicalHitbox extends GameObject, Physical {
    interface UncompletedPhysicalHitbox {
        PhysicalHitbox complete(List<VisualBlockDisplay> displays);
    }
}
