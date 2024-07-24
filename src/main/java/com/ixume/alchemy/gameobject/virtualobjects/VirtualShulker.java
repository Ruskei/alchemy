package com.ixume.alchemy.gameobject.virtualobjects;

import com.ixume.alchemy.DisplayHitbox;
import org.bukkit.*;
import org.joml.Vector3d;

public class VirtualShulker {
    private final Vector3d min;
    private final Vector3d max;

    public VirtualShulker(Vector3d min, Vector3d max) {
        this.min = min;
        this.max = max;
    }

    public boolean isInside(DisplayHitbox hitbox, double RESOLUTION) {
        for (double x = min.x; x <= max.x; x += RESOLUTION) {
            for (double y = min.y; y <= max.y; y += RESOLUTION) {
                for (double z = min.z; z <= max.z; z += RESOLUTION) {
                    if (!hitbox.isInside(new Vector3d(x, y, z))) return false;
                }
            }
        }

        return true;
    }

    public boolean isInside(VirtualShulker shulker) {
        return this.min.x < shulker.max.x && this.max.x > shulker.min.x
                && this.min.y < shulker.max.y && this.max.y > shulker.min.y
                && this.min.z < shulker.max.z && this.max.z > shulker.min.z;
    }

    private final Particle.DustOptions minEdgeDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.4F);
    private final Particle.DustOptions maxEdgeDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.4F);

    public void render() {
        World world = Bukkit.getServer().getWorld("world");
        world.spawnParticle(Particle.DUST, new Location(world, min.x, min.y, min.z), 1, minEdgeDust);
        world.spawnParticle(Particle.DUST, new Location(world, max.x, max.y, max.z), 1, maxEdgeDust);
    }
}
