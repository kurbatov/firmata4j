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
    byte FIRMATA_MAJOR_VERSION  = 2; // for non-compatible changes
    byte FIRMATA_MINOR_VERSION  = 3; // for backwards compatible changes
    byte FIRMATA_BUGFIX_VERSION = 1; // for bugfix releases
    
    // message command bytes (128-255/0x80-0xFF)
    
    /**
     * send data for a digital pin
     */
    byte DIGITAL_MESSAGE         = (byte) 0x90;
    
    /**
     * send data for an analog pin (or PWM)
     */
    byte ANALOG_MESSAGE          = (byte) 0xE0;
    
    /**
     * enable analog input by pin #
     */
    byte REPORT_ANALOG           = (byte) 0xC0;
    
    /**
     * enable digital input by port pair
     */
    byte REPORT_DIGITAL          = (byte) 0xD0;
    
    /**
     * set a pin to INPUT/OUTPUT/PWM/etc
     */
    byte SET_PIN_MODE            = (byte) 0xF4;
    
    /**
     * set value of an individual digital pin
     */
    byte SET_DIGITAL_PIN_VALUE   = (byte) 0xF5;
    
    /**
     * report protocol version
     */
    byte REPORT_VERSION          = (byte) 0xF9;
    
    /**
     * reset from MIDI
     */
    byte SYSTEM_RESET            = (byte) 0xFF;
    
    /**
     * start a MIDI Sysex message
     */
    byte START_SYSEX             = (byte) 0xF0;
    
    /**
     * end a MIDI Sysex message
     */
    byte END_SYSEX               = (byte) 0xF7;

    // extended command set using sysex (0-127/0x00-0x7F)
    /* 0x00-0x0F reserved for user-defined commands */
    byte RESERVED_COMMAND        = 0x00; // 2nd SysEx data byte is a chip-specific command (AVR, PIC, TI, etc).
    byte SERIAL_MESSAGE          = 0x60; // communicate with serial devices, including other boards
    byte ENCODER_DATA            = 0x61; // reply with encoders current positions
    byte SERVO_CONFIG            = 0x70; // set max angle, minPulse, maxPulse, freq
    byte STRING_DATA             = 0x71; // a string message with 14-bits per byte
    byte STEPPER_DATA            = 0x72; // control a stepper motor
    byte ONEWIRE_DATA            = 0x73; // send an OneWire read/write/reset/select/skip/search request
    byte SHIFT_DATA              = 0x75; // a bitstream to/from a shift register
    byte I2C_REQUEST             = 0x76; // send an I2C read/write request
    byte I2C_REPLY               = 0x77; // a reply to an I2C read request
    byte I2C_CONFIG              = 0x78; // config I2C settings such as delay times and power pins
    byte EXTENDED_ANALOG         = 0x6F; // analog write (PWM, Servo, etc) to any pin
    byte PIN_STATE_QUERY         = 0x6D; // ask for a pin's current mode and value
    byte PIN_STATE_RESPONSE      = 0x6E; // reply with pin's current mode and value
    byte CAPABILITY_QUERY        = 0x6B; // ask for supported modes and resolution of all pins
    byte CAPABILITY_RESPONSE     = 0x6C; // reply with supported modes and resolution
    byte ANALOG_MAPPING_QUERY    = 0x69; // ask for mapping of analog to pin numbers
    byte ANALOG_MAPPING_RESPONSE = 0x6A; // reply with mapping info
    byte REPORT_FIRMWARE         = 0x79; // report name and version of the firmware
    byte SAMPLING_INTERVAL       = 0x7A; // set the poll rate of the main loop
    byte SCHEDULER_DATA          = 0x7B; // send a createtask/deletetask/addtotask/schedule/querytasks/querytask request to the scheduler
    byte SYSEX_NON_REALTIME      = 0x7E; // MIDI Reserved for non-realtime messages
    byte SYSEX_REALTIME          = 0x7F; // MIDI Reserved for realtime messages

    // pin modes
    byte PIN_MODE_INPUT          = 0x00; // defined in wiring.h
    byte PIN_MODE_OUTPUT         = 0x01; // defined in wiring.h
    byte PIN_MODE_ANALOG         = 0x02; // analog pin in analogInput mode
    byte PIN_MODE_PWM            = 0x03; // digital pin in PWM output mode
    byte PIN_MODE_SERVO          = 0x04; // digital pin in Servo output mode
    byte PIN_MODE_SHIFT          = 0x05; // shiftIn/shiftOut mode
    byte PIN_MODE_I2C            = 0x06; // pin included in I2C setup
    byte PIN_MODE_ONEWIRE        = 0x07; // pin configured for 1-wire
    byte PIN_MODE_STEPPER        = 0x08; // pin configured for stepper motor
    byte PIN_MODE_ENCODER        = 0x09; // pin configured for rotary encoders
    byte PIN_MODE_SERIAL         = 0x0A; // pin configured for serial communication
    byte PIN_MODE_PULLUP         = 0x0B; // enable internal pull-up resistor for pin
    byte PIN_MODE_IGNORE         = 0x7F; // pin configured to be ignored by digitalWrite and capabilityResponse
    byte TOTAL_PIN_MODES         = 13;

    byte I2C_WRITE                = 0X00;
    byte I2C_READ                 = 0X08;
    byte I2C_READ_CONTINUOUS      = 0X10;
    byte I2C_STOP_READ_CONTINUOUS = 0X18;

    int MIN_SAMPLING_INTERVAL    = 10;
    int MAX_SAMPLING_INTERVAL    = 100;
    
}
