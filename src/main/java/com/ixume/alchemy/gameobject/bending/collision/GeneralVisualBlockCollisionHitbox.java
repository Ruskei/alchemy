package com.ixume.alchemy.gameobject.bending.collision;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import com.ixume.alchemy.gameobject.physical.Physical;
import com.ixume.alchemy.gameobject.virtualobjects.PhysicalHitbox;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.World;
import org.joml.Vector3f;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

public class GeneralVisualBlockCollisionHitbox implements GameObject, Physical {
    private final List<VisualBlockDisplay> adjustedBlockDisplays;
    private final List<Vector4d> shulkersToSpawn;
    private final List<Vector4d> spawnedShulkers;
    private final GameObjectTicker relevantTicker;

    private int progress;
    private final int life;
    private final int linger;
    private final Vector3f origin;
    private final Pair<Vector3f, Vector3f> boundingBox;
    private final float maxHeight;

    public GeneralVisualBlockCollisionHitbox(World world, Vector3f origin, int life, int linger, List<VisualBlockDisplay> adjustedDisplays) {
        progress = 0;
        relevantTicker = TickersManager.getInstance().tickers.get(world.getName());
        this.adjustedBlockDisplays = adjustedDisplays;
        PhysicalHitbox hitbox = new PhysicalHitbox(this, world, false);
        shulkersToSpawn = hitbox.shulkers;
        System.out.println("shulkers.size: " + shulkersToSpawn.size());
        spawnedShulkers = new ArrayList<>();
        this.life = life;
        this.linger = linger;
        this.origin = new Vector3f(origin);
        boundingBox = getBoundingBox();
        maxHeight = boundingBox.right().y - boundingBox.left().y;
    }

    @Override
    public void tick() {
        if (progress == life - 1) {
            spawnedShulkers.addAll(shulkersToSpawn);
            System.out.println("finalList.size: " + shulkersToSpawn.size());
            relevantTicker.proximityList.addAll(shulkersToSpawn);
            shulkersToSpawn.clear();
        } else if (progress < life - 1) {
            System.out.println("maxHeight: " + maxHeight);
            float acceptableDistance = (float) (progress) / (life - 1) * maxHeight;
            final List<Vector4d> tempList = new ArrayList<>();
            for (Vector4d shulker : shulkersToSpawn) {
//                float d = (float) Math.sqrt(shulker.x * shulker.x + shulker.y * shulker.y + shulker.z * shulker.z);
                float d = origin.distance((float) shulker.x, (float) shulker.y, (float) shulker.z);
                if (d <= acceptableDistance) tempList.add(shulker);
            }

            shulkersToSpawn.removeAll(tempList);

            relevantTicker.proximityList.addAll(tempList);
            System.out.println("tempList.size: " + tempList.size());

            spawnedShulkers.addAll(tempList);
        }

        progress++;
    }

    @Override
    public void kill() {
        for (Vector4d toRemove : spawnedShulkers) {
            relevantTicker.proximityList.remove(toRemove);
        }
    }

    @Override
    public long isInside(Vector3f isInside) {
        for (VisualBlockDisplay display : adjustedBlockDisplays) {
            if (display.isInside(isInside) == 1L) return 1L;
        }

        return 0L;
    }

    @Override
    public Pair<Vector3f, Vector3f> getBoundingBox() {
        if (boundingBox != null) return boundingBox;

        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (VisualBlockDisplay display : adjustedBlockDisplays) {
            Pair<Vector3f, Vector3f> boundingBox = display.getBoundingBox();
            min.min(boundingBox.left());
            max.max(boundingBox.right());
        }

        return Pair.of(min, max);
    }
}
