package com.ixume.alchemy.gameobject.bending.data;

//provides a proper moving damage hitbox object
//provides a moving moving physical collision object
//takes in the data neede to create them
//origin, direction, (target), (thickness)
public interface BendingObjectObjectsFactory {
    BendingObjectData getDamageHitbox(Record dataObject);
}
