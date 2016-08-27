package io.github.servercobweb.event.player;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.Cancellable;
import io.github.servercobweb.event.HandlerList;

public class PlayerLoginEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String kickMessage;
    private String ip;
    private int port;


    public PlayerLoginEvent(Player player, String kickMessage, String ip, int port) {
        super(player);
        this.kickMessage = kickMessage;
        this.ip = ip;
        this.port = port;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    public void setAddress(String address) {
        this.ip = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
