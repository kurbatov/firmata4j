package org.firmata4j.firmata.transport;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author ali
 * since 1/7/2018.
 */
public class SerialFirmataTransport extends AbstractFirmataTransport implements SerialPortEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialFirmataTransport.class);
    private final SerialPort port;

    public SerialFirmataTransport(String portName) {
        this.port = new SerialPort(portName);
    }


    @Override
    public void initializeConnector() throws IOException {
        if (!port.isOpened()) {
            try {
                port.openPort();
                port.setParams(
                        SerialPort.BAUDRATE_57600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                port.setEventsMask(SerialPort.MASK_RXCHAR);
                port.addEventListener(this);
            } catch (SerialPortException ex) {
                throw new IOException("Cannot start firmata device", ex);
            }
        }

    }

    @Override
    public void stopTransport() throws IOException {
        try {
            if (port.isOpened()) {
                port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                port.closePort();
            }
        } catch (SerialPortException ex) {
            throw new IOException("Cannot properly stop firmata device", ex);
        }
    }

    @Override
    public void sendMessage(byte[] bytes) throws IOException {
        try {
            port.writeBytes(bytes);
        } catch (SerialPortException ex) {
            throw new IOException("Cannot send message to device", ex);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        // queueing data from input buffer to processing by FSM logic
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                queueDeviceResponse(port.readBytes());
            } catch (SerialPortException ex) {
                LOGGER.error("Cannot read from device", ex);
            }
        }
    }
}
