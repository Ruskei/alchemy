package com.ixume.alchemy.gameobject.virtualobjects;

import com.google.common.collect.ImmutableList;
import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.DisplayHitbox;
import com.ixume.alchemy.gameobject.GameObject;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.physical.Physical;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class VirtualParallelepiped implements GameObject, Physical {
    private static final double RESOLUTION = 0.5;
    private final World world;
    private final DisplayHitbox hitbox;
    private boolean[] positions;

    public VirtualParallelepiped(World w, Vector3d origin, Transformation transformation) {
        this.world = w;
        hitbox = new DisplayHitbox(origin, transformation);

        Bukkit.getScheduler().runTaskAsynchronously(Alchemy.getInstance(), () -> {
            GameObjectTicker.getInstance().getProximityList().addAll(cubify());
        });
    }

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.2F);

    @Override
    public void tick() {
        hitbox.tick();
//        if (positions != null) {
//            World world = Bukkit.getServer().getWorld("world");
//            Pair<Vector3d, Vector3d> boundingBox = hitbox.getBoundingBox();
//            Vector3d min = boundingBox.left();
//            Vector3d max = boundingBox.right();
//            int width = (int) ((max.x - min.x) / RESOLUTION);
//            int length = (int) ((max.z - min.z) / RESOLUTION);
//            int height = (int) ((max.y - min.y) / RESOLUTION);
//            for (int y = 0; y < height; y++) {
//                for (int z = 0; z < length; z++) {
//                    for (int x = 0; x < width; x++) {
//                        if (positions[x + (z * width) + (y * width * length)]) {
//                            world.spawnParticle(Particle.DUST, new Location(world, min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION), 1, edgeDust);
//                        }
//                    }
//                }
//            }
//        }
//        cubes.forEach(VirtualShulker::render);
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
        positions = new boolean[width * height * length];
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    positions[x + (z * width) + (y * width * length)] = hitbox.isInside(new Vector3d(min.x + x * RESOLUTION, min.y + y * RESOLUTION, min.z + z * RESOLUTION));
                }
            }
        }

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

        System.out.println("write positions: " + (System.currentTimeMillis() - start));
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

        return shulkers;

//        for (Player p : world.getPlayers()) {
//            ServerPlayer serverPlayer = ((CraftPlayer) p).getHandle();
//            Level level = serverPlayer.level();
//            ServerGamePacketListenerImpl connection = serverPlayer.connection;
//            for (Vector4d v : shulkers) {
//                net.minecraft.world.entity.decoration.ArmorStand stand = new net.minecraft.world.entity.decoration.ArmorStand(level, v.x, v.y - 1.975, v.z);
//                stand.setInvisible(true);
//                net.minecraft.world.entity.monster.Shulker shulker = new net.minecraft.world.entity.monster.Shulker(EntityType.SHULKER, level);
//                Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
//                shulker.setVariant(Optional.of(net.minecraft.world.item.DyeColor.RED));
//                shulker.getAttribute(Attributes.SCALE).setBaseValue(v.w);
//                stand.passengers = ImmutableList.of(shulker);
//                packets.add(stand.getAddEntityPacket());
//                packets.add(shulker.getAddEntityPacket());
//                packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
//                packets.add(new ClientboundSetPassengersPacket(stand));
//                packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
//                packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
//                connection.send(new ClientboundBundlePacket(packets));
//            }
//        }
//
//        System.out.println("finish: " + (System.currentTimeMillis() - start));
    }

    private boolean convolute(int x, int y, int z, int width, int length, boolean[] positions) {
        for (int y1 = y - 2; y1 <= y + 2; y1++) {
            for (int z1 = z - 2; z1 <= z + 2; z1++) {
                for (int x1 = x - 2; x1 <= x + 2; x1++) {
                    if ((x1 == x - 2 && y1 == y - 2 && z1 == z - 2)
                    || (x1 == x + 2 && y1 == y - 2 && z1 == z - 2)
                    || (x1 == x + 2 && y1 == y + 2 && z1 == z - 2)
                    || (x1 == x + 2 && y1 == y + 2 && z1 == z + 2)
                    || (x1 == x + 2 && y1 == y - 2 && z1 == z + 2)
                    || (x1 == x - 2 && y1 == y + 2 && z1 == z + 2)
                    || (x1 == x - 2 && y1 == y - 2 && z1 == z + 2)
                    || (x1 == x - 2 && y1 == y + 2 && z1 == z - 2)) continue;

                    if (!positions[x1 + (z1 * width) + (y1 * width * length)]) return false;
                }
            }
        }

        return true;
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
//        shulker.setInvisible(true);
        shulkerStand.addPassenger(shulker);
        return shulkerStand;
    }

    @Override
    public List<Vector4d> getColliders() {
        return null;
    }
}
