package com.ixume.alchemy.gameobject.virtualobjects;

import net.minecraft.network.protocol.game.ClientboundBundlePacket;

public class VirtualShulker {
    private final ClientboundBundlePacket packet;
    private final int armorstand;
    private final int shulkerbox;

    public VirtualShulker(ClientboundBundlePacket packet, int armorstand, int shulkerbox) {
        this.packet = packet;
        this.armorstand = armorstand;
        this.shulkerbox = shulkerbox;
    }

    public ClientboundBundlePacket getPacket() {
        return packet;
    }

    public int getArmorstand() {
        return armorstand;
    }

    public int getShulkerbox() {
        return shulkerbox;
    }
}
