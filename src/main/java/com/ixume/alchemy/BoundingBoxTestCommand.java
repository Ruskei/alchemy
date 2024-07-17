package com.ixume.alchemy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BoundingBoxTestCommand implements CommandExecutor {
    private final Alchemy plugin;
    private static BoundingBoxTestCommand INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new BoundingBoxTestCommand(plugin);
    }

    private BoundingBoxTestCommand(Alchemy plugin) {
        this.plugin = plugin;
        plugin.getCommand("boundingtest").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        new BoundingBoxGameObject(player, plugin);

        return true;
    }
}
