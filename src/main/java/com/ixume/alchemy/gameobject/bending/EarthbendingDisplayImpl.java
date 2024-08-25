package com.ixume.alchemy.gameobject.bending;

import com.ixume.alchemy.gameobject.GameObject;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EarthbendingDisplayImpl implements GameObject {
    final static Vector3f IDENTITY = new Vector3f(0, 1f, 0);

    private int LINGER;
    private int LIFE;

    private int GROUND_PADDING;
    private int SMOOTH_OFFSET;

    private final World world;
    private final Vector3f origin;
    private final Vector3f dir;
    private final float maxY;
    private final List<VisualBlockDisplay> blockDisplays;
    private final List<Pair<VisualBlockDisplay, BlockDisplay>> needyBlockDisplays;
    private int progress;

    public EarthbendingDisplayImpl(World world, Vector3f origin, Vector3f dir, int linger, int life, List<VisualBlockDisplay> blockDisplays) {
        this.world = world;
        this.origin = origin;
        this.dir = dir.normalize();
        progress = 0;
        LINGER = linger;
        LIFE = life;
        if (blockDisplays.isEmpty()) throw new NullPointerException("empty array!");
        this.blockDisplays = blockDisplays;
        needyBlockDisplays = new ArrayList<>();
        blockDisplays.sort(new DescendingYSort());
        maxY = blockDisplays.getFirst().origin().y;
    }

    public void setGROUND_PADDING(int GROUND_PADDING) {
        this.GROUND_PADDING = GROUND_PADDING;
    }

    public void setSMOOTH_OFFSET(int SMOOTH_OFFSET) {
        this.SMOOTH_OFFSET = SMOOTH_OFFSET;
    }

    private void spawn() {
        float factor = (1f - (float) (progress) / LIFE) * maxY;
        while (!blockDisplays.isEmpty() && blockDisplays.get(0).origin().y >= factor) {
            VisualBlockDisplay visualBlockDisplay = blockDisplays.getFirst();
            //valid block display to spawn
            //spawn w/ relative y = 0, moves to its own relativeY
            Vector3f relativePosition = new Vector3f(visualBlockDisplay.origin().x, 0, visualBlockDisplay.origin().z).rotate(new Quaternionf().rotateTo(IDENTITY, dir));
            BlockDisplay blockDisplay = world.spawn(new Location(world, origin.x + relativePosition.x, origin.y + relativePosition.y, origin.z + relativePosition.z), BlockDisplay.class);
            blockDisplay.setBlock(visualBlockDisplay.displayData());
            blockDisplay.setInterpolationDuration(LIFE - 1 - progress);
            blockDisplay.setInterpolationDelay(-1);
            Matrix4f rotatedMatrix = new Matrix4f(visualBlockDisplay.transformationMatrix());
            Quaternionf temp = new Quaternionf();
            temp.rotateTo(IDENTITY, dir);
            rotatedMatrix.rotate(temp);
            blockDisplay.setTransformationMatrix(rotatedMatrix);
            needyBlockDisplays.add(Pair.of(visualBlockDisplay, blockDisplay));
            blockDisplays.removeFirst();
        }
    }

    private void updateMovement() {
        //needy block displays are from last tick
        System.out.println(needyBlockDisplays.size());
        for (Pair<VisualBlockDisplay, BlockDisplay> needyBlockDisplayPair : needyBlockDisplays) {
            Transformation transformation = needyBlockDisplayPair.right().getTransformation();
            transformation.getTranslation().add(new Vector3f(dir).mul(needyBlockDisplayPair.left().origin().y));
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

            //1 extra tick for updates to update the last spawned stuff
        }

        if (progress > LIFE + LINGER) kill();

        progress++;
    }

    @Override
    public void kill() {

    }
}

class DescendingYSort implements Comparator<VisualBlockDisplay> {
    @Override
    public int compare(VisualBlockDisplay o1, VisualBlockDisplay o2) {
        float diff = (o2.origin().y - o1.origin().y);
        return (diff == 0 ? 0 : (diff > 0 ? 1 : -1));
    }
}