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

import static org.firmata4j.ssd1306.SSD1306Token.*;

/**
 * This class contains methods that help to build command messages for SSD1306
 * device.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 *
 * see https://cdn-shop.adafruit.com/datasheets/SSD1306.pdf
 */
public class SSD1306MessageFactory {

    // fundamental commands
    public static byte[] setContrast(byte contrast) {
        return new byte[]{COMMAND_CONTROL_BYTE, SET_CONTRAST, contrast};
    }

    public static byte[] setDisplayInverse(boolean inverse) {
        byte command;
        if (inverse) {
            command = INVERSE_DISPLAY;
        } else {
            command = NORMAL_DISPLAY;
        }
        return new byte[]{COMMAND_CONTROL_BYTE, command};
    }

    public static final byte[] DISPLAY_RESUME = {COMMAND_CONTROL_BYTE, DISPLAY_ON_RESUME}; // resume to RAM content display
    public static final byte[] DISPLAY_RESET = {COMMAND_CONTROL_BYTE, DISPLAY_ON_RESET}; // ignore RAM content
    public static final byte[] TURN_OFF = {COMMAND_CONTROL_BYTE, LED_OFF};
    public static final byte[] TURN_ON = {COMMAND_CONTROL_BYTE, LED_ON};

    // scrolling commands
    public static byte[] setHorizontalScroll(ScrollDirection direction, byte startAddress, byte endAddress, byte speed) {
        if (startAddress < 0 || startAddress > 7 || endAddress < 0 || endAddress > 7) {
            throw new IllegalArgumentException("Start and end address must be between 0 and 7.");
        }
        if (endAddress < startAddress) {
            throw new IllegalArgumentException("End address must be larger or equal to start address.");
        }
        if (speed < 0 || speed > 7) {
            throw new IllegalArgumentException("Speed must be between 0 and 7.");
        }
        return new byte[]{
            COMMAND_CONTROL_BYTE, (byte) (SET_HORIZONTAL_SCROLL | direction.ordinal()),
            COMMAND_CONTROL_BYTE, 0, // dummy byte
            COMMAND_CONTROL_BYTE, startAddress,
            COMMAND_CONTROL_BYTE, speed,
            COMMAND_CONTROL_BYTE, endAddress,
            COMMAND_CONTROL_BYTE, 0, //dummy byte
            COMMAND_CONTROL_BYTE, (byte) 0xFF //dummy byte
        };
    }

    public static byte[] setVerticalAndHorizontalScroll(
            ScrollDirection direction,
            byte startAddress,
            byte endAddress,
            byte speed,
            byte verticalOffset
    ) {
        if (startAddress < 0 || startAddress > 7 || endAddress < 0 || endAddress > 7) {
            throw new IllegalArgumentException("Start and end address must be between 0 and 7.");
        }
        if (endAddress < startAddress) {
            throw new IllegalArgumentException("End address must be larger or equal to start address.");
        }
        if (speed < 0 || speed > 7) {
            throw new IllegalArgumentException("Speed must be between 0 and 7.");
        }
        if (verticalOffset < 0 || verticalOffset > 63) {
            throw new IllegalArgumentException("Vertical offset must be between 0 and 63.");
        }
        byte scrollType;
        if (direction == ScrollDirection.RIGHT) {
            scrollType = VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL;
        } else {
            scrollType = VERTICAL_AND_LEFT_HORIZONTAL_SCROLL;
        }
        return new byte[]{
            COMMAND_CONTROL_BYTE, scrollType,
            COMMAND_CONTROL_BYTE, 0, // dummy byte
            COMMAND_CONTROL_BYTE, startAddress,
            COMMAND_CONTROL_BYTE, speed,
            COMMAND_CONTROL_BYTE, endAddress,
            COMMAND_CONTROL_BYTE, verticalOffset
        };
    }

