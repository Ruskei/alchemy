package com.ixume.alchemy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VirtualBlockDisplayCommand implements CommandExecutor {
    private Alchemy plugin;
    private static VirtualBlockDisplayCommand INSTANCE;
    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new VirtualBlockDisplayCommand(plugin);
    }

    private VirtualBlockDisplayCommand(Alchemy plugin) {
        this.plugin = plugin;
        plugin.getCommand("virtualtest").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        new VirtualBlockDisplay(player.getLocation(), plugin);

        return true;
    }
}
