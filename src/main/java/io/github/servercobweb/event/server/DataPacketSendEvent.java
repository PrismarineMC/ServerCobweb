package io.github.servercobweb.event.server;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.Cancellable;
import io.github.servercobweb.event.HandlerList;
import io.github.servercobweb.network.protocol.mcpe.DataPacket;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class DataPacketSendEvent extends ServerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private DataPacket packet;
    private Player player;

    public DataPacketSendEvent(Player player, DataPacket packet) {
        this.packet = packet;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public DataPacket getPacket() {
        return packet;
    }
}
