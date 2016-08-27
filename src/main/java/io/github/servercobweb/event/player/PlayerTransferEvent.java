package io.github.servercobweb.event.player;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.Cancellable;
import io.github.servercobweb.event.HandlerList;

public class PlayerTransferEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private String ip;
    private int port;
    private boolean needDisconnect;

    public PlayerTransferEvent(Player player, String ip, int port, boolean needDisconnect) {
        super(player);
        this.ip = ip;
        this.port = port;
        this.needDisconnect = needDisconnect;
    }

    public boolean isNeedDisconnect() {
        return needDisconnect;
    }

    public String getAddress() {
        return ip;
    }

    public void setAddress(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
