package com.ixume.alchemy.gameobject.bending.data;

import com.ixume.alchemy.gameobject.GameObject;
//damage hitbox needs to have a shape, and a path to move; path is usually straight and thus only defined by life and distance
public record BendingObjectData(GameObject damageHitbox, GameObject collisionHitbox) {
}
