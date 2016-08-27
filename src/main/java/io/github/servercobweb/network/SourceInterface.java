package io.github.servercobweb.network;

import io.github.servercobweb.Player;
import io.github.servercobweb.network.protocol.mcpe.DataPacket;


/**
 * author: MagicDroidX
 * Nukkit Project
 */
public interface SourceInterface {

    Integer putPacket(Player player, DataPacket packet);

    Integer putPacket(Player player, DataPacket packet, boolean needACK);

    Integer putPacket(Player player, DataPacket packet, boolean needACK, boolean immediate);

    void close(Player player);

    void close(Player player, String reason);

    void setName(String name);

    boolean process();

    void shutdown();

    void emergencyShutdown();
}
