package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//depends on adjusted block displays but also needs their respective y values
public class EarthbendingDisplayImpl implements GameObject {
    public final static Vector3f IDENTITY = new Vector3f(0, 1f, 0);

    private final int LINGER;
    private final int LIFE;

    private final World world;
    private final Vector3f origin;
    private final Vector3f dir;
    private final float maxY;
    private final List<VisualBlockDisplay> adjustedBlockDisplays;
    private final List<Pair<VisualBlockDisplay, BlockDisplay>> needyBlockDisplays;
    private final List<BlockDisplay> entities;
    private final GameObjectTicker ticker;
    private final Quaternionf rotationQuaternion;
    private final Vector3i VISIBILITY_OFFSET;
    private int progress;

    public EarthbendingDisplayImpl(World world, Vector3f origin, Vector3f dir, int linger, int life, float smoothOffset, List<VisualBlockDisplay> adjustedBlockDisplays) {
        this.world = world;
        this.ticker = TickersManager.getInstance().tickers.get(world.getName());
        this.dir = dir.normalize();
        this.origin = new Vector3f(origin);
        progress = 0;
        LINGER = linger;
        LIFE = life;
        if (adjustedBlockDisplays.isEmpty()) throw new NullPointerException("empty array!");
        this.adjustedBlockDisplays = adjustedBlockDisplays;
        needyBlockDisplays = new ArrayList<>();
        entities = new ArrayList<>();
        maxY = adjustedBlockDisplays.getFirst().relativeOrigin().y;
        rotationQuaternion = new Quaternionf().rotateTo(IDENTITY, dir);

        VISIBILITY_OFFSET = findTransparentBlock(this.origin);
    }

    private static final int SEARCH_SIZE = 2;

    private Vector3i findTransparentBlock(Vector3f base) {
        for (int x = -SEARCH_SIZE; x <= SEARCH_SIZE; x++) {
            for (int y = -SEARCH_SIZE; y <= SEARCH_SIZE; y++) {
                for (int z = -SEARCH_SIZE; z <= SEARCH_SIZE; z++) {
                    if (!world.getBlockAt((int) Math.floor(base.x + x), (int) Math.floor(base.y + y), (int) Math.floor(base.z + z)).isSolid()) {
                        return new Vector3i(x, y, z);
                    }
                }
            }
        }

        return new Vector3i(0, 0, 0);
    }

    private void spawn() {
        float factor = (1f - (float) (progress + 1) / LIFE) * maxY;
        while (!adjustedBlockDisplays.isEmpty() && adjustedBlockDisplays.get(0).relativeOrigin().y >= factor) {
            VisualBlockDisplay adjustedBlockDisplay = adjustedBlockDisplays.getFirst();
            //valid block display to spawn
            //spawn w/ relative y = 0, moves to its own relativeY
            Vector3f relativePosition = new Vector3f(adjustedBlockDisplay.relativeOrigin().x, 0, adjustedBlockDisplay.relativeOrigin().z).rotate(rotationQuaternion);
            BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x + VISIBILITY_OFFSET.x, origin.y + VISIBILITY_OFFSET.y, origin.z + VISIBILITY_OFFSET.z), BlockDisplay.class);
            blockDisplay.setBlock(adjustedBlockDisplay.displayData());
            blockDisplay.setInterpolationDuration(LIFE - 1 - progress);
            blockDisplay.setInterpolationDelay(-1);
            blockDisplay.setTeleportDuration(LIFE - progress);

            Matrix4f rotatedMatrix = adjustedBlockDisplay.transformationMatrix();
            rotatedMatrix.translateLocal(relativePosition.x - VISIBILITY_OFFSET.x, relativePosition.y - VISIBILITY_OFFSET.y, relativePosition.z - VISIBILITY_OFFSET.z);

            blockDisplay.setTransformationMatrix(rotatedMatrix);
            needyBlockDisplays.add(Pair.of(adjustedBlockDisplay, blockDisplay));
            adjustedBlockDisplays.removeFirst();

            entities.add(blockDisplay);
        }
    }

    private void updateMovement() {
        //needy block displays are from last tick
        for (Pair<VisualBlockDisplay, BlockDisplay> needyBlockDisplayPair : needyBlockDisplays) {
            Transformation transformation = needyBlockDisplayPair.right().getTransformation();
            transformation.getTranslation().add(new Vector3f(dir).mul(needyBlockDisplayPair.left().relativeOrigin().y));
            needyBlockDisplayPair.right().setInterpolationDelay(-1);
            needyBlockDisplayPair.right().setTransformation(transformation);
        }

        needyBlockDisplays.clear();
    }

    @Override
    public void tick() {
        //only spawn while still alive
        //LIFE = how long spawning takes
        //yMAX is the highest y value of the visual blocks. minimum value is 0
        //each tick the remaining block entities which have a yvalue <= progress / LIFE * yMAX
        if (progress <= LIFE) {
            if (progress > 0) updateMovement();

            if (progress < LIFE) {
                spawn();
            }

//            1 extra tick for updates to update the last spawned stuff
        }

        if (progress > LIFE + LINGER) kill();

        progress++;
    }

    @Override
    public void kill() {
        for (BlockDisplay entity : entities) {
            entity.remove();
        }
        ticker.removeObject(this);
    }
}

class DescendingYSort implements Comparator<VisualBlockDisplay> {
    @Override
    public int compare(VisualBlockDisplay o1, VisualBlockDisplay o2) {
        float diff = (o2.relativeOrigin().y - o1.relativeOrigin().y);
        return (diff == 0 ? 0 : (diff > 0 ? 1 : -1));
    }
}