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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.firmata4j.EncoderEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jrkuhn
 */
public class FirmataEncoder implements Encoder {

    private final FirmataDevice device;
    private final byte encoderId;
    private final Set<EncoderEventListener> listeners = Collections.synchronizedSet(new HashSet<EncoderEventListener>());
    private volatile long currentPosition;
    private volatile boolean attached;
    private static final Logger LOGGER = LoggerFactory.getLogger(FirmataDevice.class);

    FirmataEncoder(FirmataDevice device, byte index) {
        this.device = device;
        this.encoderId = index;
        this.attached = false;
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
    public void attach(Pin pinA, Pin pinB) throws IOException, IllegalArgumentException, IllegalStateException {
        if (attached) {
            throw new IllegalStateException(MessageFormat.format("Encoder {0} is already attached", getIndex()));
        }
        boolean willUseInterrupts = pinA.supports(Pin.Mode.ENCODER) || pinB.supports(Pin.Mode.ENCODER);
        if (!willUseInterrupts) {
            LOGGER.warn(MessageFormat.format(
                    "Neither encoder pin ({0}, {1}) supports interrupts. For better performences, you should only use Interrput pins.",
                    pinA.getIndex(), pinB.getIndex()));
        }
        try {
            getDevice().sendMessage(FirmataMessageFactory.encoderAttach(getIndex(), pinA.getIndex(), pinB.getIndex()));
            if (pinA instanceof FirmataPin) {
                // The FirmataEncoder library automatically switches the pin's
                // mode to ENCODER. Update the FirmataPin's mode cache
                FirmataPin firmataPin = (FirmataPin) pinA;
                firmataPin.initMode(Pin.Mode.ENCODER);
            }
            if (pinB instanceof FirmataPin) {
                // The FirmataEncoder library automatically switches the pin's
                // mode to ENCODER. Update the FirmataPin's mode cache
                FirmataPin firmataPin = (FirmataPin) pinA;
                firmataPin.initMode(Pin.Mode.ENCODER);
            }
            attached = true;
        } catch (IOException e) {
            throw new IOException("Cannot attach encoder to device.", e);
        }
    }

    @Override
    public void detach() throws IOException, IllegalStateException {
        if (!attached) {
            throw new IllegalArgumentException("Attempting to detach unattached encoder");
        }
        try {
            getDevice().sendMessage(FirmataMessageFactory.encoderDetach(getIndex()));
            attached = false;
            // NOTE: The encoder pin modes are not changed from ENCODER
        } catch (IOException e) {
            throw new IOException("Cannot detach encoder from device.", e);
        }
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
        return attached;
    }


}
