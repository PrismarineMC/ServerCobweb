package io.github.servercobweb.plugin;

import io.github.servercobweb.event.Event;
import io.github.servercobweb.event.Listener;

/**
 * author: iNevet
 * Nukkit Project
 */
public interface EventExecutor {

    void execute(Listener listener, Event event);
}
