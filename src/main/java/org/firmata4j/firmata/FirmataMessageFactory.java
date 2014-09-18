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

import org.firmata4j.Pin;
import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This class contains methods that help to build command messages for Firmata
 * device.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FirmataMessageFactory {

    /**
     * This message requests firmware version of a Firmata device.
     */
    public static final byte[] REQUEST_FIRMWARE = new byte[]{START_SYSEX, REPORT_FIRMWARE, END_SYSEX};
    /**
     * This message requests capability repot of a Firmata device.
     */
    public static final byte[] REQUEST_CAPABILITY = new byte[]{START_SYSEX, CAPABILITY_QUERY, END_SYSEX};
    /**
     * The analog mapping query provides the information about which pins (as
     * used with Firmata's pin mode message) correspond to the analog channels.
     */
    public static final byte[] ANALOG_MAPPING_REQUEST = new byte[]{START_SYSEX, ANALOG_MAPPING_QUERY, END_SYSEX};

    /**
     * Builds a message that requests a state of specified pin.
     *
     * @param pinId index of the pin
     * @return message that requests a state of the pin
     */
    public static byte[] pinStateRequest(byte pinId) {
        return new byte[]{START_SYSEX, PIN_STATE_QUERY, pinId, END_SYSEX};
    }

    /**
     * Builds message to enable or disable reporting from Firmata device on
     * change of analog input.
     *
     * @param enable message enables analog reporting if true and disable if
     * false
     * @return message that enables or disables analog reporting
     */
    public static byte[] analogReport(boolean enable) {
        byte[] result = new byte[32];
        byte flag = (byte) (enable ? 1 : 0);
        for (byte i = 0, j = 0; i < 16; i++) {
            result[j++] = (byte) (REPORT_ANALOG | i);
            result[j++] = flag;
        }
        return result;
    }

    /**
     * Builds message to enable or disable reporting from Firmata device on
     * change of digital input.
     *
     * @param enable message enables digital reporting if true and disable if
     * false
     * @return message that enables or disables digital reporting
     */
    public static byte[] digitalReport(boolean enable) {
        byte[] result = new byte[32];
        byte flag = (byte) (enable ? 1 : 0);
        for (byte i = 0, j = 0; i < 16; i++) {
            result[j++] = (byte) (REPORT_DIGITAL | i);
            result[j++] = flag;
        }
        return result;
    }

    /**
     * Creates the message that assigns particular mode to specified pin.
     *
     * @param pinId index of the pin
     * @param mode mode
     * @return message that assigns particular mode to specified pin
     */
    public static byte[] setMode(byte pinId, Pin.Mode mode) {
        return new byte[]{SET_PIN_MODE, pinId, (byte) mode.ordinal()};
    }

    /**
     * Creates Firmata message to set digital values of port's pins.<br/>
     * Digital value should be assigned to set of pins at once. A set of pins is
     * called port.<br/>
     * A port contains 8 pins. Digital value of every pin in a set transfers in
     * one byte. Every bit in the byte represents state of pin's output.
     *
     * @param portId index of a port
     * @param value values of port's pins
     * @return Firmata message to set digital output
     */
    public static byte[] setDigitalPinValue(byte portId, byte value) {
        return new byte[]{(byte) (DIGITAL_MESSAGE | (portId & 0x0F)), (byte) (value & 0x7F), (byte) ((value >>> 7) & 0x7F)};
    }

    /**
     * Creates Firmata message to set value of an output pin in PWM mode.<br/>
     * If pin id is beyond 15th or value is greater than we can put into
     * standard analog message, extended analog message is built.
     *
     * @param pinId index of the pin
     * @param value value to be set
     * @return Firmata message to set PWM output
     */
    public static byte[] setAnalogPinValue(byte pinId, long value) {
        byte[] message;
        if (pinId <= 15 && value <= 16383) {
            message = new byte[]{(byte) (ANALOG_MESSAGE | (pinId & 0x0F)), (byte) (value & 0x7F), (byte) ((value >>> 7) & 0x7F)};
        } else {
            message = new byte[]{
                START_SYSEX,
                EXTENDED_ANALOG,
                pinId,
                (byte) (value & 0x7F),
                (byte) ((value >>> 7) & 0x7F),
                (byte) ((value >>> 14) & 0x7F),
                (byte) ((value >>> 21) & 0x7F),
                END_SYSEX
            };
        }
        return message;
    }

    /**
     * Creates a message to set sampling interval.<br/>
     * The sampling interval sets how often analog data and i2c data is reported
     * to the client. The default value is 19 milliseconds.
     *
     * @param value sampling interval in milliseconds
     * @return Firmata message to set sampling interval
     */
    public static byte[] setSamplingInterval(int value) {
        if (value > MAX_SAMPLING_INTERVAL) {
            value = MAX_SAMPLING_INTERVAL;
        } else if (value < MIN_SAMPLING_INTERVAL) {
            value = MIN_SAMPLING_INTERVAL;
        }
        return new byte[]{START_SYSEX, SAMPLING_INTERVAL, (byte) (value & 0x7F), (byte) ((value >>> 7) & 0x7F), END_SYSEX};
    }

    /**
     * Builds servo configuration message.<br/>
     * The core idea is to just add a "config" message, then use
     * {@link #setMode(byte, org.firmata4j.Pin.Mode)} to attach/detach Servo
     * support to a pin. Then the normal {@link #setAnalogPinValue(byte, long)}
     * is used to send data.
     *
     * @param pinId
     * @param minPulse
     * @param maxPulse
     * @return
     */
    public static byte[] servoConfig(byte pinId, int minPulse, int maxPulse) {
        return new byte[]{
            START_SYSEX,
            SERVO_CONFIG,
            pinId, (byte) (minPulse & 0x7F),
            (byte) ((minPulse >>> 7) & 0x7F),
            (byte) (maxPulse & 0x7F),
            (byte) ((maxPulse >>> 7) & 0x7F)
        };
    }
}
