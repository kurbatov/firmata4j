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
    public static final byte[] REQUEST_FIRMWARE = {START_SYSEX, REPORT_FIRMWARE, END_SYSEX};
    /**
     * This message requests capability repot of a Firmata device.
     */
    public static final byte[] REQUEST_CAPABILITY = {START_SYSEX, CAPABILITY_QUERY, END_SYSEX};
    /**
     * The analog mapping query provides the information about which pins (as
     * used with Firmata's pin mode message) correspond to the analog channels.
     */
    public static final byte[] ANALOG_MAPPING_REQUEST = {START_SYSEX, ANALOG_MAPPING_QUERY, END_SYSEX};

    /* I2C SUPPORT */
    /**
     * Creates SysEx message that configures I2C setup.
     *
     * @param delayInMicroseconds delay between the moment a register is written
     * to and the moment when the data can be read from that register (optional,
     * when I2C device requires a delay)
     * @return message that configures I2C
     */
    public static byte[] i2cConfigRequest(int delayInMicroseconds) {
        if (delayInMicroseconds < 0) {
            throw new IllegalArgumentException("Delay cannot be less than 0 microseconds.");
        }
        if (delayInMicroseconds > 255) {
            throw new IllegalArgumentException("Delay cannot be greater than 255 microseconds.");
        }
        byte delayLsb = (byte) (delayInMicroseconds & 0x7F);
        byte delayMsb = 0;
        if (delayInMicroseconds > 128) {
            delayMsb = 1;
        }
        return new byte[]{START_SYSEX, I2C_CONFIG, delayLsb, delayMsb, END_SYSEX};
    }

    /**
     * Builds a message that asks a Firmata device to send specified data to
     * specified I2C device. <b>Does not support 10-bit mode.</b>
     *
     * @param slaveAddress address of the I2C device you want to talk to
     * @param bytesToWrite data to send to the slaveAdderss
     * @return the message
     */
    public static byte[] i2cWriteRequest(byte slaveAddress, byte... bytesToWrite) {
        byte[] result = new byte[bytesToWrite.length * 2 + 5];
        result[0] = START_SYSEX;
        result[1] = I2C_REQUEST;
        result[2] = slaveAddress;
        result[3] = I2C_WRITE;
        //TODO replace I2C_WRITE with generated slave address (MSB) to support 10-bit mode
        // see https://github.com/firmata/protocol/blob/master/i2c.md
        for (int x = 0; x < bytesToWrite.length; x++) {
            int skipIndex = x * 2 + 4;
            result[skipIndex] = (byte) (bytesToWrite[x] & 0x7F);
            result[skipIndex + 1] = (byte) (((bytesToWrite[x] & 0xFF) >>> 7) & 0x7F);
        }
        result[result.length - 1] = END_SYSEX;
        return result;
    }

    /**
     * Builds a message that asks a Firmata device to read data from an I2C
     * device. <b>Does not support 10-bit mode.</b>
     *
     * @param slaveAddress address of the I2C device you want to talk to
     * @param register The device register to read from.
     * @param bytesToRead the number of bytes that the device will return
     * @param continuous repeatedly send updates until asked to stop
     * @return the message
     */
    public static byte[] i2cReadRequest(byte slaveAddress, int register, int bytesToRead, boolean continuous) {
        byte command;
        byte[] message;
        if (continuous) {
            command = I2C_READ_CONTINUOUS;
        } else {
            command = I2C_READ;
        }
        //TODO replace hardcoded slave address (MSB) with generated one to support 10-bit mode
        // see https://github.com/firmata/protocol/blob/master/i2c.md
        if (register == FirmataI2CDevice.REGISTER_NOT_SET) {
            message = new byte[]{
                START_SYSEX, I2C_REQUEST,
                slaveAddress, command,
                (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                END_SYSEX
            };
        } else {
            message = new byte[]{
                START_SYSEX, I2C_REQUEST,
                slaveAddress, command,
                (byte) (register & 0x7F), (byte) ((register >>> 7) & 0x7F),
                (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                END_SYSEX
            };
        }
        return message;
    }

    /**
     * Builds a message that terminates receiving continuous updates from
     * specified I2C device.
     *
     * @param slaveAddress address of the I2C device you want to shut up
     * @return the message
     */
    public static byte[] i2cStopContinuousRequest(byte slaveAddress) {
        return new byte[]{START_SYSEX, I2C_REQUEST, slaveAddress, I2C_STOP_READ_CONTINUOUS, END_SYSEX};
    }

    /* I2C SUPPORT ENDS */

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
        if (mode == Pin.Mode.UNSUPPORTED) {
            throw new IllegalArgumentException("Cannot set unsupported mode to pin " + pinId);
        }
        return new byte[]{SET_PIN_MODE, pinId, (byte) mode.ordinal()};
    }

    /**
     * Creates Firmata message to set digital values of port's pins.<br/>
     * Digital value should be assigned to a set of pins at once. A set of pins
     * is called port.<br/>
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

    /**
     * Encodes the string as a SysEx message.
     *
     * @param message string message
     * @return SysEx message
     */
    public static byte[] stringMessage(String message) {
        byte[] bytes = message.getBytes();
        byte[] result = new byte[bytes.length * 2 + 3];
        result[0] = START_SYSEX;
        result[1] = STRING_DATA;
        result[result.length - 1] = END_SYSEX;
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result[i * 2 + 2] = (byte) (b & 0x7F);
            result[i * 2 + 3] = (byte) ((b >> 7) & 0x7F);
        }
        return result;
    }
}
