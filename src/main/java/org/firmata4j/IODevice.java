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
import java.util.Set;

/**
 * This interface describes a device which is able to receive and transmit
 * signals. It has a set of pins that represent connections of the device to
 * external signal receivers and transmitters.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface IODevice {

    /**
     * Initiates communication with hardware.<br/>
     * This method initialize the {@link IODevice} instance. Initialization may
     * take some time depending on speed of communication to hardware.<br/>
     * To check whether the device is ready, use {@link #isReady()}.<br/>
     * If you want to wait until the device is initializing and continue working
     * with it, use {@link #ensureInitializationIsDone()}.<br/>
     * If you develop application in asynchronous style, you may want to
     * register an event listener that will receive a message when the device is
     * ready.
     *
     * @throws IOException when communication cannot be established
     * @see IODeviceEventListener#onStart(org.firmata4j.IOEvent)
     */
    public void start() throws IOException;

    /**
     * Terminates communication with hardware.<br/>
     * When communication is terminated, an event is published to
     * {@link IODeviceEventListener}s.
     *
     * @throws IOException when communication cannot be properly terminated
     * @see IODeviceEventListener#onStop(org.firmata4j.IOEvent)
     */
    public void stop() throws IOException;

    /**
     * Waits for initialization is done.<br/>
     * Use this method if you want to be sure that device is ready to
     * communicate.
     *
     * @throws InterruptedException when the device cannot be started or waiting
     * for initialization is interrupted or connection has not been established
     * during timeout.
     */
    public void ensureInitializationIsDone() throws InterruptedException;

    /**
     * Checks whether the device is fully initialized and ready to use.
     *
     * @return true if device is ready, false otherwise
     */
    public boolean isReady();

    /**
     * Returns a set of pins of the device.
     *
     * @return set of device's pins
     * @see Pin
     */
    public Set<Pin> getPins();

    /**
     * Returns count of pins of the device.
     *
     * @return count of pins
     */
    public int getPinsCount();

    /**
     * Returns a pin by its index on device. The index should be less than
     * result returned by {@link #getPinsCount()}.
     *
     * @param index index of the pin
     * @return the pin
     */
    public Pin getPin(int index);
    
    /**
     * Returns I2C device by its address.
     * 
     * @param address
     * @return I2C device
     * @throws IOException when communication to the IO device failed
     */
    public I2CDevice getI2CDevice(byte address) throws IOException;

    /**
     * Adds the specified listener to receive events from this device.
     *
     * @param listener the listener
     */
    public void addEventListener(IODeviceEventListener listener);

    /**
     * Removes the specified listener so that it no longer receives events from
     * this device.
     *
     * @param listener the listener
     */
    public void removeEventListener(IODeviceEventListener listener);

    /**
     * Returns the name of a protocol that the device uses.
     *
     * @return the name of a protocol
     */
    public String getProtocol();
    
    /**
     * Sends arbitrary message to device.
     * 
     * @param message the message
     * @throws IOException when sending a message fails
     */
    public void sendMessage(String message) throws IOException;

}
