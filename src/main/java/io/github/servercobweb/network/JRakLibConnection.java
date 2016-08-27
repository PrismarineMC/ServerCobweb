package io.github.servercobweb.network;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.Getter;
import io.github.servercobweb.network.protocol.mcpe.*;
import io.github.servercobweb.*;
import io.github.servercobweb.utils.*;
import net.beaconpe.jraklib.JRakLib;
import net.beaconpe.jraklib.client.ClientHandler;
import net.beaconpe.jraklib.client.ClientInstance;
import net.beaconpe.jraklib.client.JRakLibClient;
import net.beaconpe.jraklib.protocol.EncapsulatedPacket;

public class JRakLibConnection implements ClientInstance {

    public final static String ENTITY_ID_KEY = "ENTITYID";
    
    private JRakLibClient client;
    private ClientHandler handler;

    private Player player;
    private boolean connected;

    public JRakLibConnection(Player player) {
        this.player = player;
    }

    public void onTick(){
        if(handler != null){
            while(handler.handlePacket()){}
        }
    }

    public void connect(String addr, int port) {
        client = new JRakLibClient(player.getServer().getLogger(), addr, port);
        handler = new ClientHandler(client, this);
    }

    public boolean isConnected() {
        return connected;
    }

    public void send(DataPacket packet) {
        sendPacket(packet, true);
    }

    public void send(byte[] bytes) {
        sendPacket(bytes, true);
    }

    public void send(DataPacket... packets) {
        for (DataPacket packet : packets) {
            sendPacket(packet, true);
        }
    }

    public void disconnect() {
        connected = false;
        handler.disconnectFromServer();
    }

    // ==== RakNet thingy ====
    @Override
    public void connectionOpened(long serverId) {
        connected = true;

        //System.out.println("RakNet login success");
        
        player.sendLoginPacket();
    }

    @Override
    public void connectionClosed(String reason) {
        connected = false;
        player.close(reason);
    }

    @Override
    public void handleEncapsulated(EncapsulatedPacket packet, int flags) {
        byte[] buffer = Arrays.copyOfRange(packet.buffer, 1, packet.buffer.length);
        DataPacket pk = this.getPacket(buffer);
        
        player.sendDataPacket(pk);
    }

    @Override
    public void handleRaw(byte[] payload) {
    }

    @Override
    public void handleOption(String option, String value) {
    }

    public void sendPacket(DataPacket packet, boolean immediate) {
        if (packet == null) {
            return;
        }
               
        boolean overridedImmediate = immediate;
        packet.encode();
        if (packet.getBuffer().length > 512 && !BatchPacket.class.isAssignableFrom(packet.getClass())) {
            try{
                BatchPacket pkBatch = new BatchPacket();
                pkBatch.payload = Zlib.deflate(Binary.appendBytes((byte) 0xfe, packet.getBuffer()), Network.COMPRESSION_LEVEL);
                sendPacket(pkBatch, overridedImmediate);
                return;
            } catch(Exception e){
                e.printStackTrace();
                return;
            }
        }

        if(BatchPacket.class.isAssignableFrom(packet.getClass())){
            /*try{byte[] bytess = Zlib.inflate(((BatchPacket)packet).payload, 1024 * 1024 * 64);
            //System.out.println("Sending BatchPacket, len="+((BatchPacket)packet).payload.length+", islogin="+(bytess[1] == ProtocolInfo.LOGIN_PACKET ? "yes" : "no"));
            //if(bytess[1] == ProtocolInfo.LOGIN_PACKET){ LoginPacket pk = (LoginPacket)this.getPacket(bytess); pk.decode(); System.out.println(pk.username); System.out.println(pk.clientId); }}catch(Exception e){e.printStackTrace();}*/
        }

        //System.out.println(packet.getClass());

        EncapsulatedPacket encapsulated = new EncapsulatedPacket();
        encapsulated.buffer = Binary.appendBytes((byte) 0xfe, packet.getBuffer());
        encapsulated.needACK = true;
        encapsulated.reliability = (byte) 2;
        encapsulated.messageIndex = 0;
        handler.sendEncapsulated("", encapsulated, (byte) (JRakLib.FLAG_NEED_ACK | (overridedImmediate ? JRakLib.PRIORITY_IMMEDIATE : JRakLib.PRIORITY_NORMAL)));
    }

    public void sendPacket(byte[] bytes, boolean immediate) {
        if (bytes == null) {
            return;
        }
              
        boolean overridedImmediate = immediate;

        //System.out.println("Sending packet "+bytes[0]+" length="+bytes.length+" loginpacket="+(bytes[0] == ProtocolInfo.LOGIN_PACKET ? "yes" : "no"));

        //System.out.println(this.getPacket(bytes).getClass());

        if (bytes.length > 512 && bytes[0] != ProtocolInfo.BATCH_PACKET) {
            try{
                BatchPacket pkBatch = new BatchPacket();
                pkBatch.payload = Zlib.deflate(Binary.appendBytes((byte) 0xfe, bytes), Network.COMPRESSION_LEVEL);
                sendPacket(pkBatch, overridedImmediate);
                return;
            } catch(Exception e){
                e.printStackTrace();
                return;
            }
        }

        EncapsulatedPacket encapsulated = new EncapsulatedPacket();
        encapsulated.buffer = Binary.appendBytes((byte) 0xfe, bytes);
        encapsulated.needACK = true;
        encapsulated.reliability = (byte) 2;
        encapsulated.messageIndex = 0;
        handler.sendEncapsulated("", encapsulated, (byte) (JRakLib.FLAG_NEED_ACK | (overridedImmediate ? JRakLib.PRIORITY_IMMEDIATE : JRakLib.PRIORITY_NORMAL)));
    }

    private DataPacket getPacket(byte[] buffer) {
        byte pid = buffer[0];
        int start = 1;

        if (pid == (byte) 0xfe) {
            pid = buffer[1];
            start++;
        }
        DataPacket data = player.getServer().getNetwork().getPacket(pid);

        if (data == null) {
            return null;
        }

        data.setBuffer(buffer, start);

        return data;
    }

}
