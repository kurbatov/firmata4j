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
import org.firmata4j.firmata.parser.FirmataToken;

/**
 * A pin is a connector of an {@link IODevice} to external signal receiver or
 * transmitter.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface Pin {

    /**
     * Mode represents particular duty of a pin.
     */
    public static enum Mode {

        /**
         * Digital pin in input mode
         */
        INPUT,
        /**
         * Digital pin in output mode
         */
        OUTPUT,
        /**
         * Analog pin in analog input mode
         */
        ANALOG,
        /**
         * Digital pin in PWM output mode
         */
        PWM,
        /**
         * Digital pin in Servo output mode
         */
        SERVO,
        /**
         * shiftIn/shiftOut mode
         */
        SHIFT,
        /**
         * Pin included in I2C setup
         */
        I2C,
        /**
         * Pin configured for 1-wire
         */
        ONEWIRE,
        /**
         * Pin configured for stepper motor
         */
        STEPPER,
        /**
         * Pin configured for rotary encoders
         */
        ENCODER,
        /**
         * Pin configured for serial communication
         */
        SERIAL,
        /**
         * Enable internal pull-up resistor for pin
         */
        PULLUP,
        
        // add new modes here
        
        /**
         * Indicates a mode that this client library doesn't support
         */
        UNSUPPORTED,
        /**
         * Pin configured to be ignored by digitalWrite and capabilityResponse
         */
        IGNORED;

        /**
         * Resolves a mode-token from firmata message to enum value.
         *
         * @param modeToken token that stands for mode
         * @return mode
         */
        public static Mode resolve(byte modeToken) {
            if (modeToken == FirmataToken.PIN_MODE_IGNORE) {
                return IGNORED;
            }
            if (modeToken > FirmataToken.TOTAL_PIN_MODES) {
                return UNSUPPORTED;
            }
            return values()[modeToken];
        }
    }

    /**
     * Return an {@link IODevice} the pin belongs to.
     *
     * @return the pin's device
     */
    public IODevice getDevice();

    /**
     * Returns the index of the pin on its device.
     *
     * @return the index of the pin
     */
    public byte getIndex();

    /**
     * Returns current mode of the pin.
     *
     * @return current mode of the pin
     */
    public Mode getMode();

    /**
     * Assigns new mode to the pin.
     *
     * @param mode the mode the pin should get into
     * @throws IOException when assigning is failed due a communication issue
     * @throws IllegalArgumentException when the pin does not support the mode
     */
    public void setMode(Mode mode) throws IOException, IllegalArgumentException;

    /**
     * 
     * @param minPulse servo moter control pulse with [µs] on setValue(0)
     * @param maxPulse servo moter control pulse with [µs] on setValue(180)
     * @throws IOException when assigning is failed due a communication issue
     * @throws IllegalArgumentException when the pin does not support the mode
     */
    public void setServoMode(int minPulse, int maxPulse) throws IOException, IllegalArgumentException;

    /**
     * Checks if the pin supports the mode
     *
     * @param mode the mode
     * @return true if the pin supports the mode, false otherwise
     */
    public boolean supports(Mode mode);

    /**
     * Returns a set of modes supported by the pin.
     *
     * @return set of supported modes
     */
    public Set<Mode> getSupportedModes();

    /**
     * Returns current value of the pin.
     *
     * @return current value of the pin
     */
    public long getValue();

    /**
     * Sets the value to the pin. It is impossible to set a value to a pin in
     * input mode such as {@link Mode#INPUT} or {@link Mode#ANALOG}.
     *
     * @param value the value to be assigned to the pin
     * @throws IOException when setting fails due a communication issue
     * @throws IllegalStateException when the pin is in input mode such as
     * {@link Mode#INPUT} or {@link Mode#ANALOG}.
     */
    public void setValue(long value) throws IOException, IllegalStateException;

    /**
     * Adds the specified listener to receive events from this pin.
     *
     * @param listener the listener
     */
    public void addEventListener(PinEventListener listener);

    /**
     * Removes the specified listener so that it no longer receives events from
     * this pin.
     *
     * @param listener the listener
     */
    public void removeEventListener(PinEventListener listener);
    
    
    /**
     * Remove all listeners from this pin.
     *
     */
    public void removeAllEventListeners();


}
