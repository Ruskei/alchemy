package com.ixume.alchemy.gameobject.virtualobjects;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.physical.Physical;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.*;
import org.joml.Vector3f;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

public class PhysicalHitbox implements GameObject {
    private final Physical hitbox;
    private final GameObjectTicker myTicker;
    public List<Vector4d> shulkers;

    public PhysicalHitbox(Physical hitbox, World world, boolean selfInit) {
        this.hitbox = hitbox;

        if (selfInit) {
            myTicker = TickersManager.getInstance().tickers.get(world.getName());
            if (myTicker != null) {
                Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> TickersManager.getInstance().tickers.get("world").proximityList.addAll(binaryCubify()));
            } else {
                throw new NullPointerException();
            }
        } else {
            myTicker = null;
            binaryCubify();
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public void kill() {
        if (myTicker != null) {
            shulkers.forEach(s -> myTicker.getProximityList().remove(s));
            myTicker.removeObject(this);
        }
    }

    public List<Vector4d> binaryCubify() {
        long start = System.currentTimeMillis();
        Pair<Vector3f, Vector3f> boundingBox = hitbox.getBoundingBox();
        Vector3f min = boundingBox.left();
        Vector3f max = boundingBox.right();
        final float xDiff = max.x - min.x;
        final float yDiff = max.y - min.y;
        final float zDiff = max.z - min.z;
        float RESOLUTION = (float) Math.min(Math.max(Math.max(Math.max(xDiff, yDiff), zDiff) / 63d, 0.3d), 0.5);
        final int width = (int) Math.floor(xDiff / RESOLUTION);
        final int length = (int) Math.floor(zDiff / RESOLUTION);
        final int height = (int) Math.floor(yDiff / RESOLUTION);
        if (height > 63) throw new IndexOutOfBoundsException();

        long[] points = new long[(width + 1) * (length + 1)];

        //set the general positions && only choose those with potential to be cubes
        int positionIndex;
        for (int z = 0; z < length + 1; z++) {
            for (int x = 0; x < width + 1; x++) {
                positionIndex = z * width + x;
                for (int y = 0; y < height + 1; y++) {
                    points[positionIndex] |= (hitbox.isInside(new Vector3f(min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION)) << y);
                }

                points[positionIndex] &= (points[positionIndex] >> 1);
            }
        }


        long[] rawCubes = new long[width * length];
        //get actual cubes from positions
        int cubeIndex;
        for (int z = 0; z < length; z++) {
            for (int x = 0; x < width; x++) {
                cubeIndex = z * width + x;
                rawCubes[cubeIndex] = points[cubeIndex] | points[cubeIndex + 1] | points[cubeIndex + width] | points[cubeIndex + width + 1];
            }
        }

        //cull occluded
        long[] culledCubes = new long[width * length];
        System.arraycopy(rawCubes, 0, culledCubes, 0, culledCubes.length);

        int occlusionIndex;
        for (int z = 1; z < length - 1; z++) {
            for (int x = 1; x < width - 1; x++) {
                occlusionIndex = z * width + x;
                long column = rawCubes[occlusionIndex];
                culledCubes[occlusionIndex] = column & ~(column & (column >> 1) & (column << 1) & rawCubes[occlusionIndex - 1] & rawCubes[occlusionIndex + 1] & rawCubes[occlusionIndex + width] & rawCubes[occlusionIndex - width]);
            }
        }

        final List<Vector4d> shulkers = new ArrayList<>();

        int maxSize = (int) Math.floor(3d / RESOLUTION);

        int index;
        int index2;
        int index3;
        for (int z = 0; z < length; z++) {
            for (int x = 0; x < width; x++) {
                //find max raw size for each possible cube of this column first
                index = z * width + x;
                long rawColumn = rawCubes[index];
                if (rawColumn == 0) continue;
                //find first 1
                int y = Long.numberOfTrailingZeros(rawColumn);
                if (y > height) continue; //no set bits here
                while (y <= height) {
                    int h = Math.min(maxSize, Long.numberOfTrailingZeros(~(rawColumn >> y))); //height of possible cube
                    if (h == 0) break;
                    //check if this index
                    long culledColumn = culledCubes[index];
                    int size = 1; //start with 1 because starting with 0 is the same as this check ^
                    long usefulMask = culledColumn >> y;
                    long tempUsefulMask;
                    check:
                    while (size < h) {
                        if (x + size >= width || y + size >= height || z + size >= length) break;
                        //spiral square check, check NEXT size to see if it's viable
                        tempUsefulMask = 0L;
                        for (int x1 = 0; x1 <= size; x1++) {
                            index2 = index + (size * width) + x1;
                            long rawColumn2 = rawCubes[index2];
                            if (rawColumn2 == 0) break check;
                            int h2 = Long.numberOfTrailingZeros(~(rawColumn2 >> y));
                            if (h2 <= size) break check; //wasting this testing, can't get bigger
                            h = Math.min(h, h2);
                            //count useful size
                            tempUsefulMask |= culledCubes[index2] >> y;
                        }

                        for (int z1 = 0; z1 < size; z1++) {
                            index2 = index + (z1 * width) + size;
                            long rawColumn2 = rawCubes[index2];
                            if (rawColumn2 == 0) break check;
                            int h2 = Long.numberOfTrailingZeros(~(rawColumn2 >> y));
                            if (h2 <= size) break check; //wasting this testing, can't get bigger
                            h = Math.min(h, h2);
                            //count useful size
                            tempUsefulMask |= culledCubes[index2] >> y;
                        }

                        int j = Long.numberOfTrailingZeros(tempUsefulMask);
                        if (j <= size) usefulMask |= (1L << size);
                        usefulMask |= ((tempUsefulMask >> (size + 1)) << (size + 1));

                        size++;
                    }

                    usefulMask <<= (64 - size);
                    final int usefulSize = usefulMask == 0 ? 0 : size - Long.numberOfLeadingZeros(usefulMask);

                    if (usefulSize == 0) {
                        y++;
                        continue;
                    }
                    //clear out used cubes
                    if (usefulSize == 1) {
                        culledCubes[index] &= ~(1L << y);
                    }

                    if (usefulSize > 1) {
                        final long mask = ~((Long.MAX_VALUE >> (63 - usefulSize)) << y);

                        for (int x1 = 0; x1 < usefulSize; x1++) {
                            for (int z1 = 0; z1 < usefulSize; z1++) {
                                index3 = index + (z1 * width) + x1;
                                culledCubes[index3] &= mask;
                            }
                        }
                    }

                    shulkers.add(new Vector4d(min.x + x * RESOLUTION + usefulSize * RESOLUTION / 2d, min.y + y * RESOLUTION, min.z + z * RESOLUTION + usefulSize * RESOLUTION / 2d, usefulSize * RESOLUTION));

                    y++;
                }
            }
        }

        this.shulkers = shulkers;
        System.out.println("mesh time: " + (System.currentTimeMillis() - start) + "ms");
        return shulkers;
    }
}
