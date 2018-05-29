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

import java.io.IOException;
import java.util.Arrays;
import org.firmata4j.I2CDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.firmata4j.ssd1306.SSD1306Token.DATA_CONTROL_BYTE;
import static org.firmata4j.ssd1306.SSD1306MessageFactory.*;

/**
 * This class provides facilities to control a monochrome OLED display on 
 * SSD1306 driver.<br>
 * <p>
 * Example:
 * <blockquote><pre>
 *   IODevice device = new FirmataDevice(port); // connecting to firmata device
 *   device.ensureInitializationIsDone(); // waiting for the device is ready
 *   I2CDevice i2cDevice = device.getI2CDevice((byte) 0x3C); // or 0x3D - standard address for SSD1306
 *   SSD1306 display = new SSD1306(i2cDevice, SSD1306.Size.SSD1306_128_64); // constructing a display on top of i2c device reference
 *   display.init(); // initializing display
 *   display.getCanvas().drawString(3, 3, "firmata4j"); // drawing a string on display's canvas
 *   // drawing anything else
 *   display.display(); // displaying current state of display's canvas
 *   // doing anything else
 *   display.turnOff(); // switching off the display
 *   device.stop(); // stopping IODevice
 * </pre></blockquote>
 * </p>
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 *
 * see https://www.adafruit.com/category/63_98
 */
public class SSD1306 {

    private final I2CDevice device;

    private final Size size;

    private final MonochromeCanvas canvas;

    private final VCC vccState;

    private static final Logger LOGGER = LoggerFactory.getLogger(SSD1306.class);

    public SSD1306(I2CDevice device, Size size) {
        this(device, size, VCC.INTERNAL);
    }
    
    public SSD1306(I2CDevice device, Size size, VCC vcc) {
        this.device = device;
        this.size = size;
        canvas = new MonochromeCanvas(size.width, size.height);
        vccState = vcc;
    }

    /**
     * Prepares the device to operation.
     */
    public void init() {
        turnOff();
        // initialization
        command(setDisplayClock((byte) 0, (byte) 8));
        command(setMultiplexRatio(size.height));
        command(setDisplayOffset(0));           // no offset
        command(setDisplayStartLine(0));        // line #0
        command(setMemoryAddressingMode(MemoryAddressingMode.HORIZONTAL));
        command(setHorizontalFlip(false));
        command(setVerticalFlip(false));
        if (vccState == VCC.EXTERNAL) {
            command(setChargePump(false));
        } else {
            command(setChargePump(true));
        }
        int contrast = 0;
        boolean sequentialPins = true;
        boolean leftRightPinsRemap = false;
        if (size == Size.SSD1306_128_32) {
            contrast = 0x8F;
        } else if (size == Size.SSD1306_128_64) {
            if (vccState == VCC.EXTERNAL) {
                contrast = 0x9F;
            } else {
                contrast = 0xCF;
            }
            sequentialPins = false;
        } else if (size == Size.SSD1306_96_16) {
            if (vccState == VCC.EXTERNAL) {
                contrast = 0x10;
            } else {
                contrast = 0xAF;
            }
        }
        command(setCOMPinsConfig(sequentialPins, leftRightPinsRemap));
        command(setContrast((byte) contrast));
        if (vccState == VCC.EXTERNAL) {
            command(setPrechargePeriod((byte) 2, (byte) 2));
        } else {
            command(setPrechargePeriod((byte) 1, (byte) 15));
        }
        command(setVcomhDeselectLevel((byte) 3));
        command(DISPLAY_RESUME);
        command(setDisplayInverse(false));
        stopScrolling();
        display(); // synchronize canvas with display's internal buffer
        turnOn();
    }

    public void turnOn() {
        command(TURN_ON);
    }

    public void turnOff() {
        command(TURN_OFF);
    }

    public void scrollRight() {
        command(setHorizontalScroll(ScrollDirection.RIGHT, (byte) 0, (byte) 7, (byte) 0));
        command(ENABLE_SCROLLING);
    }
    
    public void scrollLeft() {
        command(setHorizontalScroll(ScrollDirection.LEFT, (byte) 0, (byte) 7, (byte) 0));
        command(ENABLE_SCROLLING);
    }
    
    public void stopScrolling() {
        command(DISABLE_SCROLLING);
    }
    
    public void invertDisplay(boolean inverted) {
        command(setDisplayInverse(inverted));
    }
    
    public void clear() {
        getCanvas().clear();
        display();
    }

    /**
     * Dims the display
     *
     * @param dim true - display is dimmed, false - display is normal
     */
    public void dim(boolean dim) {
        byte contrast;

        if (dim) {
            contrast = 0; // Dimmed display
        } else {
            if (vccState == VCC.EXTERNAL) {
                contrast = (byte) 0x9F;
            } else {
                contrast = (byte) 0xCF;
            }
        }
        // the range of contrast to too small to be really useful
        // it is useful to dim the display
        command(setContrast(contrast));
    }

    public void display() {
        command(setColumnAddress((byte) 0, (byte) (size.width - 1)));
        byte pageEndAddr = 0;
        switch (size) {
            case SSD1306_128_64:
                pageEndAddr = 7; // Page end address
                break;
            case SSD1306_128_32:
                pageEndAddr = 3; // Page end address
                break;
            case SSD1306_96_16:
                pageEndAddr = 1; // Page end address
                break;
        }
        command(setPageAddress((byte) 0, pageEndAddr));
        //TODO increase 2C bitrate if possible
        byte[] buffer = canvas.getBuffer();
        try {
            for (int i = 0; i < buffer.length / 16; i++) {
                byte[] row = new byte[17];
                row[0] = DATA_CONTROL_BYTE;
                System.arraycopy(buffer, i * 16, row, 1, 16);
                device.tell(row);
            }
        } catch (IOException e) {
            LOGGER.error("Displaying attempt failed", e);
        }
    }

    public Size getSize() {
        return size;
    }

    public MonochromeCanvas getCanvas() {
        return canvas;
    }

    private void command(byte... command) {
        try {
            for (int i = 0; i < command.length; i += 2) {
                device.tell(Arrays.copyOfRange(command, i, i + 2));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static enum Size {

        SSD1306_128_64(128, 64),
        SSD1306_128_32(128, 32),
        SSD1306_96_16(96, 16);

        public final int width, height;

        private Size(int w, int h) {
            width = w;
            height = h;
        }

    }
    
    public static enum VCC {
        EXTERNAL,
        INTERNAL
    }

}