    /**
     * Sets vertical scroll area.
     * <p>
     * For 64-row display:
     * <ul>
     *   <li>fixedRows = 0, scrolledRows : whole area scrolls</li>
     *   <li>fixedRows = 0, scrolledRows &lt; 64 : top area scrolls</li>
     *   <li>fixedRows + scrolledRows &lt; 64 : central area scrolls</li>
     *   <li>fixedRows + scrolledRows = 64 : bottom area scrolls</li>
     * </ul>
     * </p>
     * <p>
     * Note that:
     * <ul>
     *   <li>fixedRows + scrolledRows &lt;= {@link #setMultiplexRatio}</li>
     *   <li>scrolledRows &lt;= {@link #setMultiplexRatio}</li>
     *   <li>verticalOffset from {@link #setVerticalAndHorizontalScroll} &lt;= scrolledRows</li>
     *   <li>{@link #setDisplayStartLine} &lt;= scrolledRows</li>
     * </ul>
     * </p>
     * 
     * @param fixedRows No. of rows in top fixed area. It is referenced to the
     * top of the GDDRAM.
     * @param scrolledRows No. of rows in scroll area. This is the number of
     * rows to be used for vertical scrolling. The scroll area starts in the
     * first row below the top fixed area.
     * @return command sequence
     */
    public static byte[] setVerticalScrollArea(byte fixedRows, byte scrolledRows) {
        if (fixedRows < 0 || fixedRows > 63) {
            throw new IllegalArgumentException("Fixed rows must be between 0 and 63.");
        }
        if (scrolledRows < 0 || scrolledRows > 127) {
            throw new IllegalArgumentException("Scrolled rows must be between 0 and 127.");
        }
        return new byte[]{
            COMMAND_CONTROL_BYTE, SET_VERTICAL_SCROLL_AREA,
            COMMAND_CONTROL_BYTE, fixedRows,
            COMMAND_CONTROL_BYTE, scrolledRows
        };
    }
    
    public static final byte[] DISABLE_SCROLLING = {COMMAND_CONTROL_BYTE, DEACTIVATE_SCROLL};
    public static final byte[] ENABLE_SCROLLING = {COMMAND_CONTROL_BYTE, ACTIVATE_SCROLL};

    // addressing comands
    public static byte[] setMemoryAddressingMode(MemoryAddressingMode mode) {
        return new byte[]{COMMAND_CONTROL_BYTE, SET_MEMORY_ADDR_MODE, COMMAND_CONTROL_BYTE, (byte) mode.ordinal()};
    }

