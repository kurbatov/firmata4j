package org.firmata4j.firmata.transport;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * thew hardware than Firmata is implemented on
 */
public abstract class AbstractFirmataTransport implements FirmataTransportInterface {

    /**
     * queue of the received messages.
     * any device will just queue received data here and leave the rest to FirmataDevice
     */
    private BlockingQueue<byte[]> byteQueue;

    /**
     * Device connector need to know where to put received data on.
     * if must be present before starting the device.
     *
     * @param byteQueue byte queue from frimataDevice
     */
    public final void setByteQueue(BlockingQueue<byte[]> byteQueue) {
        this.byteQueue = byteQueue;
    }

    protected void queueDeviceResponse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        while (!byteQueue.offer(bytes)) {
            // trying to place bytes to queue until it succeeds
            //this implementation can cause issue with concurrent messages.
        }
    }

    @Override
    public final void startTransport() throws IOException {
        if (byteQueue == null) {
            throw new IllegalStateException("byteQueue was not set before starting device");
        }
        initializeConnector();
    }

    protected abstract void initializeConnector() throws IOException;
}
