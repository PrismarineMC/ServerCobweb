package io.github.servercobweb.command.defaults;

import io.github.servercobweb.Player;
import io.github.servercobweb.command.Command;
import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.event.TranslationContainer;
import io.github.servercobweb.utils.TextFormat;

/**
 * Created on 2015/11/11 by xtypr.
 * Package org.itxtech.nemisys.command.defaults in project Nukkit .
 */
public class KickCommand extends VanillaCommand {

    public KickCommand(String name) {
        super(name, "%nemisys.command.kick.description", "%commands.kick.usage");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));
            return false;
        }

        String name = args[0];

        String reason = "";
        for (int i = 1; i < args.length; i++) {
            reason += args[i] + " ";
        }

        if (reason.length() > 0) {
            reason = reason.substring(0, reason.length() - 1);
        }

        Player player = sender.getServer().getPlayer(name);
        if (player != null) {
            player.close(reason);
            if (reason.length() >= 1) {
                sender.sendMessage(new TranslationContainer("commands.kick.success.reason", new String[]{player.getName(), reason}));
            } else {
                sender.sendMessage(new TranslationContainer("commands.kick.success", player.getName()));
            }
        } else {
            sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.player.notFound"));
        }

        return true;
    }
}
