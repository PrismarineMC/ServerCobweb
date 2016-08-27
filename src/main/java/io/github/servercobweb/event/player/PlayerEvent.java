package io.github.servercobweb.event.player;

import io.github.servercobweb.Player;
import io.github.servercobweb.event.Event;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class PlayerEvent extends Event {
    protected Player player;

    public Player getPlayer() {
        return player;
    }

    public PlayerEvent(Player player) {
        this.player = player;
    }
}
