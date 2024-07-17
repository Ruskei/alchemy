package com.ixume.alchemy;

import java.util.ArrayList;
import java.util.List;

public class TriangleTestGameObject implements GameObject {
    private final List<Hitbox> hitbox;
    public TriangleTestGameObject(TriangleHitbox singletonHitbox) {
        hitbox = new ArrayList<>();
        hitbox.add(singletonHitbox);
    }
    @Override
    public List<Hitbox> getHitboxes() {
        return hitbox;
    }
}
