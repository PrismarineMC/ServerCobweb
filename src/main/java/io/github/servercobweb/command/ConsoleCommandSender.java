package io.github.servercobweb.command;

import io.github.servercobweb.Server;
import io.github.servercobweb.event.TextContainer;
import io.github.servercobweb.utils.MainLogger;

import java.util.Map;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class ConsoleCommandSender implements CommandSender {

    public boolean isPlayer() {
        return false;
    }

    @Override
    public Server getServer() {
        return Server.getInstance();
    }

    @Override
    public void sendMessage(String message) {
        message = this.getServer().getLanguage().translateString(message);
        for (String line : message.trim().split("\n")) {
            MainLogger.getLogger().info(line);
        }
    }

    @Override
    public void sendMessage(TextContainer message) {
        this.sendMessage(this.getServer().getLanguage().translate(message));
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

}
