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

package org.firmata4j.firmata;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.firmata4j.I2CDevice;
import org.firmata4j.I2CEvent;
import org.firmata4j.I2CListener;

/**
 * Represents an I2C device and encapsulates communication logic using Firmata
 * protocol.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FirmataI2CDevice implements I2CDevice {

    private final FirmataDevice masterDevice;

    private final byte address;

    private final AtomicInteger register = new AtomicInteger(1);

    private final AtomicBoolean receivingUpdates = new AtomicBoolean(false);

    private final I2CListener[] callbacks = new I2CListener[256];

    private final Set<I2CListener> subscribers = Collections.synchronizedSet(new HashSet<I2CListener>());

    FirmataI2CDevice(FirmataDevice masterDevice, byte address) {
        this.masterDevice = masterDevice;
        this.address = address;
    }

    @Override
    public byte getAddress() {
        return address;
    }

    @Override
    public void setDelay(int delay) throws IOException {
        masterDevice.setI2CDelay(delay);
    }

    @Override
    public void tell(byte[] data) throws IOException {
        masterDevice.sendMessage(FirmataMessageFactory.i2cWriteRequest(address, data));
    }

    @Override
    public void ask(byte responseLength, I2CListener listener) throws IOException {
        byte reg = (byte) register.getAndIncrement();
        register.compareAndSet(256, 1);
        callbacks[reg - 1] = listener;
        masterDevice.sendMessage(FirmataMessageFactory.i2cReadRequest(address, reg, responseLength, false));
    }

    @Override
    public void subscribe(I2CListener listener) {
        subscribers.add(listener);
    }

    @Override
    public void unsubscribe(I2CListener listener) {
        subscribers.remove(listener);
    }

    @Override
    public void startReceivingUpdates(byte messageLength) throws IOException {
        if (receivingUpdates.compareAndSet(false, true)) {
            masterDevice.sendMessage(FirmataMessageFactory.i2cReadRequest(address, (byte) 0, messageLength, true));
        }
    }

    @Override
    public void stopReceivingUpdates() throws IOException {
        if (receivingUpdates.compareAndSet(true, false)) {
            masterDevice.sendMessage(FirmataMessageFactory.i2cStopContinuousRequest(address));
        }
    }

    /**
     * {@link FirmataDevice} calls this method when receives a message from I2C
     * device.
     * 
     * @param register The register acts as a tag on returned data helping to 
     * identify the matching returned message to the request. Continuous updates
     * are received with 0 register.
     * @param message actual data from I2C device
     */
    void onReceive(byte register, byte[] message) {
        I2CEvent evt = new I2CEvent(this, message);
        if (register > 0) {
            callbacks[register - 1].onReceive(evt);
        } else {
            Set<I2CListener> notificationSet = new HashSet<>(subscribers);
            for (I2CListener listener : notificationSet) {
                listener.onReceive(evt);
            }
        }
    }

}
