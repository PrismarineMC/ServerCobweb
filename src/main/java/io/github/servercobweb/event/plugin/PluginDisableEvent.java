package io.github.servercobweb.event.plugin;

import io.github.servercobweb.plugin.Plugin;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class PluginDisableEvent extends PluginEvent {

    public PluginDisableEvent(Plugin plugin) {
        super(plugin);
    }
}
