package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.Spike;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class PlayerInteractListener implements Listener {
    private final Alchemy plugin;

    private static PlayerInteractListener INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new PlayerInteractListener(plugin);
    }

    private PlayerInteractListener(Alchemy plugin) {
       Bukkit.getPluginManager().registerEvents(this, plugin);
       this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) && event.getHand().getGroup().equals(EquipmentSlotGroup.MAINHAND) && event.getItem().getType().equals(Material.ACACIA_BOAT)) {
            event.setCancelled(true);
            Player p = event.getPlayer();
            World w = p.getWorld();
            Location origin = p.getEyeLocation();
            Vector dir = p.getLocation().getDirection();
            RayTraceResult rayTraceResult = w.rayTraceBlocks(origin, dir, 20);
            if (rayTraceResult != null) {
                List<Entity> nearbyEntities = w.getNearbyEntities(rayTraceResult.getHitPosition().toLocation(w), 10, 10, 10).stream().filter(k -> !(k.getType().equals(EntityType.BLOCK_DISPLAY))).toList();
                if (!nearbyEntities.isEmpty()) {
                    System.out.println(rayTraceResult.getHitBlock().getType() + " " + nearbyEntities.getFirst().getType());
                    Vector3f target = nearbyEntities.stream().toList().getFirst().getLocation().toVector().toVector3f().add(0, 1, 0);
                    Vector3f spikeOrigin = rayTraceResult.getHitPosition().toVector3f();
                    GameObjectTicker.getInstance().addHitbox(new Spike(spikeOrigin, target, rayTraceResult.getHitBlock().getBlockData(), event.getPlayer()));
                }
            }
        }
    }
}
