/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Oleg Kurbatov (o.v.kurbatov@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.firmata4j.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.firmata4j.Parser;

/**
 * Allows connections over the network.
 *
 * @author Thomas Welsch &lt;ttww@gmx.de&gt;
 */
public class NetworkTransport implements TransportInterface {

    private Parser parser;
    private Thread readerThread;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final InetAddress ip;
    private final int port;

    /**
     * Creates network transport using a sting as address.
     *
     * Address should specify host and port. Examples:
     * <ul>
     *   <li>"192.168.1.10:36363"</li>
     *   <li>"explorer-bot.local:72727"</li>
     * </ul>
     * @param address host and port
     */
    public NetworkTransport(String address) {
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Address must specify host and port but received " + address);
        }
        try {
            this.ip = InetAddress.getByName(parts[0]);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
        this.port = Integer.valueOf(parts[1]);
    }
    
    public NetworkTransport(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        socket = new Socket(ip, port);
        socket.setReuseAddress(true);
        socket.setSoTimeout(1500);
        socket.setSoLinger(true, 1500);
        socket.setSoTimeout(1500);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        readerThread = new Thread(new Reader(), "firmata-network-transport");
        readerThread.start();
    }

    @Override
    public void stop() throws IOException {
        try {
            readerThread.interrupt();
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            if (out != null) {
                out.close();
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
        out = null;
        in = null;
        socket = null;

    }

    @Override
    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    @Override
    public void setParser(Parser parser) {
        this.parser = parser;
    }

    private class Reader implements Runnable {

        @Override
        public void run() {
            byte[] buf = new byte[100];
            int readIn;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    readIn = in.read(buf);
                } catch (SocketTimeoutException e) {
                    break;  // We try to reconnect, hearthbeats (1*second) missing
                } catch (IOException e) {
                    break;
                }
                if (readIn == -1) {
                    break;  // Connection closed
                }
                byte[] data = new byte[readIn];
                System.arraycopy(buf, 0, data, 0, readIn);
                parser.parse(data);
            }
        }

    }

}
