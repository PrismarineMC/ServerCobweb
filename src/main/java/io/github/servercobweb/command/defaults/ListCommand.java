package io.github.servercobweb.command.defaults;

import io.github.servercobweb.Player;
import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.event.TranslationContainer;

/**
 * Created on 2015/11/11 by xtypr.
 * Package org.itxtech.nemisys.command.defaults in project Nukkit .
 */
public class ListCommand extends VanillaCommand {

    public ListCommand(String name) {
        super(name, "%nemisys.command.list.description", "%commands.players.usage");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        String online = "";
        int onlineCount = 0;
        for (Player player : sender.getServer().getOnlinePlayers().values()) {
            online += player.getName() + ", ";
            ++onlineCount;
        }

        if (online.length() > 0) {
            online = online.substring(0, online.length() - 2);
        }

        sender.sendMessage(new TranslationContainer("commands.players.list",
                new String[]{String.valueOf(onlineCount), String.valueOf(sender.getServer().getMaxPlayers())}));
        sender.sendMessage(online);
        return true;
    }
}
