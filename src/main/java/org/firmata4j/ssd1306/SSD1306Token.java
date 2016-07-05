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
 * @see https://cdn-shop.adafruit.com/datasheets/SSD1306.pdf
 */
public interface SSD1306Token {

    public static final byte COMMAND_CONTROL_BYTE = 0x00; // Co = 0, D/C = 0
    public static final byte DATA_CONTROL_BYTE = 0x40; // Co = 0, D/C = 1
    
    // fundamental commands
    public static final byte SET_CONTRAST = (byte) 0x81;
    public static final byte DISPLAY_ON_RESUME = (byte) 0xA4; // resume to RAM content display
    public static final byte DISPLAY_ON_RESET = (byte) 0xA5; // ignore RAM content
    public static final byte NORMAL_DISPLAY = (byte) 0xA6;
    public static final byte INVERSE_DISPLAY = (byte) 0xA7;
    public static final byte LED_OFF = (byte) 0xAE;
    public static final byte LED_ON = (byte) 0xAF;

    // scrolling commands
    public static final byte SET_HORIZONTAL_SCROLL = 0x26;
    public static final byte VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
    public static final byte VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;
    public static final byte SET_VERTICAL_SCROLL_AREA = (byte) 0xA3;
    public static final byte DEACTIVATE_SCROLL = 0x2E;
    public static final byte ACTIVATE_SCROLL = 0x2F;
    
    // addressing comands
    public static final byte SET_MEMORY_ADDR_MODE = 0x20;
    public static final byte SET_COLUMN_ADDR = 0x21;
    public static final byte SET_PAGE_ADDR = 0x22;
    public static final byte PAGE_ADDR_SET_LOW_COLUMN = 0x00;
    public static final byte PAGE_ADDR_SET_HIGH_COLUMN = 0x10;
    public static final byte RAM_PAGE_START_ADDRESS = (byte) 0xB0;
    
    // hardvare configuration commands
    public static final byte SET_START_LINE = 0x40;
    public static final byte SEG_REMAP = (byte) 0xA0;
    public static final byte SET_MULTIPLEX = (byte) 0xA8;
    public static final byte COM_SCAN_INC = (byte) 0xC0;
    public static final byte COM_SCAN_DEC = (byte) 0xC8;
    public static final byte SET_DISPLAY_OFFSET = (byte) 0xD3;
    public static final byte SET_COM_PINS = (byte) 0xDA;

    // timing configuration commands
    public static final byte SET_DISPLAY_CLOCK = (byte) 0xD5;
    public static final byte SET_PRECHARGE_PERIOD = (byte) 0xD9;
    public static final byte SET_VCOMH_DESELECT = (byte) 0xDB;
    public static final byte NOP = (byte) 0xE3;
    
    public static final byte SET_CHARGE_PUMP = (byte) 0x8D;

}
