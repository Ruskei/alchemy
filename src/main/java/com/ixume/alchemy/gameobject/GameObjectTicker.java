package com.ixume.alchemy.gameobject;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.hitbox.Hitbox;
import com.ixume.alchemy.hitbox.HitboxFragmentImpl;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class GameObjectTicker {
    private volatile List<GameObject> objects;
    private volatile List<GameObject> objectsToAdd;
    private volatile List<GameObject> objectsToRemove;

    private static GameObjectTicker INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) {
            INSTANCE = new GameObjectTicker(plugin);
        }
    }

    public static GameObjectTicker getInstance() {return INSTANCE;}

    private GameObjectTicker(Alchemy plugin) {
        objects = new ArrayList<>();
        objectsToAdd = new ArrayList<>();
        objectsToRemove = new ArrayList<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            GameObjectTicker.INSTANCE.objects.stream().filter(o -> o instanceof Hitbox).map(o -> ((Hitbox) o)).forEach(o -> o.getFragments().forEach(HitboxFragmentImpl::render));
            tick();
        }, 10, 1);
    }

    private void tick() {
        World world = Bukkit.getWorld("world");
        for (GameObject object : objects) {
            object.tick();
            if (object instanceof Hitbox hitbox) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType().equals(EntityType.BLOCK_DISPLAY)) continue;
                    hitbox.collide(entity);
                }

                for (GameObject object2 : objects) {
                    if (object == object2) continue;
                    if (object2 instanceof Hitbox hitbox2) {
                        hitbox.collide(hitbox2);
                    }
                }
            }
        }

        GameObjectTicker.getInstance().objects.removeAll(GameObjectTicker.getInstance().objectsToRemove);
        GameObjectTicker.getInstance().objectsToRemove.clear();
        GameObjectTicker.getInstance().objects.addAll(GameObjectTicker.getInstance().objectsToAdd);
        GameObjectTicker.getInstance().objectsToAdd.clear();
    }

    public void addObject(GameObject gameObject) {
        objectsToAdd.add(gameObject);
    }
    public void removeObject(GameObject gameObject) {
        objectsToRemove.add(gameObject);
    }
}
