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
 */package org.firmata4j.firmata;

import org.firmata4j.Encoder;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.firmata4j.EncoderEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;

/**
 *
 * @author jrkuhn
 */
public class FirmataEncoder implements Encoder {
    private final FirmataDevice device;
    private final byte encoderId;
    private final Set<EncoderEventListener> listeners = Collections.synchronizedSet(new HashSet<EncoderEventListener>());
    private volatile long currentPosition;

    FirmataEncoder(FirmataDevice device, byte index) {
        this.device = device;
        this.encoderId = index;
    }
    
    @Override
    public FirmataDevice getDevice() {
        return device;
    }

    @Override
    public byte getIndex() {
        return encoderId;
    }
    
    @Override
    public long getPosition() {
        return currentPosition;
    }
    
    @Override
    public synchronized void resetPosition() throws IOException, IllegalStateException {
        long newPosition = 0;
        byte[] message = FirmataMessageFactory.encoderResetPosition(encoderId);
        if (currentPosition != newPosition) {
            device.sendMessage(message);
            updatePosition(newPosition);
        }
    }

    @Override
    public void addEventListener(EncoderEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(EncoderEventListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void attach(Pin a, Pin b) throws IOException, IllegalArgumentException, IllegalStateException {
        device.attachEncoder(this, a, b);
    }
    
    
    synchronized void updatePosition(long position) {
        if (position != currentPosition) {
            currentPosition = position;
            IOEvent evt = new IOEvent(this);
            getDevice().encoderChanged(evt); // the device listeners receive the event first
            for (EncoderEventListener listener : listeners) { // then pin listeners receive the event
                listener.onPositionChange(evt);
            }
        }
    }

    @Override
    public boolean isAttached() {
        return getDevice().isAttached(this);
    }
    
}
