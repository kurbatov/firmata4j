/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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
 * Contains constants for protocol message types and payload parts.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public interface FirmataEventType {
    
    String PROTOCOL_MESSAGE = "protocolMessage";
    String PROTOCOL_MAJOR = "major";
    String PROTOCOL_MINOR = "minor";
    
    String FIRMWARE_MESSAGE = "firmwareMessage";
    String FIRMWARE_MAJOR = "major";
    String FIRMWARE_MINOR = "minor";
    String FIRMWARE_NAME = "firmwareName";
    
    String ANALOG_MAPPING_MESSAGE = "analogMapping";
    String ANALOG_MAPPING = "analogMapping";
    
    String ANALOG_MESSAGE_RESPONSE = "analogMessage";
    String DIGITAL_MESSAGE_RESPONSE = "digitalMessage";
    String I2C_MESSAGE = "i2cMessage";
    String I2C_ADDRESS = "i2cAddress";
    String I2C_REGISTER = "i2cRegister";

    String PIN_CAPABILITIES_MESSAGE = "pinCapabilities";
    String PIN_STATE = "pinState";
    String PIN_ID = "pinId";
    String PIN_SUPPORTED_MODES = "supportedModes";
    String PIN_MODE = "pinMode";
    String PIN_VALUE = "pinValue";
    
    String SYSTEM_RESET_MESSAGE = "systemReset";
    
    String STRING_MESSAGE = "stringMessage";
    
    String SYSEX_CUSTOM_MESSAGE = "sysexCustomMessage";
    
    String ERROR_MESSAGE = "error";
    String ERROR_DESCRIPTION = "description";
    String ERROR_CAUSE = "cause";
    
}
