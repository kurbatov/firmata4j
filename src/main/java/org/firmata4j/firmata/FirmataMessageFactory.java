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

    /* I2C SUPPORT */
    public static byte[] i2CConfigRequest(int delayInMicroseconds) {

        byte delayLsb = (byte) (delayInMicroseconds & 0x7F);
        byte delayMsb = 0;
        if(delayInMicroseconds > 128){
            delayMsb = 1;
        }
        return new byte[]{START_SYSEX, I2C_CONFIG, delayLsb, delayMsb, END_SYSEX};
    }

    /**
     * Structure of an i2C
     *
     * @param slaveAddress
     * @param bytesToWrite
     * @return message that requests i2c write
     */
    public static byte[] i2CWriteRequest(final byte slaveAddress,byte[] bytesToWrite){
        byte[] result = new byte[bytesToWrite.length*2+5];
        result[0] = START_SYSEX;
        result[1] = I2C_REQUEST;
        result[2] = slaveAddress;
        result[3] = I2C_WRITE; // Write Request
        for(int x=0;x<bytesToWrite.length;x++){
            int skipIndex = x * 2;
            result[4+skipIndex] = (byte) (bytesToWrite[x] & 0x7F);
            result[5+skipIndex] = (byte) (((bytesToWrite[x]&0xFF) >>> 7) & 0x7F);
        }
        result[4+bytesToWrite.length*2] = END_SYSEX;
        return result;
    }


    /**
     * Structure of an i2c read request. Does not support 10 bit mode.
     *
     * 0 - SYSEX START
     * 1 - SYSEX COMMAND - 0x76 I2C REQUEST  - ARGV0
     * 2 - I2C MODE - 0x10 Start, 0X18 Stop Continuous Read, 0x08 Single Read - ARGV1
     * 3 - Slave Register LSB - ARGV2
     * 4 - Slave Register MSB - ARGV3
     *
     * @param slaveAddress The 8 bit address of the i2c device you want to talk to.
     * @param slaveRegister The slave register will act as a tag on your returned data enabling you to identify the
     *                      matching returned message to the request.
     * @param bytesToRead the number of bytes that the device will return
     * @param continuous repeatedly send updates until asked to stop
     * @return message that requests i2c read
     */

    public static byte[] i2CReadRequest(final byte slaveAddress,final byte slaveRegister, int bytesToRead, boolean continuous){
        if(continuous){
            if(slaveRegister==0) {
                return new byte[]{
                        START_SYSEX, I2C_REQUEST,
                        slaveAddress, I2C_READ_CONTINUOUS,
                        (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                        END_SYSEX
                };
            } else {
                return new byte[]{
                        START_SYSEX, I2C_REQUEST,
                        slaveAddress, I2C_READ_CONTINUOUS,
                        (byte) (slaveRegister & 0x7F), (byte) ((slaveRegister >>> 7) & 0x7F),
                        (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                        END_SYSEX
                };
            }
        } else {
            if(slaveRegister==0) {
                return new byte[]{START_SYSEX, I2C_REQUEST,
                        slaveAddress, I2C_READ,
                        (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                        END_SYSEX
                };
            } else {
                return new byte[]{START_SYSEX, I2C_REQUEST,
                        slaveAddress, I2C_READ,
                        (byte) (slaveRegister & 0x7F), (byte) ((slaveRegister >>> 7) & 0x7F),
                        (byte) (bytesToRead & 0x7F), (byte) ((bytesToRead >>> 7) & 0x7F),
                        END_SYSEX
                };

            }
        }
    }

    public static byte[] i2CStopContinuousRequest(final byte slaveAddress){
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
     * @return message that requests servo configuration
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
     * Builds an encoder attach message.
     *
     * @param encoderId index of the encoder to attach
     * @param pinAId index of the first pin to attach to the encoder
     * @param pinBId index of the second pin to attach to the encoder
     * @return Firmata SysEx message
     */
    public static byte[] encoderAttach(byte encoderId, byte pinAId, byte pinBId) {
        return new byte[]{START_SYSEX, ENCODER_DATA, ENCODER_ATTACH,
            encoderId, pinAId, pinBId, END_SYSEX
        };
    }
    
    /**
     * Builds an encoder report position request message.
     *
     * @param encoderId index of the encoder to report
     * @return Firmata SysEx message
     */
    public static byte[] encoderReportPosition(byte encoderId) {
        return new byte[]{START_SYSEX, ENCODER_DATA, ENCODER_REPORT_POSITION,
            encoderId, END_SYSEX};
    }
    
    /**
     * Builds an encoder reset position request message.
     *
     * @param encoderId index of the encoder to reset
     * @return Firmata SysEx message
     */
    public static byte[] encoderResetPosition(byte encoderId) {
        return new byte[]{START_SYSEX, ENCODER_DATA, ENCODER_RESET_POSITION,
            encoderId, END_SYSEX};
    }
    
    /**
     * Builds an encoder auto report position request message.
     *
     * @param enable enables auto-reporting once per sampling interval if true
     * @return Firmata SysEx message
     */
    public static byte[] encoderReport(boolean enable) {
        return new byte[]{START_SYSEX, ENCODER_DATA, ENCODER_REPORT_AUTO,
            (byte) (enable ? 1 : 0), END_SYSEX};
    }
    
    /**
     * Builds an encoder detach message.
     *
     * @param encoderId index of the encoder to detach
     * @return Firmata SysEx message
     */
    public static byte[] encoderDetach(byte encoderId) {
        return new byte[]{START_SYSEX, ENCODER_DATA, ENCODER_DETACH,
            encoderId, END_SYSEX
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
