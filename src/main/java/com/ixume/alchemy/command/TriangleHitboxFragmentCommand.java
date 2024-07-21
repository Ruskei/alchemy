package com.ixume.alchemy.command;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TriangleTestGameObject;
import com.ixume.alchemy.hitbox.TriangleHitboxFragment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class TriangleHitboxFragmentCommand implements CommandExecutor {
    private final List<Vector3d> vertices;
    private final Alchemy plugin;

    private static TriangleHitboxFragmentCommand INSTANCE;
    private TriangleHitboxFragmentCommand(Alchemy plugin) {
        vertices = new ArrayList<>();
        this.plugin = plugin;
    }

    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new TriangleHitboxFragmentCommand(plugin);
        plugin.getCommand("hitbox").setExecutor(INSTANCE);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        vertices.add(player.getLocation().toVector().toVector3d());
        if (vertices.size() == 3) {
            System.out.println("reached 4 vertices");
            GameObjectTicker.getInstance().addObject(new TriangleTestGameObject(new TriangleHitboxFragment(vertices.toArray(new Vector3d[0]), plugin)));
            vertices.clear();
        }

        return true;
    }
}
