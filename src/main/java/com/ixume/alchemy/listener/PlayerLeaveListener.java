package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {
    private static PlayerLeaveListener INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new PlayerLeaveListener(plugin);
    }

    private PlayerLeaveListener(Alchemy plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameObjectTicker relevantTicker = TickersManager.getInstance().tickers.get(player.getWorld().getName());
        if (relevantTicker != null) {
            relevantTicker.proximityList.playerPositionsMap.remove(player.getUniqueId());
        }
    }
}
