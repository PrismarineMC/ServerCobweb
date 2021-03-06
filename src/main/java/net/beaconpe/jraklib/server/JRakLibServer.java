/**
 * JRakLib is not affiliated with Jenkins Software LLC or RakNet.
 * This software is a port of RakLib https://github.com/PocketMine/RakLib.

 * This file is part of JRakLib.
 *
 * JRakLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JRakLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JRakLib.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.beaconpe.jraklib.server;

import net.beaconpe.jraklib.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * JRakLib server.
 */
public class JRakLibServer extends Thread{
    protected InetSocketAddress _interface;

    protected Logger logger;
    protected boolean shutdown = false;

    protected Queue<byte[]> externalQueue;
    protected Queue<byte[]> internalQueue;

    public JRakLibServer(Logger logger, int port, String _interface){
        if(port < 1 || port > 65536){
            throw new IllegalArgumentException("Invalid port range.");
        }
        this._interface = new InetSocketAddress(_interface, port);
        this.logger = logger;
        this.shutdown = false;

        externalQueue = new ConcurrentLinkedQueue<>();
        internalQueue = new ConcurrentLinkedQueue<>();

        start();
    }

    public boolean isShutdown(){
        return shutdown == true;
    }

    public void shutdown(){
        shutdown = true;
    }

    public int getPort(){
        return _interface.getPort();
    }

    public String getInterface(){
        return _interface.getHostString();
    }

    public Logger getLogger(){
        return logger;
    }

    public Queue<byte[]> getExternalQueue(){
        return externalQueue;
    }

    public Queue<byte[]> getInternalQueue(){
        return internalQueue;
    }

    public void pushMainToThreadPacket(byte[] bytes){
        internalQueue.add(bytes);
    }

    public byte[] readMainToThreadPacket(){
        if(!internalQueue.isEmpty()) {
            return internalQueue.remove();
        }
        return null;
    }

    public void pushThreadToMainPacket(byte[] bytes){
        externalQueue.add(bytes);
    }

    public byte[] readThreadToMainPacket(){
        if(!externalQueue.isEmpty()) {
            return externalQueue.remove();
        }
        return null;
    }

    private class ShutdownHandler extends Thread{
        public void run(){
            if(shutdown != true){
                logger.emergency("JRakLib crashed!");
            }
        }
    }

    public void run(){
        setName("JRakLib Thread #"+getId());
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
        UDPServerSocket socket = new UDPServerSocket(logger, _interface.getPort(), _interface.getHostString());
        new SessionManager(this, socket);
    }
}
