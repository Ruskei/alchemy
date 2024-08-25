package com.ixume.alchemy;

import com.ixume.alchemy.command.VirtualBlockDisplayCommand;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.gameobject.TickersManager;
import com.ixume.alchemy.gameobject.bending.EarthbendingDisplayImpl;
import com.ixume.alchemy.gameobject.bending.VisualBlockDisplay;
import com.ixume.alchemy.listener.PlayerInteractListener;
import com.ixume.alchemy.listener.PlayerJoinListener;
import com.ixume.alchemy.listener.PlayerLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class Alchemy extends JavaPlugin {
    private static Alchemy INSTANCE;
    public static Alchemy getInstance() {return INSTANCE;}
    @Override
    public void onEnable() {
        INSTANCE = this;
        PlayerInteractListener.init(this);
        PlayerJoinListener.init(this);
        PlayerLeaveListener.init(this);

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
