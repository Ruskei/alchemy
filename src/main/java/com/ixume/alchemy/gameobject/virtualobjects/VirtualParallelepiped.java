package com.ixume.alchemy.gameobject.virtualobjects;

import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.GameObject;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class VirtualParallelepiped implements GameObject {
    private static final double RESOLUTION = 0.5;
    private final World world;
    private final DisplayHitbox hitbox;
    private List<VirtualShulker> cubes;

    public VirtualParallelepiped(World w, Vector3d origin, Transformation transformation) {
        this.world = w;
        hitbox = new DisplayHitbox(origin, transformation);
        cubes = new ArrayList<>();
        cubify();
    }

    @Override
    public void tick() {
        hitbox.tick();
//        cubes.forEach(VirtualShulker::render);
    }

    @Override
    public void kill() {

    }

    private void cubify() {
        Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
        Vector3d min = boundingBox.left();
        Vector3d max = boundingBox.right();

        for (double x = min.x; x <= max.x; x += RESOLUTION) {
            for (double y = min.y; y <= max.y; y += RESOLUTION) {
                for (double z = min.z; z <= max.z; z += RESOLUTION) {
                    double s = 0;
                    outer:
                    while (true) {
                        VirtualShulker shulker = new VirtualShulker(new Vector3d(x, y, z), new Vector3d(x + s + RESOLUTION, y + s + RESOLUTION, z + s + RESOLUTION));
                        if (shulker.isInside(hitbox, RESOLUTION)) {
                            //probably valid size

                            for (VirtualShulker cube : cubes) {
                                if (cube.isInside(shulker)) {
                                    //invalid size
                                    break outer;
                                }
                            }

                        } else {
                            break;
                        }

                        s += RESOLUTION;
                    }

                    if (s > 0) {
                        cubes.add(new VirtualShulker(new Vector3d(x, y, z), new Vector3d(x + s, y + s, z + s)));
                        spawnShulker(new Vector3d(x + s / 2d, y, z + s / 2d), s);
                    }
                }
            }
        }

        int a = 0;
        for (VirtualShulker s1 : cubes) {
            for (VirtualShulker s2 : cubes) {
                if (s1 == s2) continue;
                if (s1.isInside(s2)) a++;
            }
        }
    }

    private ArmorStand spawnShulker(Vector3d pos, double scale) {
        ArmorStand shulkerStand = world.spawn(new Location(world, pos.x, pos.y - 1.475 - 0.5, pos.z), ArmorStand.class);
        shulkerStand.setGravity(false);
        shulkerStand.setInvisible(true);
        Shulker shulker = world.spawn(new Location(world, pos.x, pos.y, pos.z), Shulker.class);
        shulker.setColor(DyeColor.RED);
        shulker.setAI(false);
        shulker.setGravity(false);
        shulker.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(scale);
        shulkerStand.addPassenger(shulker);
        return shulkerStand;
    }
}
