package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.Spike;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.playerdata.Plane;
import com.ixume.alchemy.playerdata.PlayerDataHolder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

public class PlayerInteractListener implements Listener {

    private static PlayerInteractListener INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new PlayerInteractListener(plugin);
    }

    private PlayerInteractListener(Alchemy plugin) {
       Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) && event.getHand().getGroup().equals(EquipmentSlotGroup.MAINHAND)) {
            if (event.getItem().getType().equals(Material.ACACIA_BOAT)) {
                Player p = event.getPlayer();
                World w = p.getWorld();
                GameObjectTicker relevantTicker = TickersManager.getInstance().tickers.get(w.getName());
                if (relevantTicker == null) return;
                event.setCancelled(true);
                Location origin = p.getEyeLocation();
                Vector dir = p.getLocation().getDirection();
                RayTraceResult rayTraceResult = w.rayTraceBlocks(origin, dir, 20);
                if (rayTraceResult != null) {
                    Location raycastLocation = rayTraceResult.getHitPosition().toLocation(w);
                    List<Entity> nearbyEntities = w.getNearbyEntities(raycastLocation, 14, 14, 14).stream().filter(k -> !(k.getType().equals(EntityType.BLOCK_DISPLAY)) && !(k.isDead())).toList();
                    if (!nearbyEntities.isEmpty()) {
                        Entity closest = null;
                        double d = Double.MAX_VALUE;
                        for (Entity e : nearbyEntities) {
                            double d1 = e.getLocation().distanceSquared(raycastLocation);
                            if (d1 < d) {
                                d = d1;
                                closest = e;
                            }
                        }

                        Vector3f target = closest.getLocation().toVector().toVector3f().add(0, 1, 0);
                        Vector3f spikeOrigin = rayTraceResult.getHitPosition().toVector3f();
                        relevantTicker.addObject(new Spike(spikeOrigin, target, rayTraceResult.getHitBlock().getBlockData(), event.getPlayer()));
                    }
                }
            } else if (event.getItem().getType().equals(Material.CHERRY_PRESSURE_PLATE)) {
                System.out.println("set navigation plane");
                //set navigation plane
                Player player = event.getPlayer();
                Vector3d playerDir = player.getLocation().getDirection().toVector3d();

                Vector3d perpendicular = new Vector3d(playerDir).rotateY(Math.PI / 2d);
                perpendicular.y = 0;
                perpendicular.normalize();
                Vector3d normal = playerDir.cross(perpendicular);

                PlayerDataHolder.getInstance().playerDataMap.get(player.getUniqueId()).bendingPlane = new Plane(normal, player.getEyeLocation().toVector().toVector3d());
            } else if (event.getItem().getType().equals(Material.TOTEM_OF_UNDYING)) {
                System.out.println("clutch");
                Player player = event.getPlayer();
                if (!PlayerDataHolder.getInstance().playerDataMap.containsKey(player.getUniqueId())) {
                   return;
                }

                Plane bendingPlane = PlayerDataHolder.getInstance().playerDataMap.get(player.getUniqueId()).bendingPlane;
                if (bendingPlane == null) return;

                Vector3d intersectionPoint = bendingPlane.intersectWithVector(player.getLocation().getDirection().toVector3d().mul(20), player.getEyeLocation().toVector().toVector3d());
                if (intersectionPoint == null) {
                   return;
                }

                player.getWorld().spawnParticle(Particle.DUST, new Location(player.getWorld(), intersectionPoint.x, intersectionPoint.y, intersectionPoint.z), 1, normalDust);
            }
        }
    }

    private final Particle.DustOptions normalDust = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 2F);
}