    /**
     * Setup column start and end address.<br>
     * This command is only for horizontal or vertical addressing mode.
     *
     * @param startAddress Column start address, range : 0-127
     * @param endAddress Column end address, range : 0-127
     * @return command sequence
     */
    public static byte[] setColumnAddress(byte startAddress, byte endAddress) {
        if (startAddress < 0 || endAddress < 0) {
            throw new IllegalArgumentException("Start and end address must be positive.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_COLUMN_ADDR, COMMAND_CONTROL_BYTE, startAddress, COMMAND_CONTROL_BYTE, endAddress};
    }

    /**
     * Setup page start and end address.<br>
     * This command is only for horizontal or vertical addressing mode.
     *
     * @param startAddress Page start Address, range : 0-7
     * @param endAddress Page end Address, range : 0-7
     * @return command sequence
     */
    public static byte[] setPageAddress(byte startAddress, byte endAddress) {
        if (startAddress < 0 || startAddress > 7 || endAddress < 0 || endAddress > 7) {
            throw new IllegalArgumentException("Start and end address must be between 0 and 7.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_PAGE_ADDR, COMMAND_CONTROL_BYTE, startAddress, COMMAND_CONTROL_BYTE, endAddress};
    }

    /**
     * Set the lower nibble of the column start address register for Page
     * Addressing Mode. The initial display line register is reset to 0 after
     * RESET.<br>
     * This command is only for page addressing mode.
     *
     * @param address lower nibble of the column start address
     * @return command sequence
     */
    public static byte[] setLowerColumnStartAddress(byte address) {
        if (address < 0 || address > 15) {
            throw new IllegalArgumentException("Start address must be between 0 and 15.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, (byte) (PAGE_ADDR_SET_LOW_COLUMN | address)};
    }

    /**
     * Set the higher nibble of the column start address register for Page
     * Addressing Mode. The initial display line register is reset to 0 after
     * RESET.<br>
     * This command is only for page addressing mode.
     *
     * @param address higher nibble of the column start address
     * @return command sequence
     */
    public static byte[] setHigherColumnStartAddress(byte address) {
        if (address < 0 || address > 15) {
            throw new IllegalArgumentException("Start address must be between 0 and 15.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, (byte) (PAGE_ADDR_SET_HIGH_COLUMN | address)};
    }

    /**
     * Set GDDRAM Page Start Address (PAGE0~PAGE7) for Page Addressing Mode.<br>
     * This command is only for page addressing mode.
     *
     * @param address Page start Address, range : 0-7
     * @return command sequence
     */
    public static byte[] setRAMPageStartAddress(byte address) {
        if (address < 0 || address > 7) {
            throw new IllegalArgumentException("Start address must be between 0 and 7.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, (byte) (RAM_PAGE_START_ADDRESS | address)};
    }

    // hardvare configuration commands
    public static byte[] setDisplayStartLine(int line) {
        if (line < 0 || line > 63) {
            throw new IllegalArgumentException("Line register must be between 0 and 63.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, (byte) (SET_START_LINE | line)};
    }

    public static byte[] setHorizontalFlip(boolean flip) {
        byte command;
        if (flip) {
            command = SEG_REMAP | 0x1;
        } else {
            command = SEG_REMAP;
        }
        return new byte[]{COMMAND_CONTROL_BYTE, command};
    }

    public static byte[] setMultiplexRatio(int multiplexRatio) {
        if (multiplexRatio < 16 || multiplexRatio > 64) {
            throw new IllegalArgumentException("Multiplex ratio must be between 16 an 64.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_MULTIPLEX, COMMAND_CONTROL_BYTE, (byte) (multiplexRatio - 1)};
    }

    public static byte[] setVerticalFlip(boolean flip) {
        byte[] command = new byte[2];
        command[0] = COMMAND_CONTROL_BYTE;
        if (flip) {
            command[1] = COM_SCAN_DEC;
        } else {
            command[1] = COM_SCAN_INC;
        }
        return command;
    }

    public static byte[] setDisplayOffset(int offset) {
        if (offset < 0 || offset > 63) {
            throw new IllegalArgumentException("Display offset must be between 0 and 63.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_DISPLAY_OFFSET, COMMAND_CONTROL_BYTE, (byte) offset};
    }

    public static byte[] setCOMPinsConfig(boolean sequential, boolean leftRightRemap) {
        byte param = 0x02;
        if (!sequential) {
            param |= 0x10;
        }
        if (leftRightRemap) {
            param |= 0x20;
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_COM_PINS, COMMAND_CONTROL_BYTE, param};
    }

    // timing configuration commands
    public static byte[] setDisplayClock(byte divideRatio, byte oscillatorFrequency) {
        if (divideRatio < 0 || oscillatorFrequency < 0) {
            throw new IllegalArgumentException("Divide ratio and oscillator frequency must be non-negative.");
        }
        byte param = (byte) ((oscillatorFrequency << 4) & divideRatio);
        return new byte[]{COMMAND_CONTROL_BYTE, SET_DISPLAY_CLOCK, COMMAND_CONTROL_BYTE, param};
    }

    public static byte[] setPrechargePeriod(byte phase1Period, byte phase2Period) {
        if (phase1Period <= 0 || phase2Period <= 0) {
            throw new IllegalArgumentException("Phase 1 and 2 periods must be positive.");
        }
        byte param = (byte) ((phase2Period << 4) & phase1Period);
        return new byte[]{COMMAND_CONTROL_BYTE, SET_PRECHARGE_PERIOD, COMMAND_CONTROL_BYTE, param};
    }

    public static byte[] setVcomhDeselectLevel(byte level) {
        if (level < 0 || level > 7) {
            throw new IllegalArgumentException("Level value must be between 0 and 7.");
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_VCOMH_DESELECT, COMMAND_CONTROL_BYTE, (byte) (level << 4)};
    }

    public static byte[] setChargePump(boolean enabled) {
        byte param = 0x10;
        if (enabled) {
            param |= 0x04;
        }
        return new byte[]{COMMAND_CONTROL_BYTE, SET_CHARGE_PUMP, COMMAND_CONTROL_BYTE, param};
    }

    public static enum ScrollDirection {

        RIGHT,
        LEFT
    }

    public static enum MemoryAddressingMode {

        HORIZONTAL,
        VERTICAL,
        PAGE
    }

}
