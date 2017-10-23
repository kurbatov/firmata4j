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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;

/**
 * This class contains implementation of Firmata pin.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FirmataPin implements Pin {

    private final FirmataDevice device;
    private final byte pinId;
    private final Set<Mode> supportedModes = Collections.synchronizedSet(EnumSet.noneOf(Mode.class));
    private final Set<PinEventListener> listeners = Collections.synchronizedSet(new HashSet<PinEventListener>());
    private volatile Mode currentMode;
    private volatile long currentValue;

    /**
     * Constructs Firmata pin for the specified device.
     *
     * @param device the device the pin belongs to
     * @param index the index of pin on the device
     */
    public FirmataPin(FirmataDevice device, byte index) {
        this.device = device;
        this.pinId = index;
    }

    @Override
    public FirmataDevice getDevice() {
        return device;
    }

    @Override
    public byte getIndex() {
        return pinId;
    }

    @Override
    public Mode getMode() {
        return currentMode;
    }

    @Override
    public void setMode(Mode mode) throws IOException {
        setMode(mode, 544, 2400); // Arduino defaults (https://www.arduino.cc/en/Reference/ServoAttach)
    }

    @Override
    public void setServoMode(int minPulse, int maxPulse) throws IOException {
        setMode(Mode.SERVO, minPulse, maxPulse);
    }

    private synchronized void setMode(Mode mode, int minPulse, int maxPulse) throws IOException {
        if (supports(mode)) {
            if (currentMode != mode) {
                if (mode == Mode.SERVO) {
                    getDevice().sendMessage(FirmataMessageFactory.servoConfig(pinId, minPulse, maxPulse));
                    // The currentValue for a servo is unknown as the motor is 
                    // send to the 1.5ms position when pinStateRequest is invoked
                    currentValue = -1;
                }
                getDevice().sendMessage(FirmataMessageFactory.setMode(pinId, mode));
                currentMode = mode;
                IOEvent evt = new IOEvent(this);
                getDevice().pinChanged(evt);
                for (PinEventListener listener : listeners) {
                    listener.onModeChange(evt);
                }
                getDevice().sendMessage(FirmataMessageFactory.pinStateRequest(pinId));
            }
        } else {
            throw new IllegalArgumentException(String.format("Pin %d does not support mode %s", pinId, mode));
        }
    }

    @Override
    public boolean supports(Mode mode) {
        return supportedModes.contains(mode);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Mode> getSupportedModes() {
        if (supportedModes.isEmpty()) {
            return Collections.EMPTY_SET;
        } else {
            return EnumSet.copyOf(supportedModes);
        }
    }

    @Override
    public long getValue() {
        return currentValue;
    }

    @Override
    public synchronized void setValue(long value) throws IOException, IllegalStateException {
        byte[] message;
        long newValue;
        if (currentMode == Mode.OUTPUT) {
            //have to calculate the value of whole port (8-pin set) the pin sits in
            byte portId = (byte) (pinId / 8);
            byte pinInPort = (byte) (pinId % 8);
            byte portValue = 0;
            for (int i = 0; i < 8; i++) {
                Pin p = device.getPin(portId * 8 + i);
                if (p.getMode() == Mode.OUTPUT && p.getValue() > 0) {
                    portValue |= 1 << i;
                }
            }
            byte bitmask = (byte) (1 << pinInPort);
            boolean val = value > 0;
            if (val) {
                portValue |= bitmask;
            } else {
                portValue &= ((byte) ~bitmask);
            }
            message = FirmataMessageFactory.setDigitalPinValue(portId, portValue);
            newValue = val ? 1 : 0;
        } else if (currentMode == Mode.ANALOG || currentMode == Mode.PWM || currentMode == Mode.SERVO) {
            message = FirmataMessageFactory.setAnalogPinValue(pinId, value);
            newValue = value;
        } else {
            throw new IllegalStateException(String.format("Port %d is in %s mode and its value cannot be set.", pinId, currentMode));
        }
        if (currentValue != newValue) {
            device.sendMessage(message);
            updateValue(value);
        }
    }

    @Override
    public void addEventListener(PinEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(PinEventListener listener) {
        listeners.remove(listener);
    }
    
    
    @Override
    public void removeAllEventListeners() {
        listeners.clear();
    }
    

    /**
     * Adds supported mode to the pin.
     *
     * @param mode the mode
     */
    void addSupprotedMode(Mode mode) {
        supportedModes.add(mode);
    }

    /**
     * Sets initial mode of a pin. This method bypasses standard
     * {@link #setMode(com.codefactory.firmata4j.Pin.Mode)} to avoid sending a
     * message to hardware.
     *
     * @param mode initial mode
     */
    synchronized void initMode(Mode mode) {
        currentMode = mode;
    }

    /**
     * Sets initial value of a pin. This method bypasses standard
     * {@link #setValue(long)} to avoid sending a message to hardware.
     *
     * @param value initial value
     */
    synchronized void initValue(long value) {
        currentValue = value;
    }

    /**
     * Permits the {@link FirmataDevice} to update input pin value.
     *
     * @param value the new value
     */
    synchronized void updateValue(long value) {
        if (value != currentValue) {
            currentValue = value;
            IOEvent evt = new IOEvent(this);
            getDevice().pinChanged(evt); // the device listeners receive the event first
            for (PinEventListener listener : listeners) { // then pin listeners receive the event
                listener.onValueChange(evt);
            }
        }
    }
    
}
