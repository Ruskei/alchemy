package com.ixume.alchemy.listener;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.playerdata.PlayerData;
import com.ixume.alchemy.playerdata.PlayerDataHolder;
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
        GameObjectTicker relevantTicker = TickersManager.getInstance().tickers.get(player.getWorld().getName());
        if (relevantTicker != null) {
            relevantTicker.registerPlayer(player);
        }

        PlayerDataHolder.getInstance().registerPlayer(player);
    }
}
