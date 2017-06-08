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

import java.util.Arrays;

/**
 * This event occurs when I2C device transmits some data. {@link I2CListener}
 * processes this kind of events.
 * 
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class I2CEvent {

    private final I2CDevice device;
    private final int register;
    private final byte[] data;

    public I2CEvent(I2CDevice device, int register, byte[] data) {
        this.device = device;
        this.register = register;
        this.data = data;
    }

    /**
     * Returns data received from {@link I2CDevice}.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns register address received from {@link I2CDevice}.
     */
    public int getRegister() {
        return register;
    }

    /**
     * Returns {@link I2CDevice} which sent a piece of data.
     */
    public I2CDevice getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return String.format("I2CEvent [device=%s, register=0x%02X, data=%s]", device, register, Arrays.toString(data));
    }
}
