package com.ixume.alchemy.gameobject.virtualobjects;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.physical.Physical;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.*;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

public class VirtualParallelepiped implements GameObject, Physical {
    private double RESOLUTION;
    private final DisplayHitbox hitbox;
    private final GameObjectTicker myTicker;
    private List<Vector4d> shulkers;

    public VirtualParallelepiped(Vector3d origin, Transformation transformation, World world, boolean binary) {
        hitbox = new DisplayHitbox(origin, transformation, world);

        myTicker = TickersManager.getInstance().tickers.get(world.getName());
        if (myTicker != null) {
            if (binary) {
                Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> TickersManager.getInstance().tickers.get("world").proximityList.addAll(binaryCubify()));
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> TickersManager.getInstance().tickers.get("world").proximityList.addAll(naiveCubify()));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public VirtualParallelepiped(Vector3d origin, Matrix4f transformation, World world, boolean binary) {
        hitbox = new DisplayHitbox(origin, transformation, world);

        myTicker = TickersManager.getInstance().tickers.get(world.getName());
        if (myTicker != null) {
            if (binary) {
                Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> TickersManager.getInstance().tickers.get("world").proximityList.addAll(binaryCubify()));
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> TickersManager.getInstance().tickers.get("world").proximityList.addAll(naiveCubify()));
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void tick() {
        hitbox.tick();
    }

    @Override
    public void kill() {
        shulkers.forEach(s -> myTicker.getProximityList().remove(s));
        myTicker.removeObject(this);
    }

    public List<Vector4d> binaryCubify() {
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        final double xDiff = max.x - min.x;
        final double yDiff = max.y - min.y;
        final double zDiff = max.z - min.z;
        RESOLUTION = Math.min(Math.max(Math.max(Math.max(xDiff, yDiff), zDiff) / 63d, 0.1d), 0.5);
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
                    points[positionIndex] |= (hitbox.inInsideLong(new Vector3d(min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION)) << y);
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
                rawCubes[cubeIndex] = points[cubeIndex] & points[cubeIndex + 1] & points[cubeIndex + width] & points[cubeIndex + width + 1];
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
        return shulkers;
    }

    public List<Vector4d> naiveCubify() {
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        int width = (int) ((max.x - min.x) / RESOLUTION);
        int length = (int) ((max.z - min.z) / RESOLUTION);
        int height = (int) ((max.y - min.y) / RESOLUTION);
//        create 3d
        boolean[] raw = getRaw(min, width, height, length);
        boolean[] rawCubes = getCubes(raw, width, height, length);
        boolean[] occludedCubes = rawCubes.clone();


        List<Integer> occluded = new ArrayList<>();
        for (int y = 1; y < height - 1; y++) {
            for (int z = 1; z < length - 1; z++) {
                for (int x = 1; x < width - 1; x++) {
                    int i = x + (z * width) + (y * width * length);
                    if (rawCubes[i]) {
                        if (convolute(x, y, z, width, length, rawCubes)) {
                            occluded.add(i);
                        }
                    }
                }
            }
        }

        for (Integer i : occluded) {
            occludedCubes[i] = false;
        }


        List<Vector4d> shulkers = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    //check if this position is inside the shape
                    int index = x + (z * width) + (y * width * length);
                    if (!rawCubes[index]) continue;
                    //find largest cube now,
                    int testSize = 0;
                    int usefulSize = 0;
                    check:
                    for (int e = 0; e < Math.floor(3d / RESOLUTION); e++) {
                        if (x + testSize >= width || y + testSize >= height || z + testSize >= length) break;
                        boolean useful = false;
                        //pretend size is 1 more than it is, then if that's fine, move on, if it's not, leave
                        //if any of these are false, then the cube cannot be formed
                        for (int j = y; j <= y + testSize; j++) {
                            for (int i = x; i <= x + testSize; i++) {
                                int index2 = i + ((z + testSize) * width) + (j * width * length);
                                if (!rawCubes[index2]) break check;
                                if (!useful && occludedCubes[index2]) {
                                    useful = true;
                                    break;
                                }
                            }
                        }

                        for (int j = y; j <= y + testSize; j++) {
                            for (int i = z; i < z + testSize; i++) {
                                int index2 = x + testSize + (i * width) + (j * width * length);
                                if (!rawCubes[index2]) break check;
                                if (!useful && occludedCubes[index2]) {
                                    useful = true;
                                    break;
                                }
                            }
                        }

                        for (int j = z; j < z + testSize; j++) {
                            for (int i = x; i < x + testSize; i++) {
                                int index2 = i + (j * width) + ((y + testSize) * width * length);
                                if (!rawCubes[index2]) break check;
                                if (!useful && occludedCubes[index2]) {
                                    useful = true;
                                    break;
                                }
                            }
                        }

//                        if (!useful) break;
                        testSize++;
                        if (useful) usefulSize = testSize;
                    }

//                    usefulSize = testSize;

                    if (usefulSize > 0) {
                        //hollow out the cube
//                        if (usefulSize > 1) {
                        for (int y1 = y; y1 < y + usefulSize; y1++) {
                            for (int z1 = z; z1 < z + usefulSize; z1++) {
                                for (int x1 = x; x1 < x + usefulSize; x1++) {
                                    int index2 = x1 + (z1 * width) + (y1 * width * length);
//                                        rawCubes[index2] = false;
                                    occludedCubes[index2] = false;
                                }
                            }
                        }
//                        }

                        shulkers.add(new Vector4d(min.x + x * RESOLUTION + usefulSize * RESOLUTION / 2d, min.y + y * RESOLUTION, min.z + z * RESOLUTION + usefulSize * RESOLUTION / 2d, usefulSize * RESOLUTION));
                    }
                }
            }
        }

        System.out.println("size: " + shulkers.size());
//        System.out.println("finish: " + (System.currentTimeMillis() - start));

        return shulkers;
    }

    private boolean[] getRaw(Vector3d min, int width, int height, int length) {
        boolean[] positions = new boolean[width * height * length];
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    positions[x + (z * width) + (y * width * length)] = hitbox.isInside(new Vector3d(min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION));
                }
            }
        }

        return positions;
    }

    private boolean[] getCubes(boolean[] raw, int width, int height, int length) {
        boolean[] positions = new boolean[width * height * length];
        for (int y = 0; y < height - 1; y++) {
            for (int z = 0; z < length - 1; z++) {
                for (int x = 0; x < width - 1; x++) {
                    int i = x + (z * width) + (y * width * length);
                    if (!raw[i]) continue;
                    boolean b = canCube(raw, i, width, length);
                    positions[i] = b;
                }
            }
        }

        return positions;
    }

    private boolean canCube(boolean[] positions, int i, int width, int length) {
        return positions[i] && positions[i + width] && positions[i + 1] && positions[i + 1 + width] &&
                positions[i + width * length] && positions[i + width + width * length] && positions[i + 1 + width * length] && positions[i + 1 + width + width * length];
    }

    private boolean convolute(int x, int y, int z, int width, int length, boolean[] positions) {
        return positions[(x + 1) + (z * width) + (y * width * length)] &&
                positions[(x - 1) + (z * width) + (y * width * length)] &&
                positions[x + ((z + 1) * width) + (y * width * length)] &&
                positions[x + ((z - 1) * width) + (y * width * length)] &&
                positions[x + (z * width) + ((y + 1) * width * length)] &&
                positions[x + (z * width) + ((y - 1) * width * length)];
    }

    @Override
    public List<Vector4d> getColliders() {
        return null;
    }
}
