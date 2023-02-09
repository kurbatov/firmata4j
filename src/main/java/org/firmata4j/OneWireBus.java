/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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

/**
 * Allows interactions with with OneWire devices attached to the same bus.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface OneWireBus {
    
    /**
     * Seraces for all devices on the bus.
     * @return array of identifiers for available devices
     */
    int[] search();
    
    /**
     * Searches for devices on the bus.
     * @param alarmedOnly if only identifiers for alarmed devices to be found
     * @return array of device identifiers
     */
    int[] search(boolean alarmedOnly);
    
    /**
     * Configures this OneWire bus.
     *
     * @param enableParasiticPower whether to leave the pin on the high state
     * after write if the data pin has to power the bus
     */
    void config(boolean enableParasiticPower);
    
    /**
     * Resets all devices on the bus.
     */
    void reset();
    
    /**
     * Reads data from specfied device.
     *
     * @param device device id obtained with {@link #search}
     * @return data from device
     */
    byte[] read(byte device);
    
    /**
     * Sends data to specified device.
     *
     * @param device device id obtained with {@link #search}
     * @param data data to send
     */
    void write(byte device, byte ... data);
    
    /**
     * Sends data to specified device and requests data from the same device in
     * the same interaction.
     *
     * @param device device id obtained with {@link #search}
     * @param data data to send
     * @return data from device
     */
    byte[] writeAndRead(byte device, byte ... data);
    
    /**
     * Avoid doing anything for the specified amount of time.
     * 
     * Use to let a device perform calculation.
     * 
     * @param delay time in milliseconds
     */
    void delay(int delay);
    
}
