/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.firmata4j.fsm.Parser;

/**
 * Allows connections over the serial interface.
 *
 * @author Ali Kia
 */
public class SerialTransport implements TransportInterface, SerialPortEventListener {

    private Parser parser;

    private final SerialPort port;

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialTransport.class);

    public SerialTransport(String portName) {
        this.port = new SerialPort(portName);
    }

    @Override
    public void start() throws IOException {
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
    public void stop() throws IOException {
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
    public void write(byte[] bytes) throws IOException {
        try {
            port.writeBytes(bytes);
        } catch (SerialPortException ex) {
            throw new IOException("Cannot send message to device", ex);
        }
    }

    @Override
    public void setParser(Parser parser) {
        this.parser = parser;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        // queueing data from input buffer to processing by FSM logic
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                parser.parse(port.readBytes());
            } catch (SerialPortException ex) {
                LOGGER.error("Cannot read from device", ex);
            }
        }
    }

}
