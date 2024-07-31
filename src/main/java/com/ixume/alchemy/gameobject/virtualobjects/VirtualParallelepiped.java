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

    public VirtualParallelepiped(Vector3d origin, Transformation transformation, World world) {
        hitbox = new DisplayHitbox(origin, transformation, world);

        GameObjectTicker relevantTicker = TickersManager.getInstance().tickers.get(world.getName());
        if (relevantTicker != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> relevantTicker.getProximityList().addAll(cubify()));
        }
    }

    @Override
    public void tick() {
        hitbox.tick();
    }

    @Override
    public void kill() {

    }

    private List<Vector4d> cubify() {
        long start = System.currentTimeMillis();
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();
        int width = (int) ((max.x - min.x) / RESOLUTION);
        int length = (int) ((max.z - min.z) / RESOLUTION);
        int height = (int) ((max.y - min.y) / RESOLUTION);
//        create 3d
        boolean[] positions = new boolean[width * height * length];
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    positions[x + (z * width) + (y * width * length)] = hitbox.isInside(new Vector3d(min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION));
                }
            }
        }

        System.out.println("write positions: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        List<Integer> occluded = new ArrayList<>();
        for (int y = 2; y < height - 2; y++) {
            for (int z = 2; z < length - 2; z++) {
                for (int x = 2; x < width - 2; x++) {
                    int i = x + (z * width) + (y * width * length);
                    if (positions[i]) {
                        if (convolute(x, y, z, width, length, positions)) {
                            occluded.add(i);
                        }
                    }
                }
            }
        }

        for (Integer i : occluded) {
            positions[i] = false;
        }

        System.out.println("convolution: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        List<Vector4d> shulkers = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    //check if this position is inside the shape
                    int index = x + (z * width) + (y * width * length);
                    if (!positions[index]) continue;
                    //find largest cube now,
                    int size = 0;
                    check:
                    while (true) {
                        int testSize = size + 1;
                        if (x + testSize >= width || y + testSize >= height || z + testSize >= length) break;
                        //pretend size is 1 more than it is, then if that's fine, move on, if it's not, leave
                        //if any of these are false, then the cube cannot be formed
                        for (int j = y; j <= y + testSize; j++) {
                            for (int i = x; i <= x + testSize; i++) {
                                if (!positions[i + ((z + testSize) * width) + (j * width * length)]) break check;
                            }
                        }

                        for (int j = y; j <= y + testSize; j++) {
                            for (int i = z; i < z + testSize; i++) {
                                if (!positions[x + testSize + (i * width) + (j * width * length)]) break check;
                            }
                        }

                        for (int j = z; j < z + testSize; j++) {
                            for (int i = x; i < x + testSize; i++) {
                                if (!positions[i + (j * width) + ((y + testSize) * width * length)]) break check;
                            }
                        }

                        size++;
                    }

                    if (size > 0) {
                        //hollow out the cube
                        if (size > 1) {
                            for (int y1 = y + 1; y1 < y + size; y1++) {
                                for (int z1 = z + 1; z1 < z + size; z1++) {
                                    for (int x1 = x + 1; x1 < x + size; x1++) {
                                        positions[x1 + (z1 * width) + (y1 * width * length)] = false;
                                    }
                                }
                            }
                        }

                        shulkers.add(new Vector4d(min.x + x * RESOLUTION + size * RESOLUTION / 2d, min.y + y * RESOLUTION, min.z + z * RESOLUTION + size * RESOLUTION / 2d, size * RESOLUTION));
                    }
                }
            }
        }

        System.out.println("finish: " + (System.currentTimeMillis() - start));

        return shulkers;
    }

    private boolean convolute(int x, int y, int z, int width, int length, boolean[] positions) {
        int SIZE = 2;
        for (int y1 = y - SIZE; y1 <= y + SIZE; y1++) {
            for (int z1 = z - SIZE; z1 <= z + SIZE; z1++) {
                for (int x1 = x - SIZE; x1 <= x + SIZE; x1++) {
                    if ((x1 == x - SIZE && y1 == y - SIZE && z1 == z - SIZE)
                    || (x1 == x + SIZE && y1 == y - SIZE && z1 == z - SIZE)
                    || (x1 == x + SIZE && y1 == y + SIZE && z1 == z - SIZE)
                    || (x1 == x + SIZE && y1 == y + SIZE && z1 == z + SIZE)
                    || (x1 == x + SIZE && y1 == y - SIZE && z1 == z + SIZE)
                    || (x1 == x - SIZE && y1 == y + SIZE && z1 == z + SIZE)
                    || (x1 == x - SIZE && y1 == y - SIZE && z1 == z + SIZE)
                    || (x1 == x - SIZE && y1 == y + SIZE && z1 == z - SIZE)) continue;

                    if (!positions[x1 + (z1 * width) + (y1 * width * length)]) return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<Vector4d> getColliders() {
        return null;
    }
}
