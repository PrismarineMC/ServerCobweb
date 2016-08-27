package io.github.servercobweb.event.server;

import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.event.HandlerList;

/**
 * Called when an RCON command is executed.
 * @author Tee7even
 */
public class RemoteServerCommandEvent extends ServerCommandEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public RemoteServerCommandEvent(CommandSender sender, String command) {
        super(sender, command);
    }
}
