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

package org.firmata4j.ssd1306;

/**
 * This class contains set of constants that represent tokens of SSD1306 device
 * protocol (commands and control bytes).
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 *
 * see https://cdn-shop.adafruit.com/datasheets/SSD1306.pdf
 */
public interface SSD1306Token {

    byte COMMAND_CONTROL_BYTE = 0x00; // Co = 0, D/C = 0
    byte DATA_CONTROL_BYTE = 0x40; // Co = 0, D/C = 1
    
    // fundamental commands
    byte SET_CONTRAST = (byte) 0x81;
    byte DISPLAY_ON_RESUME = (byte) 0xA4; // resume to RAM content display
    byte DISPLAY_ON_RESET = (byte) 0xA5; // ignore RAM content
    byte NORMAL_DISPLAY = (byte) 0xA6;
    byte INVERSE_DISPLAY = (byte) 0xA7;
    byte LED_OFF = (byte) 0xAE;
    byte LED_ON = (byte) 0xAF;

    // scrolling commands
    byte SET_HORIZONTAL_SCROLL = 0x26;
    byte VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
    byte VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;
    byte SET_VERTICAL_SCROLL_AREA = (byte) 0xA3;
    byte DEACTIVATE_SCROLL = 0x2E;
    byte ACTIVATE_SCROLL = 0x2F;
    
    // addressing comands
    byte SET_MEMORY_ADDR_MODE = 0x20;
    byte SET_COLUMN_ADDR = 0x21;
    byte SET_PAGE_ADDR = 0x22;
    byte PAGE_ADDR_SET_LOW_COLUMN = 0x00;
    byte PAGE_ADDR_SET_HIGH_COLUMN = 0x10;
    byte RAM_PAGE_START_ADDRESS = (byte) 0xB0;
    
    // hardvare configuration commands
    byte SET_START_LINE = 0x40;
    byte SEG_REMAP = (byte) 0xA0;
    byte SET_MULTIPLEX = (byte) 0xA8;
    byte COM_SCAN_INC = (byte) 0xC0;
    byte COM_SCAN_DEC = (byte) 0xC8;
    byte SET_DISPLAY_OFFSET = (byte) 0xD3;
    byte SET_COM_PINS = (byte) 0xDA;

    // timing configuration commands
    byte SET_DISPLAY_CLOCK = (byte) 0xD5;
    byte SET_PRECHARGE_PERIOD = (byte) 0xD9;
    byte SET_VCOMH_DESELECT = (byte) 0xDB;
    byte NOP = (byte) 0xE3;
    
    byte SET_CHARGE_PUMP = (byte) 0x8D;

}
