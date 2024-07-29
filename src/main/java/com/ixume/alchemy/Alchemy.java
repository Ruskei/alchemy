package com.ixume.alchemy;

import com.ixume.alchemy.command.VirtualObjectTestCommand;
import com.ixume.alchemy.command.VirtualBlockDisplayCommand;
import com.ixume.alchemy.gameobject.GameObjectTicker;
import com.ixume.alchemy.listener.PlayerInteractListener;
import com.ixume.alchemy.listener.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Alchemy extends JavaPlugin {
    private static Alchemy INSTANCE;
    public static Alchemy getInstance() {return INSTANCE;}
    @Override
    public void onEnable() {
        INSTANCE = this;
        PlayerInteractListener.init(this);
        PlayerJoinListener.init(this);

        VirtualObjectTestCommand.init(this);
        VirtualBlockDisplayCommand.init(this);

        GameObjectTicker.init(this);
    }
}
