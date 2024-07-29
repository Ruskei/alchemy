package com.ixume.alchemy.command;

import com.ixume.alchemy.Alchemy;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualObjectTestCommand implements CommandExecutor {
    private final Alchemy plugin;

    private static VirtualObjectTestCommand INSTANCE;
    private VirtualObjectTestCommand(Alchemy plugin) {
        this.plugin = plugin;
    }

    public static void init(Alchemy plugin) {
        if (INSTANCE == null) INSTANCE = new VirtualObjectTestCommand(plugin);
        plugin.getCommand("hitbox").setExecutor(INSTANCE);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

//        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
//        List<ArmorStand> stands = GameObjectTicker.getInstance().proximityList.get(player.getLocation().toVector().toVector3d());
//        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(serverPlayer.getId()).stream().mapToInt(Pair::left).toArray()));
//        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(GameObjectTicker.getInstance().proximityList.playerChunkMap.get(serverPlayer.getId()).stream().mapToInt(Pair::right).toArray()));
//        for (ArmorStand stand : stands) {
//            Shulker shulker = (Shulker) stand.passengers.get(0);
//            Collection<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
//            packets.add(stand.getAddEntityPacket());
//            packets.add(shulker.getAddEntityPacket());
//            packets.add(new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().packAll()));
//            packets.add(new ClientboundSetPassengersPacket(stand));
//            packets.add(new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData().packAll()));
//            packets.add(new ClientboundUpdateAttributesPacket(shulker.getId(), shulker.getAttributes().getSyncableAttributes()));
//            serverPlayer.connection.send(new ClientboundBundlePacket(packets));
//        }
//
//        GameObjectTicker.getInstance().proximityList.playerChunkMap.put(serverPlayer.getId(), stands.stream().map(a -> Pair.of(a.getId(), a.passengers.get(0).getId())).toList());

        return true;
    }
}
