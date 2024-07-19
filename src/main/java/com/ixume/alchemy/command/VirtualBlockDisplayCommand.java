package com.ixume.alchemy.command;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.VirtualBlockDisplay;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VirtualBlockDisplayCommand implements CommandExecutor {
    private final Alchemy plugin;
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

        World world = player.getWorld();
        if (!world.getEntities().isEmpty()) {
            BlockDisplay c = null;
            double d = Double.MAX_VALUE;
            for (BlockDisplay e : world.getEntitiesByClass(BlockDisplay.class)) {
                double d1 = player.getLocation().distanceSquared(e.getLocation());
                if (d1 < d) {
                    d = d1;
                    c = e;
                }

                if (c != null) {
                    new VirtualBlockDisplay(c, plugin);
                }
            }
        }

        return true;
    }
}
