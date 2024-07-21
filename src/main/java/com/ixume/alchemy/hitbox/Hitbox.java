package com.ixume.alchemy.hitbox;

import org.bukkit.entity.Entity;
import org.joml.Vector3d;

import java.util.List;

public interface Hitbox {
    //to collide with a hitbox, this hitbox needs access to all the fragments of the other hitbox
    List<Vector3d> collide(Hitbox hitbox);
    List<HitboxFragmentImpl> getFragments();

    List<Vector3d> collide(Entity entity);
}
