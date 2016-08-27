package io.github.servercobweb;

import io.github.servercobweb.event.player.PlayerLoginEvent;
import io.github.servercobweb.event.player.PlayerLogoutEvent;
import io.github.servercobweb.event.player.PlayerTransferEvent;
import io.github.servercobweb.network.*;
import io.github.servercobweb.network.protocol.mcpe.*;
import net.beaconpe.jraklib.client.JRakLibClient;
import io.github.servercobweb.utils.Binary;
import io.github.servercobweb.utils.Skin;
import io.github.servercobweb.utils.TextFormat;

import java.util.logging.Logger;
import java.util.*;
import java.io.IOException;

/**
 * Author: PeratX
 * Nemisys Project
 */
public class Player {
    private byte[] cachedLoginPacket = new byte[0];
    private String name;
    private String ip;
    private int port;
    private long clientId;
    private long randomClientId;
    private int protocol;
    private UUID uuid;
    private SourceInterface interfaz;
    private JRakLibConnection client;
    private Server server;
    private byte[] rawUUID;
    private boolean isFirstTimeLogin = true;
    private long lastUpdate;
    public boolean closed;
    private Skin skin;
    private ArrayList<UUID> players = new ArrayList<UUID>();

    public Player(SourceInterface interfaz, long clientId, String ip, int port){
        this.interfaz = interfaz;
        this.clientId = clientId;
        this.ip = ip;
        this.port = port;
        this.name = "null";
        this.server = Server.getInstance();
        this.lastUpdate = System.currentTimeMillis();
    }

    public long getClientId(){
        return this.clientId;
    }

    public UUID getUniqueId(){
        return this.uuid;
    }

    public byte[] getRawUUID(){
        return this.rawUUID;
    }

    public Server getServer(){
        return this.server;
    }

