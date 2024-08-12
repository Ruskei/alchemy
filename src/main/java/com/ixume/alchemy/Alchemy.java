package com.ixume.alchemy;

import com.ixume.alchemy.command.VirtualBlockDisplayCommand;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.listener.PlayerInteractListener;
import com.ixume.alchemy.listener.PlayerJoinListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Alchemy extends JavaPlugin {
    private static Alchemy INSTANCE;
    public static Alchemy getInstance() {return INSTANCE;}
    @Override
    public void onEnable() {
        INSTANCE = this;
        PlayerInteractListener.init(this);
        PlayerJoinListener.init(this);

        VirtualBlockDisplayCommand.init(this);

        TickersManager.getInstance().tickers.put("world", new GameObjectTicker(this, Bukkit.getWorld("world")));
    }

//    public void packetsTest(Player player, Display.ItemDisplay nmsDisplay) {
//        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
//        ServerGamePacketListenerImpl connection = serverPlayer.connection;
//        connection.send(nmsDisplay.getAddEntityPacket());
//        connection.send(new ClientboundSetEntityDataPacket(nmsDisplay.getId(), nmsDisplay.getEntityData().getNonDefaultValues()));
//        serverPlayer.passengers.addLast(nmsDisplay);
//        connection.send(new ClientboundSetPassengersPacket(serverPlayer));
//    }
}
