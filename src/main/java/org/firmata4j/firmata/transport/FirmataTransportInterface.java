package org.firmata4j.firmata.transport;

import java.io.IOException;

/**
 * @author akia
 * @since 2018-01
 */
public interface FirmataTransportInterface {

    /**
     * start transport and initialize connector
     */
    void startTransport() throws IOException;

    /**
     * shutdown connector and stop the transport
     */
    void stopTransport() throws IOException;

    /**
     * send a message to device
     *
     * @param bytes data to send
     */
    void sendMessage(byte[] bytes) throws IOException;

}
