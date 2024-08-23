package com.ixume.alchemy.playerdata;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataHolder {
    private static PlayerDataHolder INSTANCE;
    public static PlayerDataHolder getInstance() {
        if (INSTANCE == null) INSTANCE = new PlayerDataHolder();
        return INSTANCE;
    }

    public Map<UUID, PlayerData> playerDataMap;

    public PlayerDataHolder() {
        playerDataMap = new ConcurrentHashMap<>();
    }

    public void registerPlayer(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerData());
    }

    public void removePlayer(UUID id) {
        playerDataMap.remove(id);
    }
}
