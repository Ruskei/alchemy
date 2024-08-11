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
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

public class VirtualParallelepiped implements GameObject, Physical {
    private static final double RESOLUTION = 0.5;
    private final DisplayHitbox hitbox;
    private long[] cubes;
    private final World world;

    public VirtualParallelepiped(Vector3d origin, Transformation transformation, World world) {
        this.world = world;
        hitbox = new DisplayHitbox(origin, transformation, world);

        GameObjectTicker relevantTicker = TickersManager.getInstance().tickers.get(world.getName());
        if (relevantTicker != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> relevantTicker.getProximityList().addAll(binaryCubify()));
        }
    }

    @Override
    public void tick() {
        hitbox.tick();
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        int width = (int) ((max.x - min.x) / RESOLUTION);
        int length = (int) ((max.z - min.z) / RESOLUTION);
        int height = (int) ((max.y - min.y) / RESOLUTION);

        if (cubes != null) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (((cubes[x + (z * width)] >> y) & 1) == 1) {
                                Vector3d p = new Vector3d(min.x + x * RESOLUTION + RESOLUTION / 2d, min.y + y * RESOLUTION + RESOLUTION / 2d, min.z + z * RESOLUTION + RESOLUTION / 2d);
                                world.spawnParticle(Particle.DUST, new Location(world, p.x, p.y, p.z), 1, edgeDust);
                            }
                        }
                }
            }
        }
    }

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);

    @Override
    public void kill() {

    }

    private List<Vector4d> binaryCubify() {
        long start = System.currentTimeMillis();

        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        int width = (int) Math.floor((max.x - min.x) / RESOLUTION);
        int length = (int) Math.floor((max.z - min.z) / RESOLUTION);
        int height = (int) Math.floor((max.y - min.y) / RESOLUTION);
        if (width > 63 || length > 63 || height > 63) throw new IndexOutOfBoundsException();

        long[] points = new long[(width + 1) * (length + 1)];
//        long full = 0xFFFFFFFFFFFFFFFFL;
//        long xMask = full << (64 - width);
//        long yMask = full << (64 - height);
//        long zMask = full << (64 - length);

        //set the general positions && only choose those with potential to be cubes
        for (int z = 0; z < length + 1; z++) {
            for (int x = 0; x < width + 1; x++) {
                int index = z * width + x;
                for (int y = 0; y < height + 1; y++) {
                    points[index] |= (hitbox.inInsideLong(new Vector3d(min.x + x * RESOLUTION + RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION)) << y);
                }

                points[index] &= (points[index] >> 1);
            }
        }

        System.out.println("write positions: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        long[] cubes = new long[width * length];
        //get actual cubes from positions
        for (int z = 0; z < length; z++) {
            for (int x = 0; x < width; x++) {
                int index = z * width + x;
                cubes[index] = points[index] & points[index + 1] & points[index + width] & points[index + width + 1];
            }
        }

        this.cubes = cubes;

        System.out.println("cubify: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        //cull occluded
        long[] culledCubes = new long[width * length];

        for (int z = 1; z < length - 1; z++) {
            for (int x = 1; x < width - 1; x++) {
                int index = z * width + x;
                long column = cubes[index];
                culledCubes[index] = column & ~(column & (column >> 1) & (column << 1) & cubes[index - 1] & cubes[index + 1] & cubes[index + width] & cubes[index - width]);
            }
        }

        this.cubes = culledCubes;

        System.out.println("cull occluded: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        return new ArrayList<>();
    }

    private List<Vector4d> naiveCubify() {
        long start = System.currentTimeMillis();
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        int width = (int) ((max.x - min.x) / RESOLUTION);
        int length = (int) ((max.z - min.z) / RESOLUTION);
        int height = (int) ((max.y - min.y) / RESOLUTION);
//        create 3d
        boolean[] raw = getRaw(min, width, height, length);
//        cubes = raw;
        boolean[] rawCubes = getCubes(raw, width, height, length);
        boolean[] occludedCubes = rawCubes.clone();

        System.out.println("write positions: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

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

//        cubes = occludedCubes.clone();

//
        System.out.println("convolution: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

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

        System.out.println(shulkers.size());
        System.out.println("finish: " + (System.currentTimeMillis() - start));

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
        return positions[(x+1) + (z * width) + (y * width * length)] &&
                positions[(x-1) + (z * width) + (y * width * length)] &&
                positions[x + ((z+1) * width) + (y * width * length)] &&
                positions[x + ((z-1) * width) + (y * width * length)] &&
                positions[x + (z * width) + ((y+1) * width * length)] &&
                positions[x + (z * width) + ((y-1) * width * length)];
    }

    @Override
    public List<Vector4d> getColliders() {
        return null;
    }
}
