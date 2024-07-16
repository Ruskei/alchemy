package com.ixume.alchemy;

import com.ixume.alchemy.listener.PlayerInteractListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Alchemy extends JavaPlugin {
    @Override
    public void onEnable() {
        PlayerInteractListener.init(this);

        TriangleHitboxFragmentCommand.init(this);
        VirtualBlockDisplayCommand.init(this);

        HitboxRenderer.init(this);
    }
}
