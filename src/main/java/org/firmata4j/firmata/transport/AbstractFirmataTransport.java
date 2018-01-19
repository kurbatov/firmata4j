package org.firmata4j.firmata.transport;

import org.firmata4j.firmata.FirmataParser;

import java.io.IOException;

/**
 * thew hardware than Firmata is implemented on
 */
public abstract class AbstractFirmataTransport implements FirmataTransportInterface {

    private FirmataParser parser;

    protected final void queueDeviceResponse(byte[] bytes) {
        parser.parse(bytes);
    }

    @Override
    public final void startTransport() throws IOException {
        if (parser == null) {
            throw new IOException("parser was not set before starting transport");
        }
        initializeConnector();
    }

    protected abstract void initializeConnector() throws IOException;

    @Override
    public void setParser(FirmataParser parser) {
        this.parser = parser;
    }
}
