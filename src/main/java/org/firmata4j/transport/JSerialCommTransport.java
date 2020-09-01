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

import java.io.IOException;

import org.firmata4j.Parser;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Implementation of {@link TransportInterface} based on {@link SerialPort}.
 * 
 * @author Norbert Sándor
 */
public class JSerialCommTransport implements TransportInterface
{
    private final SerialPort serialPort;

    private Parser parser;

    public JSerialCommTransport(String portDescriptor)
    {
        serialPort = SerialPort.getCommPort(portDescriptor);
    }

    @Override
    public void start() throws IOException
    {
        if (!serialPort.isOpen())
        {
            if (serialPort.openPort())
            {
                serialPort.setComPortParameters(jssc.SerialPort.BAUDRATE_57600, jssc.SerialPort.DATABITS_8,
                        SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                serialPort.addDataListener(new SerialPortDataListener()
                {
                    @Override
                    public void serialEvent(SerialPortEvent event)
                    {
                        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED)
                        {
                            parser.parse(event.getReceivedData());
                        }
                    }

                    @Override
                    public int getListeningEvents()
                    {
                        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                    }
                });
            } else
            {
                throw new IOException("Cannot start firmata device: port=" + serialPort);
            }
        }
    }

    @Override
    public void stop() throws IOException
    {
        if (serialPort.isOpen() && !serialPort.closePort())
        {
            throw new IOException("Cannot properly stop firmata device: port=" + serialPort);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException
    {
        serialPort.writeBytes(bytes, bytes.length);
    }

    @Override
    public void setParser(Parser parser)
    {
        this.parser = parser;
    }
}
