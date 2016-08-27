package io.github.servercobweb.command.defaults;

import io.github.servercobweb.command.Command;
import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.event.TranslationContainer;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class StopCommand extends VanillaCommand {

    public StopCommand(String name) {
        super(name, "%nemisys.command.stop.description", "%commands.stop.usage");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        sender.sendMessage(new TranslationContainer("commands.stop.start"));

        sender.getServer().shutdown();

        return true;
    }
}
