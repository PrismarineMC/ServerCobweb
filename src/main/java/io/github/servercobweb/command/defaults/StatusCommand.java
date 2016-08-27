package io.github.servercobweb.command.defaults;

import io.github.servercobweb.ServerCobweb;
import io.github.servercobweb.Server;
import io.github.servercobweb.command.CommandSender;
import io.github.servercobweb.math.NemisysMath;
import io.github.servercobweb.utils.TextFormat;

/**
 * Created on 2015/11/11 by xtypr.
 * Package org.itxtech.nemisys.command.defaults in project Nukkit .
 */
public class StatusCommand extends VanillaCommand {

    public StatusCommand(String name) {
        super(name, "%nemisys.command.status.description", "%nemisys.command.status.usage");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        Server server = sender.getServer();
        sender.sendMessage(TextFormat.GREEN + "---- " + TextFormat.WHITE + "Server status" + TextFormat.GREEN + " ----");

        long time = (System.currentTimeMillis() - ServerCobweb.START_TIME) / 1000;
        int seconds = NemisysMath.floorDouble(time % 60);
        int minutes = NemisysMath.floorDouble((time % 3600) / 60);
        int hours = NemisysMath.floorDouble(time % (3600 * 24) / 3600);
        int days = NemisysMath.floorDouble(time / (3600 * 24));
        String upTimeString = TextFormat.RED + days + TextFormat.GOLD + " days " +
                TextFormat.RED + hours + TextFormat.GOLD + " hours " +
                TextFormat.RED + minutes + TextFormat.GOLD + " minutes " +
                TextFormat.RED + seconds + TextFormat.GOLD + " seconds";
        sender.sendMessage(TextFormat.GOLD + "Uptime: " + upTimeString);

        String tpsColor = TextFormat.GREEN;
        float tps = server.getTicksPerSecond();
        if (tps < 17) {
            tpsColor = TextFormat.GOLD;
        } else if (tps < 12) {
            tpsColor = TextFormat.RED;
        }

        sender.sendMessage(TextFormat.GOLD + "Current TPS: " + tpsColor + NemisysMath.round(tps, 2));

        sender.sendMessage(TextFormat.GOLD + "Load: " + tpsColor + server.getTickUsage() + "%");

        sender.sendMessage(TextFormat.GOLD + "Network upload: " + TextFormat.GREEN + NemisysMath.round((server.getNetwork().getUpload() / 1024 * 1000), 2) + " kB/s");

        sender.sendMessage(TextFormat.GOLD + "Network download: " + TextFormat.GREEN + NemisysMath.round((server.getNetwork().getDownload() / 1024 * 1000), 2) + " kB/s");

        sender.sendMessage(TextFormat.GOLD + "Thread count: " + TextFormat.GREEN + Thread.getAllStackTraces().size());


        Runtime runtime = Runtime.getRuntime();
        double totalMB = NemisysMath.round(((double) runtime.totalMemory()) / 1024 / 1024, 2);
        double usedMB = NemisysMath.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 2);
        double maxMB = NemisysMath.round(((double) runtime.maxMemory()) / 1024 / 1024, 2);
        double usage = usedMB / maxMB * 100;
        String usageColor = TextFormat.GREEN;

        if (usage > 85) {
            usageColor = TextFormat.GOLD;
        }

        sender.sendMessage(TextFormat.GOLD + "Used memory: " + usageColor + usedMB + " MB. (" + NemisysMath.round(usage, 2) + "%)");

        sender.sendMessage(TextFormat.GOLD + "Total memory: " + TextFormat.RED + totalMB + " MB.");

        sender.sendMessage(TextFormat.GOLD + "Maximum VM memory: " + TextFormat.RED + maxMB + " MB.");

        sender.sendMessage(TextFormat.GOLD + "Available processors: " + TextFormat.GREEN + runtime.availableProcessors());


        String playerColor = TextFormat.GREEN;
        if (((float) server.getOnlinePlayers().size() / (float) server.getMaxPlayers()) > 0.85) {
            playerColor = TextFormat.GOLD;
        }

        sender.sendMessage(TextFormat.GOLD + "Players: " + playerColor + server.getOnlinePlayers().size() + TextFormat.GREEN + " online, " +
                TextFormat.RED + server.getMaxPlayers() + TextFormat.GREEN + " max. ");

        return true;
    }
}
