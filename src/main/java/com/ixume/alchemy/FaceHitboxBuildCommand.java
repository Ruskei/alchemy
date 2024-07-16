package com.ixume.alchemy;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FaceHitboxBuildCommand implements CommandExecutor {
    private List<Location> vertices;
    private Alchemy plugin;

    private static FaceHitboxBuildCommand INSTANCE;
    private FaceHitboxBuildCommand(Alchemy plugin) {
        vertices = new ArrayList<>();
        this.plugin = plugin;
    }

    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new FaceHitboxBuildCommand(plugin);
        plugin.getCommand("hitbox").setExecutor(INSTANCE);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        vertices.add(player.getLocation().clone());
        if (vertices.size() == 3) {
            System.out.println("reached 4 vertices");
            HitboxRenderer.getInstance().addHitbox(new TriangleHitboxFragment(vertices.toArray(new Location[0]), plugin));
            vertices.clear();
        }

        return true;
    }
}
