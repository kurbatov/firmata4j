/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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
package org.firmata4j;

import java.io.IOException;
import org.firmata4j.firmata.FirmataDevice;

/**
 * An encoder is a connector of an {@link IODevice} to a rotary encoder.
 * 
 * Use {@link attach} to attach the encoder to two digital pins. Ideally,
 * one or both of the pins should be interrupt capable to minimize the
 * chance of missing a position change. Interrupt capable pins have the
 * {@link Pin.Mode.ENCODER} flag set. Having both pins on an interrupt
 * minimizes the chances of missing an encoder step.
 * 
 * However, the Arduino Encoder library can work with any digital pin by polling.
 *
 * @author Jeffrey Kuhn &lt;drjrkuhn@gmail.com&gt;
 */
public interface Encoder {

    /**
     * Return the {@link IODevice} the encoder belongs to.
     *
     * @return the encoder's device
     */
    FirmataDevice getDevice();

    /**
     * Returns the index of the encoder on its device.
     *
     * @return the index of the encoder
     */
    byte getIndex();

    /**
     * Returns current position of the encoder.
     *
     * @return current position of the encoder
     */
    long getPosition();

    /**
     * Resets the current position of the encoder to zero.
     */
    void resetPosition() throws IOException, IllegalStateException;
    
    /**
     * Attaches an encoder to two digital pins. 
     * 
     * Once attached, the encoder will start sending position data
     * 
     * @param pinA  first pin to attach encoder to
     * @param pinB  second pin to attach encoder to
     */
    void attach(Pin pinA, Pin pinB) throws IOException, IllegalArgumentException, IllegalStateException;
    
    /**
     * Detach an encoder from its digital pins and halt position data.
     */
    void detach() throws IOException, IllegalStateException;
    
    /**
     * Determine whether encoder is attached.
     * 
     * @return attached state of encoder
     */
    boolean isAttached();
    
    /**
     * Adds the specified listener to receive events from this encoder.
     *
     * @param listener the listener
     */
    void addEventListener(EncoderEventListener listener);

    /**
     * Removes the specified listener so that it no longer receives events from
     * this encoder.
     *
     * @param listener the listener
     */
    void removeEventListener(EncoderEventListener listener);

}
