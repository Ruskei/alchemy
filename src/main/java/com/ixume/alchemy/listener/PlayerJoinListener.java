package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.Chunk;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.joml.Vector3d;

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
        Vector3d playerVector =  GameObjectTicker.getInstance().proximityList.getKeyFromRaw(player.getLocation().toVector().toVector3d());
        if (GameObjectTicker.getInstance().proximityList.chunkMap.containsKey(playerVector)) {
            GameObjectTicker.getInstance().getProximityList().playerChunkMap.put(player.getEntityId(), GameObjectTicker.getInstance().proximityList.chunkMap.get(playerVector));
        }
    }
}
