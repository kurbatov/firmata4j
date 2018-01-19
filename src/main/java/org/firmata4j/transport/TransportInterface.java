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

import java.io.IOException;
import org.firmata4j.fsm.Parser;

/**
 * Interface to abstract the device connection (serial/network) to a device.
 *
 * @author Ali Kia
 */
public interface TransportInterface {

    /**
     * Starts the transport and initializes the connector.
     *
     * @throws java.io.IOException
     */
    void start() throws IOException;

    /**
     * Shuts down the connector and stops the transport.
     *
     * @throws java.io.IOException
     */
    void stop() throws IOException;

    /**
     * Sends data to the device.
     *
     * @param bytes data to send
     * @throws java.io.IOException
     */
    void write(byte[] bytes) throws IOException;

    /**
     * Sets the parser. Transport transmits received data to the parser.
     *
     * @param parser data parser
     */
    void setParser(Parser parser);
}
