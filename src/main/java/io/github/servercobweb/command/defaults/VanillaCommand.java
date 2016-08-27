package io.github.servercobweb.command.defaults;

import io.github.servercobweb.command.Command;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class VanillaCommand extends Command {

    public VanillaCommand(String name) {
        super(name);
    }

    public VanillaCommand(String name, String description) {
        super(name, description);
    }

    public VanillaCommand(String name, String description, String usageMessage) {
        super(name, description, usageMessage);
    }

    public VanillaCommand(String name, String description, String usageMessage, String[] aliases) {
        super(name, description, usageMessage, aliases);
    }
}
