package io.github.servercobweb.event.server;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.Cancellable;
import io.github.servercobweb.event.HandlerList;
import io.github.servercobweb.network.protocol.mcpe.DataPacket;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class DataPacketReceiveEvent extends ServerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private DataPacket packet;
    private Player player;

    public DataPacketReceiveEvent(Player player, DataPacket packet) {
        this.packet = packet;
        this.player = player;
    }

    public DataPacket getPacket() {
        return packet;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }
}