    public void handleDataPacket(DataPacket packet){
        if(this.closed){
            return;
        }
        this.lastUpdate = System.currentTimeMillis();

        switch (packet.pid()){
            case ProtocolInfo.BATCH_PACKET:
                if(this.cachedLoginPacket.length == 0){
                    this.getServer().getNetwork().processBatch((BatchPacket)packet, this);
                }else{
                    this.redirectPacket(packet.getBuffer());
                }
                break;
            case ProtocolInfo.LOGIN_PACKET:
                LoginPacket loginPacket = (LoginPacket)packet; 
                this.cachedLoginPacket = loginPacket.cacheBuffer;
                this.skin = loginPacket.skin;
                this.name = loginPacket.username;
                this.uuid = loginPacket.clientUUID;
                this.rawUUID = Binary.writeUUID(this.uuid);
                this.randomClientId = loginPacket.clientId;
                this.protocol = loginPacket.protocol;

                this.server.getLogger().info(this.getServer().getLanguage().translateString("nemisys.player.logIn", new String[]{
                        TextFormat.AQUA + this.name + TextFormat.WHITE,
                        this.ip,
                        String.valueOf(this.port),
                        TextFormat.GREEN + this.getRandomClientId() + TextFormat.WHITE,
                }));

                PlayerLoginEvent ev;
                this.server.getPluginManager().callEvent(ev = new PlayerLoginEvent(this, "Plugin Reason", this.server.getMainIp(), this.server.getMainPort()));
                if(ev.isCancelled()){
                    this.close(ev.getKickMessage());
                    break;
                }

                try{
                    if(JRakLibClient.pingServer(this.server.getLogger(), ev.getAddress(), ev.getPort(), 5, 200) == null){
                        this.close(TextFormat.RED + "No server online!");
                        break;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                    this.close(TextFormat.RED + "No server online!");
                    break;
                }

                this.transfer(ev.getAddress(), ev.getPort());
                break;
            case ProtocolInfo.PLAY_STATUS_PACKET:
                PlayStatusPacket pk = (PlayStatusPacket)packet;
                switch(pk.status){
                    case PlayStatusPacket.LOGIN_SUCCESS:
                        if (this.isFirstTimeLogin && this.client != null) this.redirectPacket(packet.getBuffer());
                        break;
                    case PlayStatusPacket.LOGIN_FAILED_CLIENT:
                        this.close("disconnectScreen.outdatedClient");
                        break;
                    case PlayStatusPacket.LOGIN_FAILED_SERVER:
                        this.close("disconnectScreen.outdatedServer");
                        break;
                    default:
                        if (this.client != null) this.redirectPacket(packet.getBuffer());
                        break;
                }
                break;
            case ProtocolInfo.START_GAME_PACKET:
                if(!this.isFirstTimeLogin) break;
            default:
                if (this.client != null) this.redirectPacket(packet.getBuffer());
        }
    }

    public void redirectPacket(byte[] buffer){
        this.client.send(buffer);
    }

    public String getIp(){
        return this.ip;
    }

    public int getPort(){
        return this.port;
    }

    public UUID getUUID(){
        return this.uuid;
    }

    public String getName(){
        return this.name;
    }

    public void onUpdate(long currentTick){
        if((System.currentTimeMillis() - this.lastUpdate) > 5 * 60 * 1000){//timeout
            this.close("timeout");
        }
        if(this.client != null) this.client.onTick();
    }

    public void removeAllPlayers(){
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_REMOVE;
        List<PlayerListPacket.Entry> entries = new ArrayList<>();
        for (UUID p : this.players) {
            if (p == this.uuid) {
                continue;
            }
            entries.add(new PlayerListPacket.Entry(p));
        }

        pk.entries = entries.stream().toArray(PlayerListPacket.Entry[]::new);
        this.sendDataPacket(pk);
    }

    public void transfer(String ip, int port) {
        this.transfer(ip, port, false);
    }

    public void transfer(String ip, int port, boolean needDisconnect){
        PlayerTransferEvent ev;
        this.server.getPluginManager().callEvent(ev = new PlayerTransferEvent(this, ip, port, needDisconnect));
        if(!ev.isCancelled()){
            if(this.client != null && needDisconnect){
                this.client.disconnect();
                this.removeAllPlayers();
            }
            this.client = new JRakLibConnection(this);
            this.client.connect(ip, port);

            this.isFirstTimeLogin = false;

            this.server.getLogger().info(this.name + " has been transferred to " + ip + ":" + port);
        }
    }

    public void sendLoginPacket(){
        this.client.send(this.cachedLoginPacket);//Binary.appendBytes(ProtocolInfo.LOGIN_PACKET, this.cachedLoginPacket));
    }

    public void sendDataPacket(DataPacket pk){
        this.sendDataPacket(pk, false);
    }

    public void sendDataPacket(DataPacket pk, boolean direct){
        this.sendDataPacket(pk, direct, false);
    }

    public void sendDataPacket(DataPacket pk, boolean direct, boolean needACK){
        if(pk instanceof PlayerListPacket){
            PlayerListPacket packet = (PlayerListPacket)pk;
            if(packet.type == PlayerListPacket.TYPE_ADD){
                for(PlayerListPacket.Entry entry : packet.entries){
                    players.add(entry.uuid);
                }
            } else {
                for(PlayerListPacket.Entry entry : packet.entries){
                    if(players.contains(entry.uuid)) players.remove(entry.uuid);
                }
            }
        }
        this.interfaz.putPacket(this, pk, needACK, direct);
    }

    public void close(){
        this.close("Generic Reason");
    }

    public void close(String reason){
        this.close(reason, true);
    }

    public void close(String reason, boolean notify){
        if(!this.closed){
            if(notify && reason.length() > 0){
                DisconnectPacket pk = new DisconnectPacket();
                pk.message = reason;
                this.sendDataPacket(pk, true);
            }

            this.server.getPluginManager().callEvent(new PlayerLogoutEvent(this));
            this.closed = true;

            if(this.client != null){
                this.client.disconnect();
            }

            this.server.getLogger().info(this.getServer().getLanguage().translateString("nemisys.player.logOut", new String[]{
                            TextFormat.AQUA + this.getName() + TextFormat.WHITE,
                            this.ip,
                            String.valueOf(this.port),
                            this.getServer().getLanguage().translateString(reason)
            }));

            this.interfaz.close(this, notify ? reason : "");
            this.getServer().removePlayer(this);
        }
    }

    public int rawHashCode() {
        return super.hashCode();
    }

    public int getProtocol() {
        return protocol;
    }

    public long getRandomClientId() {
        return randomClientId;
    }

    public Skin getSkin() {
        return this.skin;
    }

}
