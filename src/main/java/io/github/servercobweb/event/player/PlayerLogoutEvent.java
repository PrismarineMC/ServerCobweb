package io.github.servercobweb.event.player;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.HandlerList;

public class PlayerLogoutEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public PlayerLogoutEvent(Player player) {
        super(player);
    }

}
