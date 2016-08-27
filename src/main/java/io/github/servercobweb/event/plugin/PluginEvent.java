package io.github.servercobweb.event.plugin;

import io.github.servercobweb.event.Event;
import io.github.servercobweb.event.HandlerList;
import io.github.servercobweb.plugin.Plugin;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class PluginEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Plugin plugin;

    public PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
