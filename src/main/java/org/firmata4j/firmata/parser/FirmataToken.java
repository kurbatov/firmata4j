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

package org.firmata4j.firmata.parser;

/**
 * This class contains set of constants that represent tokens of Firmata 
 * protocol.<br/>
 * In addition it contains headers and keys of events that occur when Firmata 
 * messages are received.
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface FirmataToken {
    
    /**
     * Version numbers for the protocol. The protocol is still changing, so these
     * version numbers are important.  This number can be queried so that host
     * software can test whether it will be compatible with the currently
     * installed firmware.
     */
    public static final byte FIRMATA_MAJOR_VERSION  = 2; // for non-compatible changes
    public static final byte FIRMATA_MINOR_VERSION  = 3; // for backwards compatible changes
    public static final byte FIRMATA_BUGFIX_VERSION = 1; // for bugfix releases
    
    // message command bytes (128-255/0x80-0xFF)
    
    /**
     * send data for a digital pin
     */
    public static final byte DIGITAL_MESSAGE         = (byte) 0x90; 
    
    /**
     * send data for an analog pin (or PWM)
     */
    public static final byte ANALOG_MESSAGE          = (byte) 0xE0;
    
    /**
     * enable analog input by pin #
     */
    public static final byte REPORT_ANALOG           = (byte) 0xC0;
    
    /**
     * enable digital input by port pair
     */
    public static final byte REPORT_DIGITAL          = (byte) 0xD0;
    
    /**
     * set a pin to INPUT/OUTPUT/PWM/etc
     */
    public static final byte SET_PIN_MODE            = (byte) 0xF4;
    
    /**
     * set value of an individual digital pin
     */
    public static final byte SET_DIGITAL_PIN_VALUE   = (byte) 0xF5;
    
    /**
     * report protocol version
     */
    public static final byte REPORT_VERSION          = (byte) 0xF9;
    
    /**
     * reset from MIDI
     */
    public static final byte SYSTEM_RESET            = (byte) 0xFF;
    
    /**
     * start a MIDI Sysex message
     */
    public static final byte START_SYSEX             = (byte) 0xF0;
    
    /**
     * end a MIDI Sysex message
     */
    public static final byte END_SYSEX               = (byte) 0xF7;

    // extended command set using sysex (0-127/0x00-0x7F)
    /* 0x00-0x0F reserved for user-defined commands */
    public static final byte RESERVED_COMMAND        = 0x00; // 2nd SysEx data byte is a chip-specific command (AVR, PIC, TI, etc).
    public static final byte SERIAL_MESSAGE          = 0x60; // communicate with serial devices, including other boards
    public static final byte ENCODER_DATA            = 0x61; // reply with encoders current positions
    public static final byte SERVO_CONFIG            = 0x70; // set max angle, minPulse, maxPulse, freq
    public static final byte STRING_DATA             = 0x71; // a string message with 14-bits per byte
    public static final byte STEPPER_DATA            = 0x72; // control a stepper motor
    public static final byte ONEWIRE_DATA            = 0x73; // send an OneWire read/write/reset/select/skip/search request
    public static final byte SHIFT_DATA              = 0x75; // a bitstream to/from a shift register
    public static final byte I2C_REQUEST             = 0x76; // send an I2C read/write request
    public static final byte I2C_REPLY               = 0x77; // a reply to an I2C read request
    public static final byte I2C_CONFIG              = 0x78; // config I2C settings such as delay times and power pins
    public static final byte EXTENDED_ANALOG         = 0x6F; // analog write (PWM, Servo, etc) to any pin
    public static final byte PIN_STATE_QUERY         = 0x6D; // ask for a pin's current mode and value
    public static final byte PIN_STATE_RESPONSE      = 0x6E; // reply with pin's current mode and value
    public static final byte CAPABILITY_QUERY        = 0x6B; // ask for supported modes and resolution of all pins
    public static final byte CAPABILITY_RESPONSE     = 0x6C; // reply with supported modes and resolution
    public static final byte ANALOG_MAPPING_QUERY    = 0x69; // ask for mapping of analog to pin numbers
    public static final byte ANALOG_MAPPING_RESPONSE = 0x6A; // reply with mapping info
    public static final byte REPORT_FIRMWARE         = 0x79; // report name and version of the firmware
    public static final byte SAMPLING_INTERVAL       = 0x7A; // set the poll rate of the main loop
    public static final byte SCHEDULER_DATA          = 0x7B; // send a createtask/deletetask/addtotask/schedule/querytasks/querytask request to the scheduler
    public static final byte SYSEX_NON_REALTIME      = 0x7E; // MIDI Reserved for non-realtime messages
    public static final byte SYSEX_REALTIME          = 0x7F; // MIDI Reserved for realtime messages

    // pin modes
    public static final byte PIN_MODE_INPUT          = 0x00; // defined in wiring.h
    public static final byte PIN_MODE_OUTPUT         = 0x01; // defined in wiring.h
    public static final byte PIN_MODE_ANALOG         = 0x02; // analog pin in analogInput mode
    public static final byte PIN_MODE_PWM            = 0x03; // digital pin in PWM output mode
    public static final byte PIN_MODE_SERVO          = 0x04; // digital pin in Servo output mode
    public static final byte PIN_MODE_SHIFT          = 0x05; // shiftIn/shiftOut mode
    public static final byte PIN_MODE_I2C            = 0x06; // pin included in I2C setup
    public static final byte PIN_MODE_ONEWIRE        = 0x07; // pin configured for 1-wire
    public static final byte PIN_MODE_STEPPER        = 0x08; // pin configured for stepper motor
    public static final byte PIN_MODE_ENCODER        = 0x09; // pin configured for rotary encoders
    public static final byte PIN_MODE_SERIAL         = 0x0A; // pin configured for serial communication
    public static final byte PIN_MODE_PULLUP         = 0x0B; // enable internal pull-up resistor for pin
    public static final byte PIN_MODE_IGNORE         = 0x7F; // pin configured to be ignored by digitalWrite and capabilityResponse
    public static final byte TOTAL_PIN_MODES         = 13;

    public static final byte I2C_WRITE                = 0X00;
    public static final byte I2C_READ                 = 0X08;
    public static final byte I2C_READ_CONTINUOUS      = 0X10;
    public static final byte I2C_STOP_READ_CONTINUOUS = 0X18;

    public static final int MIN_SAMPLING_INTERVAL    = 10;
    public static final int MAX_SAMPLING_INTERVAL    = 100;

    // event types and names
    public static final String FIRMATA_MESSAGE_EVENT_TYPE = "firmataMessage";
    
    public static final String PROTOCOL_MESSAGE = "protocolMessage";
    public static final String PROTOCOL_MAJOR = "major";
    public static final String PROTOCOL_MINOR = "minor";
    
    public static final String FIRMWARE_MESSAGE = "firmwareMessage";
    public static final String FIRMWARE_MAJOR = "major";
    public static final String FIRMWARE_MINOR = "minor";
    public static final String FIRMWARE_NAME = "firmwareName";
    
    public static final String ANALOG_MAPPING_MESSAGE = "analogMapping";
    public static final String ANALOG_MAPPING = "analogMapping";
    
    public static final String ANALOG_MESSAGE_RESPONSE = "analogMessage";
    public static final String DIGITAL_MESSAGE_RESPONSE = "digitalMessage";
    public static final String I2C_MESSAGE = "i2cMessage";
    public static final String I2C_ADDRESS = "i2cAddress";
    public static final String I2C_REGISTER = "i2cRegister";

    public static final String PIN_CAPABILITIES_MESSAGE = "pinCapabilities";
    public static final String PIN_STATE = "pinState";
    public static final String PIN_ID = "pinId";
    public static final String PIN_SUPPORTED_MODES = "supportedModes";
    public static final String PIN_MODE = "pinMode";
    public static final String PIN_VALUE = "pinValue";
    
    public static final String SYSTEM_RESET_MESSAGE = "systemReset";
    
    public static final String STRING_MESSAGE = "stringMessage";
    
    public static final String ERROR_MESSAGE = "error";
    public static final String ERROR_DESCRIPTION = "description";
    public static final String ERROR_CAUSE = "cause";
    
}
