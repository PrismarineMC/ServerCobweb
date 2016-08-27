package io.github.servercobweb.command.defaults;

import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.event.TranslationContainer;
import io.github.servercobweb.network.protocol.mcpe.ProtocolInfo;
import io.github.servercobweb.plugin.Plugin;
import io.github.servercobweb.plugin.PluginDescription;
import io.github.servercobweb.utils.TextFormat;

import java.util.List;

/**
 * Created on 2015/11/12 by xtypr.
 * Package org.itxtech.nemisys.command.defaults in project Nukkit .
 */
public class VersionCommand extends VanillaCommand {

    public VersionCommand(String name) {
        super(name,
                "%nemisys.command.version.description",
                "%nemisys.command.version.usage",
                new String[]{"ver", "about"}
        );
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TranslationContainer("nemisys.server.info.extended", new String[]{
                    sender.getServer().getName(),
                    sender.getServer().getServerVersion(),
                    sender.getServer().getCodename(),
                    sender.getServer().getApiVersion(),
                    sender.getServer().getVersion(),
                    String.valueOf(ProtocolInfo.CURRENT_PROTOCOL)
            }));
        } else {
            String pluginName = "";
            for (String arg : args) pluginName += arg + " ";
            pluginName = pluginName.trim();
            final boolean[] found = {false};
            final Plugin[] exactPlugin = {sender.getServer().getPluginManager().getPlugin(pluginName)};

            if (exactPlugin[0] == null) {
                pluginName = pluginName.toLowerCase();
                final String finalPluginName = pluginName;
                sender.getServer().getPluginManager().getPlugins().forEach((s, p) -> {
                    if (s.toLowerCase().contains(finalPluginName)) {
                        exactPlugin[0] = p;
                        found[0] = true;
                    }
                });
            } else {
                found[0] = true;
            }

            if (found[0]) {
                PluginDescription desc = exactPlugin[0].getDescription();
                sender.sendMessage(TextFormat.DARK_GREEN + desc.getName() + TextFormat.WHITE + " version " + TextFormat.DARK_GREEN + desc.getVersion());
                if (desc.getDescription() != null) {
                    sender.sendMessage(desc.getDescription());
                }
                if (desc.getWebsite() != null) {
                    sender.sendMessage("Website: " + desc.getWebsite());
                }
                List<String> authors = desc.getAuthors();
                final String[] authorsString = {""};
                authors.forEach((s) -> authorsString[0] += s);
                if (authors.size() == 1) {
                    sender.sendMessage("Author: " + authorsString[0]);
                } else if (authors.size() >= 2) {
                    sender.sendMessage("Authors: " + authorsString[0]);
                }
            } else {
                sender.sendMessage(new TranslationContainer("nemisys.command.version.noSuchPlugin"));
            }
        }
        return true;
    }
}
