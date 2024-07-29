package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.Chunk;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class PlayerJoinListener implements Listener {
    private static PlayerJoinListener INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new PlayerJoinListener(plugin);
    }

    private PlayerJoinListener(Alchemy plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
//        Chunk playerChunk = GameObjectTicker.getInstance().proximityList.get(player.getLocation().toVector().toVector3d());
//        if (playerChunk == null)
        GameObjectTicker.getInstance().getProximityList().playerChunkMap.put(((CraftPlayer) joinEvent.getPlayer()).getHandle().getId(), GameObjectTicker.getInstance().proximityList.get(joinEvent.getPlayer().getLocation().toVector().toVector3d()));
    }
}
