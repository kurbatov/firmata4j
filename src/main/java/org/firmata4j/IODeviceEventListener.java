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

/**
 * This interface describes an object that can receive and handle events from an
 * {@link IODevice}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface IODeviceEventListener {

    /**
     * Invoked when an {@link IODevice} has been successfully started
     * and initialized.
     *
     * @param event the event
     */
    public void onStart(IOEvent event);

    /**
     * Invoked when communication with {@link IODevice} has been 
     * successfully terminated.
     *
     * @param event the event
     */
    public void onStop(IOEvent event);

    /**
     * Invoked when the state of one of device's pins has been changed.
     * It can be change of mode or a value.
     *
     * @param event the event
     */
    public void onPinChange(IOEvent event);

    /**
     * Invoked when a string message has been received from the device.
     * 
     * @param event the event
     * @param message the message
     */
    public void onMessageReceive(IOEvent event, String message);

}
