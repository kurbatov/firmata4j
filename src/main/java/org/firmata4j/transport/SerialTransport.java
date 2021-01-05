/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.firmata4j.Parser;

/**
 * Transfers data over the serial interface.
 * 
 * Using an instance of this transport requires one of the following libraries
 * present in the classpath:
 * 
 * <pre>
 *  &lt;dependency&gt;
        &lt;groupId&gt;com.fazecast&lt;/groupId&gt;
        &lt;artifactId&gt;jSerialComm&lt;/artifactId&gt;
        &lt;version&gt;2.6.2&lt;/version&gt;
    &lt;/dependency&gt;
 *  &lt;dependency&gt;
        &lt;groupId&gt;org.scream3r&lt;/groupId&gt;
        &lt;artifactId&gt;jssc&lt;/artifactId&gt;
        &lt;version&gt;2.8.0&lt;/version&gt;
    &lt;/dependency&gt;
 * </pre>
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class SerialTransport implements TransportInterface {

    private TransportInterface delegate;

    private static final Logger LOGGER = LoggerFactory.getLogger(SerialTransport.class);

    public SerialTransport(String portName) {
        try {
            Class.forName("com.fazecast.jSerialComm.SerialPort", false, this.getClass().getClassLoader());
            delegate = new JSerialCommTransport(portName);
            LOGGER.debug("Using jSerialComm transport");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("jssc.SerialPort", false, this.getClass().getClassLoader());
                delegate = new JSerialCommTransport(portName);
                LOGGER.debug("Using jssc transport");
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(
                        "Serial communication library is not found in the classpath. "
                        + "Please make sure that there is at least one dependency "
                        + "as described in the javadoc of org.firmata4j.transport.SerialTransport");
            }
        }
    }

    @Override
    public void start() throws IOException {
        delegate.start();
    }

    @Override
    public void stop() throws IOException {
        delegate.stop();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        delegate.write(bytes);
    }

    @Override
    public void setParser(Parser parser) {
        delegate.setParser(parser);
    }

}
