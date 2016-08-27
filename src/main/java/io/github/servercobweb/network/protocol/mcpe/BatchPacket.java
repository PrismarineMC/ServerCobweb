package io.github.servercobweb.network.protocol.mcpe;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class BatchPacket extends DataPacket {
    public static final byte NETWORK_ID = ProtocolInfo.BATCH_PACKET;

    public byte[] payload;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.payload = this.get(this.getInt());
    }

    @Override
    public void encode() {
        this.reset();
        this.putInt(this.payload.length);
        this.put(this.payload);
    }
}
