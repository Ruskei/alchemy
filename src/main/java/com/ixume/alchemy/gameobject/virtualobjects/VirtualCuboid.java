package com.ixume.alchemy.gameobject.virtualobjects;

import com.ixume.alchemy.gameobject.GameObject;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.joml.Vector3d;

//implement a basic slanted surface first
//every tick,
public class VirtualCuboid implements GameObject {
    private final VirtualPlane[] planes;
    private final Player p;

    private ArmorStand shulkerStandN;
    private Shulker shulkerN;

    private ArmorStand shulkerStandS;
    private Shulker shulkerS;

    private final Particle.DustOptions edgeDust = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0F);

    public VirtualCuboid(Vector3d min, Vector3d max, Player p) {
        this.p = p;
        planes = new VirtualPlane[]{
                new VirtualPlane(new Vector3d(min), new Vector3d(max.x, min.y, max.z), 1, p),
                new VirtualPlane(new Vector3d(min), new Vector3d(max.x, max.y, min.z), -1, p),
                new VirtualPlane(new Vector3d(min), new Vector3d(min.x, max.y, max.z), 1, p),
                new VirtualPlane(new Vector3d(min.x, min.y, max.z), new Vector3d(max), 1, p),
                new VirtualPlane(new Vector3d(max.x, min.y, max.z), new Vector3d(max.x, max.y, min.z), 1, p),
                new VirtualPlane(new Vector3d(min.x, max.y, min.z), new Vector3d(max), -1, p)
        };
    }

    private void setNorth(Vector3d pos) {
        if (shulkerStandN == null) {
            shulkerStandN = p.getWorld().spawn(new Location(p.getWorld(), pos.x, pos.y - 1.475 - 1, pos.z), ArmorStand.class);
            shulkerStandN.setInvulnerable(true);
            shulkerStandN.setGravity(false);
            shulkerStandN.setInvisible(true);
            shulkerN = p.getWorld().spawn(new Location(p.getWorld(), pos.x, pos.y, pos.z), Shulker.class);
            shulkerN.setColor(DyeColor.RED);
            shulkerN.setAI(false);
            shulkerN.setInvulnerable(true);
            shulkerStandN.addPassenger(shulkerN);
        } else {
            shulkerStandN.removePassenger(shulkerN);
            shulkerStandN.teleport(new Location(p.getWorld(), pos.x, pos.y - 1.475 - 1, pos.z));
            shulkerStandN.addPassenger(shulkerN);
        }
    }

    private void setSouth(Vector3d pos) {
        if (shulkerStandS == null) {
            shulkerStandS = p.getWorld().spawn(new Location(p.getWorld(), pos.x, pos.y - 1.475 - 1, pos.z), ArmorStand.class);
            shulkerStandS.setInvulnerable(true);
            shulkerStandS.setGravity(false);
            shulkerStandS.setInvisible(true);
            shulkerS = p.getWorld().spawn(new Location(p.getWorld(), pos.x, pos.y, pos.z), Shulker.class);
            shulkerS.setColor(DyeColor.RED);
            shulkerS.setAI(false);
            shulkerS.setInvulnerable(true);
            shulkerStandS.addPassenger(shulkerS);
        } else {
            shulkerStandS.removePassenger(shulkerS);
            shulkerStandS.teleport(new Location(p.getWorld(), pos.x, pos.y - 1.475 - 1, pos.z));
            shulkerStandS.addPassenger(shulkerS);
        }
    }

    @Override
    public void tick() {
        World world = Bukkit.getServer().getWorld("world");
        Vector3d intersectionN = new Vector3d(0, 0, Double.NEGATIVE_INFINITY);
        Vector3d intersectionS = new Vector3d(0, 0, Double.POSITIVE_INFINITY);
        //north intersection from player with this
        for (VirtualPlane plane : planes) {
            Pair<Vector3d, Vector3d> intersectionsNS = plane.intersectNS();
            if (intersectionsNS.getLeft() != null) {
                intersectionN = intersectionsNS.getLeft().z > intersectionN.z ? intersectionsNS.getLeft() : intersectionN;
            }

            if (intersectionsNS.getRight() != null) {
                intersectionS = intersectionsNS.getRight().z < intersectionS.z ? intersectionsNS.getRight() : intersectionS;
            }

            plane.render();
        }

        if (intersectionN.z != Double.NEGATIVE_INFINITY) {
            setNorth(intersectionN);
            world.spawnParticle(Particle.DUST, new Location(world, intersectionN.x, intersectionN.y, intersectionN.z), 1, edgeDust);
        } else {
            if (shulkerStandN != null) {
                shulkerN.remove();
                shulkerN = null;
                shulkerStandN.remove();
                shulkerStandN = null;
            }
        }

        if (intersectionS.z != Double.POSITIVE_INFINITY) {
            setSouth(intersectionS);
            world.spawnParticle(Particle.DUST, new Location(world, intersectionS.x, intersectionS.y, intersectionS.z), 1, edgeDust);
        } else {
            if (shulkerStandS != null) {
                shulkerS.remove();
                shulkerS = null;
                shulkerStandS.remove();
                shulkerStandS = null;
            }
        }
    }

    @Override
    public void kill() {

    }
}
