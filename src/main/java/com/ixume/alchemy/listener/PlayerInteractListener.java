package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.util.Transformation;

public class PlayerInteractListener implements Listener {
    private static PlayeinirInteractListener INSTANCE;
    public static PlayerInteractListener init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new PlayerInteractListener(plugin);
        return INSTANCE;
    }

    private PlayerInteractListener(Alchemy plugin) {
       Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && event.getHand().getGroup().equals(EquipmentSlotGroup.MAINHAND) && event.getItem().getType().equals(Material.ACACIA_BOAT)) {
            System.out.println("LEFT_CLICK_BLOCK");
            event.setCancelled(true);
            Player player = event.getPlayer();
            World world = player.getWorld();
            Block block = event.getClickedBlock();
            BlockDisplay blockDisplay = world.spawn(block.getLocation().add(0, 2, 0), BlockDisplay.class);
            blockDisplay.setBlock(block.getBlockData());
            Transformation transformation = blockDisplay.getTransformation();
            transformation.getLeftRotation().rotateY((float) (player.getYaw() * Math.PI / 180f));
            blockDisplay.setTransformation(transformation);
        }
    }
}
