package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.Spike;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;
import org.joml.Vector3f;

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
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().getGroup().equals(EquipmentSlotGroup.MAINHAND) && event.getItem().getType().equals(Material.ACACIA_BOAT)) {
            System.out.println("LEFT_CLICK_BLOCK");
            event.setCancelled(true);
//            Player player = event.getPlayer();
//            World world = player.getWorld();
//            Block block = event.getClickedBlock();
//            BlockDisplay blockDisplay = world.spawn(event.getInteractionPoint().add(0, 0.5, 0), BlockDisplay.class);
//            blockDisplay.setBlock(block.getBlockData());
//
//            Transformation transformation = blockDisplay.getTransformation();
//            transformation.getLeftRotation().identity();
//            transformation.getLeftRotation().rotateY((float) (-player.getYaw() * Math.PI / 180f));
//
//            transformation.getLeftRotation();
//            Vector3f v = new Vector3f(0.5f, 0.5f, 0.5f);
//            v.rotate(transformation.getLeftRotation());
//            v.mul(-1);
//            transformation.getTranslation().set(v);
//            blockDisplay.setTransformation(transformation);
            GameObjectTicker.getInstance().addHitbox(new Spike(event.getInteractionPoint().toVector().toVector3d(), event.getClickedBlock().getBlockData(), event.getPlayer()));
        }
    }
}
