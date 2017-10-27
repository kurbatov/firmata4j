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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public static final int REGISTER_NOT_SET = -1;

    private final FirmataDevice masterDevice;

    private final byte address;

    private final AtomicBoolean receivingUpdates = new AtomicBoolean(false);

    private final Map<Integer, I2CListener> callbacks = new ConcurrentHashMap<>();

    private final Set<I2CListener> subscribers = new ConcurrentSkipListSet<>();

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
    public void tell(byte... data) throws IOException {
        masterDevice.sendMessage(FirmataMessageFactory.i2cWriteRequest(address, data));
    }

    @Override
    public void ask(byte responseLength, I2CListener listener) throws IOException {
        ask(REGISTER_NOT_SET, responseLength, listener);
    }

    @Override
    public void ask(int register, byte responseLength, I2CListener listener) throws IOException {
        callbacks.put(register, listener);
        masterDevice.sendMessage(FirmataMessageFactory.i2cReadRequest(address, register, responseLength, false));
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
    public boolean startReceivingUpdates(int register, byte messageLength) throws IOException {
        boolean result = receivingUpdates.compareAndSet(false, true);
        if (result) {
            masterDevice.sendMessage(FirmataMessageFactory.i2cReadRequest(address, register, messageLength, true));
        }
        return result;
    }
    
    @Override
    public boolean startReceivingUpdates(byte messageLength) throws IOException {
        boolean result = receivingUpdates.compareAndSet(false, true);
        if (result) {
            masterDevice.sendMessage(FirmataMessageFactory.i2cReadRequest(address, REGISTER_NOT_SET, messageLength, true));
        }
        return result;
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
     * @param register The device register read from.
     * @param message actual data from I2C device
     */
    void onReceive(int register, byte[] message) {
        I2CEvent evt = new I2CEvent(this, register, message);
        I2CListener listener = callbacks.remove(register);
        if (listener == null) {
            for (I2CListener subscriber : subscribers) {
                subscriber.onReceive(evt);
            }
        } else {
            listener.onReceive(evt);
        }
    }

    @Override
    public String toString() {
        return String.format("FirmataI2CDevice [address=0x%02X]", address);
    }

}
