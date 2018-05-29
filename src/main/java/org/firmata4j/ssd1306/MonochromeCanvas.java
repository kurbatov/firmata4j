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

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import static java.lang.Math.*;
import java.util.Arrays;

/**
 * This is monochrome 1-bit-per-pixel canvas that allows to draw basic graphical
 * shapes, characters, strings and bitmaps.<br>
 * It was inspired by Adafruit GFX library.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 *
 * see https://github.com/adafruit/Adafruit-GFX-Library
 */
public class MonochromeCanvas {

    private final int width;

    private final int height;

    private final byte[] buffer;

    private int rotation = 0;

    private int textsize = 1;

    private Color color = Color.BRIGHT;

    private Color bgcolor = Color.DARK;

    private int cursorX = 0;

    private int cursorY = 0;

    private boolean wrap = true;

    public MonochromeCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new byte[width * height / 8];
    }

    public void setPixel(int x, int y, Color color) {
        if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
            return;
        }
        // check rotation, move pixel around if necessary
        int tmp;
        switch (getRotation()) {
            case 1:
                tmp = x;
                x = y;
                y = tmp;
                x = width - x - 1;
                break;
            case 2:
                x = width - x - 1;
                y = height - y - 1;
                break;
            case 3:
                tmp = x;
                x = y;
                y = tmp;
                y = height - y - 1;
                break;
        }
        // x is which column
        switch (color) {
            case BRIGHT:
                buffer[x + (y / 8) * width] |= (1 << (y & 7));
                break;
            case DARK:
                buffer[x + (y / 8) * width] &= ~(1 << (y & 7));
                break;
            case INVERSE:
                buffer[x + (y / 8) * width] ^= (1 << (y & 7));
                break;
        }
    }

    public void drawLine(int fromX, int fromY, int toX, int toY, Color color) {
        boolean steep = abs(toY - fromY) > abs(toX - fromX);
        if (steep) {
            int tmp = fromX;
            fromX = fromY;
            fromY = tmp;
            tmp = toX;
            toX = toY;
            toY = tmp;
        }
        if (fromX > toX) {
            int tmp = fromX;
            fromX = toX;
            toX = tmp;
            tmp = fromY;
            fromY = toY;
            toY = tmp;
        }
        int dx, dy;
        dx = toX - fromX;
        dy = abs(toY - fromY);
        int err = dx / 2;
        int ystep;
        if (fromY < toY) {
            ystep = 1;
        } else {
            ystep = -1;
        }
        for (int x = fromX, y = fromY; x <= toX; x++) {
            if (steep) {
                setPixel(y, x, color);
            } else {
                setPixel(x, y, color);
            }
            err -= dy;
            if (err < 0) {
                y += ystep;
                err += dx;
            }
        }
    }
    
    public void drawLine(int fromX, int fromY, int toX, int toY) {
        drawLine(fromX, fromY, toX, toY, color);
    }

    public void drawVerticalLine(int x, int y, int h, Color color) {
        drawLine(x, y, x, y + h - 1, color);
    }

    public void drawHorizontalLine(int x, int y, int w, Color color) {
        drawLine(x, y, x + w - 1, y, color);
    }

    // Draw a rectangle
    public void drawRect(int x, int y, int w, int h, Color color) {
        drawHorizontalLine(x, y, w, color);
        drawHorizontalLine(x, y + h - 1, w, color);
        drawVerticalLine(x, y, h, color);
        drawVerticalLine(x + w - 1, y, h, color);
    }
    
    public void drawRect(int x, int y, int w, int h) {
        drawRect(x, y, w, h, color);
    }

    public void fillRect(int x, int y, int w, int h, Color color) {
        for (int i = x; i < x + w; i++) {
            drawVerticalLine(i, y, h, color);
        }
    }
    
    public void fillRect(int x, int y, int w, int h) {
        fillRect(x, y, w, h, color);
    }

    public void fillScreen(Color color) {
        fillRect(0, 0, width, height, color);
    }

    public void drawCircle(int centerX, int centerY, int r, Color color) {
        int f = 1 - r;
        int ddF_x = 1;
        int ddF_y = -2 * r;
        int x = 0;
        int y = r;
        setPixel(centerX, centerY + r, color);
        setPixel(centerX, centerY - r, color);
        setPixel(centerX + r, centerY, color);
        setPixel(centerX - r, centerY, color);
        while (x < y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;
            setPixel(centerX + x, centerY + y, color);
            setPixel(centerX - x, centerY + y, color);
            setPixel(centerX + x, centerY - y, color);
            setPixel(centerX - x, centerY - y, color);
            setPixel(centerX + y, centerY + x, color);
            setPixel(centerX - y, centerY + x, color);
            setPixel(centerX + y, centerY - x, color);
            setPixel(centerX - y, centerY - x, color);
        }
    }
    
    public void drawCircle(int centerX, int centerY, int r) {
        drawCircle(centerX, centerY, r, color);
    }

    public void drawCircleHelper(int centerX, int centerY, int r, int cornername, Color color) {
        int f = 1 - r;
        int ddF_x = 1;
        int ddF_y = -2 * r;
        int x = 0;
        int y = r;
        while (x < y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;
            if ((cornername & 0x4) != 0) {
                setPixel(centerX + x, centerY + y, color);
                setPixel(centerX + y, centerY + x, color);
            }
            if ((cornername & 0x2) != 0) {
                setPixel(centerX + x, centerY - y, color);
                setPixel(centerX + y, centerY - x, color);
            }
            if ((cornername & 0x8) != 0) {
                setPixel(centerX - y, centerY + x, color);
                setPixel(centerX - x, centerY + y, color);
            }
            if ((cornername & 0x1) != 0) {
                setPixel(centerX - y, centerY - x, color);
                setPixel(centerX - x, centerY - y, color);
            }
        }
    }

    public void fillCircle(int x, int y, int r, Color color) {
        drawVerticalLine(x, y - r, 2 * r + 1, color);
        fillCircleHelper(x, y, r, 3, 0, color);
    }

    public void fillCircleHelper(int centerX, int centerY, int r, int cornername, int delta, Color color) {
        int f = 1 - r;
        int ddF_x = 1;
        int ddF_y = -2 * r;
        int x = 0;
        int y = r;
        while (x < y) {
            if (f >= 0) {
                y--;
                ddF_y += 2;
                f += ddF_y;
            }
            x++;
            ddF_x += 2;
            f += ddF_x;

            if ((cornername & 0x1) != 0) {
                drawVerticalLine(centerX + x, centerY - y, 2 * y + 1 + delta, color);
                drawVerticalLine(centerX + y, centerY - x, 2 * x + 1 + delta, color);
            }
            if ((cornername & 0x2) != 0) {
                drawVerticalLine(centerX - x, centerY - y, 2 * y + 1 + delta, color);
                drawVerticalLine(centerX - y, centerY - x, 2 * x + 1 + delta, color);
            }
        }
    }

    public void drawRoundRect(int x, int y, int w, int h, int r, Color color) {
        drawHorizontalLine(x + r, y, w - 2 * r, color); // Top
        drawHorizontalLine(x + r, y + h - 1, w - 2 * r, color); // Bottom
        drawVerticalLine(x, y + r, h - 2 * r, color); // Left
        drawVerticalLine(x + w - 1, y + r, h - 2 * r, color); // Right
        // corners
        drawCircleHelper(x + r, y + r, r, 1, color);
        drawCircleHelper(x + w - r - 1, y + r, r, 2, color);
        drawCircleHelper(x + w - r - 1, y + h - r - 1, r, 4, color);
        drawCircleHelper(x + r, y + h - r - 1, r, 8, color);
    }

    public void fillRoundRect(int x, int y, int w, int h, int r, Color color) {
        fillRect(x + r, y, w - 2 * r, h, color);
        // corners
        fillCircleHelper(x + w - r - 1, y + r, r, 1, h - 2 * r - 1, color);
        fillCircleHelper(x + r, y + r, r, 2, h - 2 * r - 1, color);
    }

    public void drawTriangle(int x0, int y0, int x1, int y1, int x2, int y2, Color color) {
        drawLine(x0, y0, x1, y1, color);
        drawLine(x1, y1, x2, y2, color);
        drawLine(x2, y2, x0, y0, color);
    }

    public void fillTriangle(int x0, int y0, int x1, int y1, int x2, int y2, Color color) {
        int a, b, y;
        // Sort coordinates by Y order (y2 >= y1 >= y0)
        if (y0 > y1) {
            int tmp = y0;
            y0 = y1;
            y1 = tmp;
            tmp = x0;
            x0 = x1;
            x1 = tmp;
        }
        if (y1 > y2) {
            int tmp = y2;
            y2 = y1;
            y1 = tmp;
            tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if (y0 > y1) {
            int tmp = y0;
            y0 = y1;
            y1 = tmp;
            tmp = x0;
            x0 = x1;
            x1 = tmp;
        }
        if (y0 == y2) { // Handle awkward all-on-same-line case as its own thing
            a = b = x0;
            if (x1 < a) {
                a = x1;
            } else if (x1 > b) {
                b = x1;
            }
            if (x2 < a) {
                a = x2;
            } else if (x2 > b) {
                b = x2;
            }
            drawHorizontalLine(a, y0, b - a + 1, color);
            return;
        }
        int dx01 = x1 - x0,
                dy01 = y1 - y0,
                dx02 = x2 - x0,
                dy02 = y2 - y0,
                dx12 = x2 - x1,
                dy12 = y2 - y1;
        int sa = 0,
                sb = 0;

        // For upper part of triangle, find scanline crossings for segments
        // 0-1 and 0-2.  If y1=y2 (flat-bottomed triangle), the scanline y1
        // is included here (and second loop will be skipped, avoiding a /0
        // error there), otherwise scanline y1 is skipped here and handled
        // in the second loop...which also avoids a /0 error here if y0=y1
        // (flat-topped triangle).
        int last;
        if (y1 == y2) {
            last = y1;   // Include y1 scanline
        } else {
            last = y1 - 1; // Skip it
        }
        for (y = y0; y <= last; y++) {
            a = x0 + sa / dy01;
            b = x0 + sb / dy02;
            sa += dx01;
            sb += dx02;
            if (a > b) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            drawHorizontalLine(a, y, b - a + 1, color);
        }
        // For lower part of triangle, find scanline crossings for segments
        // 0-2 and 1-2.  This loop is skipped if y1=y2.
        sa = dx12 * (y - y1);
        sb = dx02 * (y - y0);
        for (; y <= y2; y++) {
            a = x1 + sa / dy12;
            b = x0 + sb / dy02;
            sa += dx12;
            sb += dx02;
            if (a > b) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            drawHorizontalLine(a, y, b - a + 1, color);
        }
    }

    public void drawChar(int x, int y, char c, Color color, Color bg, int size) {
        if ((x >= width) || // Clip right
                (y >= height) || // Clip bottom
                ((x + 6 * size - 1) < 0) || // Clip left
                ((y + 8 * size - 1) < 0)) // Clip top
        {
            return;
        }
        for (int i = 0; i < 6; i++) {
            char line;
            if (i < 5) {
                line = FONT[(c * 5) + i];
            } else {
                line = 0x0;
            }
            for (int j = 0; j < 8; j++, line >>= 1) {
                if ((line & 0x1) != 0) {
                    if (size == 1) {
                        setPixel(x + i, y + j, color);
                    } else {
                        fillRect(x + (i * size), y + (j * size), size, size, color);
                    }
                } else if (bg != color) {
                    if (size == 1) {
                        setPixel(x + i, y + j, bg);
                    } else {
                        fillRect(x + i * size, y + j * size, size, size, bg);
                    }
                }
            }
        }
    }

    public void drawString(int x, int y, String s) {
        for (char c : s.toCharArray()) {
            if (c == '\n') {
                y += textsize * 8;
                x = 0;
            } else if (c == '\r') {
                // skip it
            } else {
                if (wrap && ((x + textsize * 6) >= width)) {
                    x = 0;            // Reset x to zero
                    y += textsize * 8; // Advance y one line
                }
                drawChar(x, y, c, color, bgcolor, textsize);
                x += textsize * 6;
            }
        }
    }

    public void write(char c) {
        if (c == '\n') {
            cursorY += textsize * 8;
            cursorX = 0;
        } else if (c == '\r') {
            // skip it
        } else {
            if (wrap && ((cursorX + textsize * 6) >= width)) {
                cursorX = 0;            // Reset x to zero
                cursorY += textsize * 8; // Advance y one line
            }
            drawChar(cursorX, cursorY, c, color, bgcolor, textsize);
            cursorX += textsize * 6;
        }
    }

    public void write(String s) {
        for (char c : s.toCharArray()) {
            write(c);
        }
    }

    public Rectangle getTextBounds(String str, int x, int y) {
        int originX = x;
        int originY = y;
        int w, h;
        int lineWidth = 0, maxWidth = 0; // Width of current, all lines
        for (char c : str.toCharArray()) {
            if (c != '\n') { // Not a newline
                if (c != '\r') { // Not a carriage return, is normal char
                    if (wrap && ((x + textsize * 6) >= width)) {
                        x = 0;            // Reset x to 0
                        y += textsize * 8; // Advance y by 1 line
                        if (lineWidth > maxWidth) {
                            maxWidth = lineWidth; // Save widest line
                        }
                        lineWidth = textsize * 6; // First char on new line
                    } else { // No line wrap, just keep incrementing X
                        lineWidth += textsize * 6; // Includes interchar x gap
                    }
                } // Carriage return - do nothing
            } else { // Newline
                x = 0;            // Reset x to 0
                y += textsize * 8; // Advance y by 1 line
                if (lineWidth > maxWidth) {
                    maxWidth = lineWidth; // Save widest line
                }
                lineWidth = 0;     // Reset lineWidth for new line
            }
        }
        // End of string
        if (lineWidth != 0) {
            y += textsize * 8; // Add height of last (or only) line
        }
        w = maxWidth - 1;      // Don't include last interchar x gap
        h = y - originY;
        return new Rectangle(originX, originY, w, h);
    }

    /**
     * Draws a bitmap. Each byte of data contains values for 8 consecutive
     * pixels in a row.
     *
     * @param x
     * @param y
     * @param bitmap data to display. Each bit stands for a pixel.
     * @param opaque if true, 0 - bits is drawn in background color, if false -
     * the color of corresponding pixel doesn't change
     */
    public void drawBitmap(int x, int y, byte[][] bitmap, boolean opaque) {
        int h = bitmap.length;
        if (h == 0) {
            return;
        }
        int w = bitmap[0].length;
        if (w == 0) {
            return;
        }
        for (int i = 0; i < h; i++) {
            if (y + i >= 0 && y + i < height) {
                for (int j = 0; j < w; j++) {
                    if (x + j * 8 >= 0 && x + j * 8 < width) {
                        byte b = bitmap[i][j];
                        for (int shift = 7; shift >= 0; shift--) {
                            int mask = 1 << shift;
                            if ((b & mask) != 0) {
                                setPixel(x + j * 8 + (7 - shift), y + i, color);
                            } else if (opaque) {
                                setPixel(x + j * 8 + (7 - shift), y + i, bgcolor);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws an image. The image is grayscalled before drawing.
     *
     * @param x
     * @param y
     * @param image an image to draw
     * @param opaque if true, 0 - bits is drawn in background color, if false -
     * the color of corresponding pixel doesn't change
     * @param invert true to make dark pixels of the image be drawn as bright
     * pixels on screen
     */
    public void drawImage(int x, int y, BufferedImage image, boolean opaque, boolean invert) {
        drawBitmap(x, y, convertToBitmap(image, invert), opaque);
    }

    public void clear() {
        Arrays.fill(buffer, (byte) 0);
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color textcolor) {
        this.color = textcolor;
    }

    public Color getBackgroundColor() {
        return bgcolor;
    }

    public void setBackgroundColor(Color bgcolor) {
        this.bgcolor = bgcolor;
    }

    public int getTextsize() {
        return textsize;
    }

    public void setTextsize(int textsize) {
        this.textsize = textsize;
    }

    public void setWordWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public void setCursor(int x, int y) {
        cursorX = x;
        cursorY = y;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public byte[] getBuffer() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private byte[][] convertToBitmap(BufferedImage image, boolean invert) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(image, gray);
        int byteWidth = (gray.getWidth() + 7) / 8;
        byte[][] result = new byte[gray.getHeight()][byteWidth];
        for (int i = 0; i < gray.getHeight(); i++) {
            for (int j = 0; j < byteWidth; j++) {
                int b = 0;
                for (int shift = 7; shift >= 0 && j * 8 + (7 - shift) < gray.getWidth(); shift--) {
                    int p = gray.getRGB(j * 8 + (7 - shift), i) & 0xFF;
                    if (p != 0 ^ invert) {
                        b |= (1 << shift);
                    }
                }
                result[i][j] = (byte) b;
            }
        }
        return result;
    }

    public static enum Color {

        DARK,
        BRIGHT,
        INVERSE
    }

    private static final char[] FONT = {
        0x00, 0x00, 0x00, 0x00, 0x00,
        0x3E, 0x5B, 0x4F, 0x5B, 0x3E,
        0x3E, 0x6B, 0x4F, 0x6B, 0x3E,
        0x1C, 0x3E, 0x7C, 0x3E, 0x1C,
        0x18, 0x3C, 0x7E, 0x3C, 0x18,
        0x1C, 0x57, 0x7D, 0x57, 0x1C,
        0x1C, 0x5E, 0x7F, 0x5E, 0x1C,
        0x00, 0x18, 0x3C, 0x18, 0x00,
        0xFF, 0xE7, 0xC3, 0xE7, 0xFF,
        0x00, 0x18, 0x24, 0x18, 0x00,
        0xFF, 0xE7, 0xDB, 0xE7, 0xFF,
        0x30, 0x48, 0x3A, 0x06, 0x0E,
        0x26, 0x29, 0x79, 0x29, 0x26,
        0x40, 0x7F, 0x05, 0x05, 0x07,
        0x40, 0x7F, 0x05, 0x25, 0x3F,
        0x5A, 0x3C, 0xE7, 0x3C, 0x5A,
        0x7F, 0x3E, 0x1C, 0x1C, 0x08,
        0x08, 0x1C, 0x1C, 0x3E, 0x7F,
        0x14, 0x22, 0x7F, 0x22, 0x14,
        0x5F, 0x5F, 0x00, 0x5F, 0x5F,
        0x06, 0x09, 0x7F, 0x01, 0x7F,
        0x00, 0x66, 0x89, 0x95, 0x6A,
        0x60, 0x60, 0x60, 0x60, 0x60,
        0x94, 0xA2, 0xFF, 0xA2, 0x94,
        0x08, 0x04, 0x7E, 0x04, 0x08,
        0x10, 0x20, 0x7E, 0x20, 0x10,
        0x08, 0x08, 0x2A, 0x1C, 0x08,
        0x08, 0x1C, 0x2A, 0x08, 0x08,
        0x1E, 0x10, 0x10, 0x10, 0x10,
        0x0C, 0x1E, 0x0C, 0x1E, 0x0C,
        0x30, 0x38, 0x3E, 0x38, 0x30,
        0x06, 0x0E, 0x3E, 0x0E, 0x06,
        0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x5F, 0x00, 0x00,
        0x00, 0x07, 0x00, 0x07, 0x00,
        0x14, 0x7F, 0x14, 0x7F, 0x14,
        0x24, 0x2A, 0x7F, 0x2A, 0x12,
        0x23, 0x13, 0x08, 0x64, 0x62,
        0x36, 0x49, 0x56, 0x20, 0x50,
        0x00, 0x08, 0x07, 0x03, 0x00,
        0x00, 0x1C, 0x22, 0x41, 0x00,
        0x00, 0x41, 0x22, 0x1C, 0x00,
        0x2A, 0x1C, 0x7F, 0x1C, 0x2A,
        0x08, 0x08, 0x3E, 0x08, 0x08,
        0x00, 0x80, 0x70, 0x30, 0x00,
        0x08, 0x08, 0x08, 0x08, 0x08,
        0x00, 0x00, 0x60, 0x60, 0x00,
        0x20, 0x10, 0x08, 0x04, 0x02,
        0x3E, 0x51, 0x49, 0x45, 0x3E,
        0x00, 0x42, 0x7F, 0x40, 0x00,
        0x72, 0x49, 0x49, 0x49, 0x46,
        0x21, 0x41, 0x49, 0x4D, 0x33,
        0x18, 0x14, 0x12, 0x7F, 0x10,
        0x27, 0x45, 0x45, 0x45, 0x39,
        0x3C, 0x4A, 0x49, 0x49, 0x31,
        0x41, 0x21, 0x11, 0x09, 0x07,
        0x36, 0x49, 0x49, 0x49, 0x36,
        0x46, 0x49, 0x49, 0x29, 0x1E,
        0x00, 0x00, 0x14, 0x00, 0x00,
        0x00, 0x40, 0x34, 0x00, 0x00,
        0x00, 0x08, 0x14, 0x22, 0x41,
        0x14, 0x14, 0x14, 0x14, 0x14,
        0x00, 0x41, 0x22, 0x14, 0x08,
        0x02, 0x01, 0x59, 0x09, 0x06,
        0x3E, 0x41, 0x5D, 0x59, 0x4E,
        0x7C, 0x12, 0x11, 0x12, 0x7C,
        0x7F, 0x49, 0x49, 0x49, 0x36,
        0x3E, 0x41, 0x41, 0x41, 0x22,
        0x7F, 0x41, 0x41, 0x41, 0x3E,
        0x7F, 0x49, 0x49, 0x49, 0x41,
        0x7F, 0x09, 0x09, 0x09, 0x01,
        0x3E, 0x41, 0x41, 0x51, 0x73,
        0x7F, 0x08, 0x08, 0x08, 0x7F,
        0x00, 0x41, 0x7F, 0x41, 0x00,
        0x20, 0x40, 0x41, 0x3F, 0x01,
        0x7F, 0x08, 0x14, 0x22, 0x41,
        0x7F, 0x40, 0x40, 0x40, 0x40,
        0x7F, 0x02, 0x1C, 0x02, 0x7F,
        0x7F, 0x04, 0x08, 0x10, 0x7F,
        0x3E, 0x41, 0x41, 0x41, 0x3E,
        0x7F, 0x09, 0x09, 0x09, 0x06,
        0x3E, 0x41, 0x51, 0x21, 0x5E,
        0x7F, 0x09, 0x19, 0x29, 0x46,
        0x26, 0x49, 0x49, 0x49, 0x32,
        0x03, 0x01, 0x7F, 0x01, 0x03,
        0x3F, 0x40, 0x40, 0x40, 0x3F,
        0x1F, 0x20, 0x40, 0x20, 0x1F,
        0x3F, 0x40, 0x38, 0x40, 0x3F,
        0x63, 0x14, 0x08, 0x14, 0x63,
        0x03, 0x04, 0x78, 0x04, 0x03,
        0x61, 0x59, 0x49, 0x4D, 0x43,
        0x00, 0x7F, 0x41, 0x41, 0x41,
        0x02, 0x04, 0x08, 0x10, 0x20,
        0x00, 0x41, 0x41, 0x41, 0x7F,
        0x04, 0x02, 0x01, 0x02, 0x04,
        0x40, 0x40, 0x40, 0x40, 0x40,
        0x00, 0x03, 0x07, 0x08, 0x00,
        0x20, 0x54, 0x54, 0x78, 0x40,
        0x7F, 0x28, 0x44, 0x44, 0x38,
        0x38, 0x44, 0x44, 0x44, 0x28,
        0x38, 0x44, 0x44, 0x28, 0x7F,
        0x38, 0x54, 0x54, 0x54, 0x18,
        0x00, 0x08, 0x7E, 0x09, 0x02,
        0x18, 0xA4, 0xA4, 0x9C, 0x78,
        0x7F, 0x08, 0x04, 0x04, 0x78,
        0x00, 0x44, 0x7D, 0x40, 0x00,
        0x20, 0x40, 0x40, 0x3D, 0x00,
        0x7F, 0x10, 0x28, 0x44, 0x00,
        0x00, 0x41, 0x7F, 0x40, 0x00,
        0x7C, 0x04, 0x78, 0x04, 0x78,
        0x7C, 0x08, 0x04, 0x04, 0x78,
        0x38, 0x44, 0x44, 0x44, 0x38,
        0xFC, 0x18, 0x24, 0x24, 0x18,
        0x18, 0x24, 0x24, 0x18, 0xFC,
        0x7C, 0x08, 0x04, 0x04, 0x08,
        0x48, 0x54, 0x54, 0x54, 0x24,
        0x04, 0x04, 0x3F, 0x44, 0x24,
        0x3C, 0x40, 0x40, 0x20, 0x7C,
        0x1C, 0x20, 0x40, 0x20, 0x1C,
        0x3C, 0x40, 0x30, 0x40, 0x3C,
        0x44, 0x28, 0x10, 0x28, 0x44,
        0x4C, 0x90, 0x90, 0x90, 0x7C,
        0x44, 0x64, 0x54, 0x4C, 0x44,
        0x00, 0x08, 0x36, 0x41, 0x00,
        0x00, 0x00, 0x77, 0x00, 0x00,
        0x00, 0x41, 0x36, 0x08, 0x00,
        0x02, 0x01, 0x02, 0x04, 0x02,
        0x3C, 0x26, 0x23, 0x26, 0x3C,
        0x1E, 0xA1, 0xA1, 0x61, 0x12,
        0x3A, 0x40, 0x40, 0x20, 0x7A,
        0x38, 0x54, 0x54, 0x55, 0x59,
        0x21, 0x55, 0x55, 0x79, 0x41,
        0x22, 0x54, 0x54, 0x78, 0x42, // a-umlaut
        0x21, 0x55, 0x54, 0x78, 0x40,
        0x20, 0x54, 0x55, 0x79, 0x40,
        0x0C, 0x1E, 0x52, 0x72, 0x12,
        0x39, 0x55, 0x55, 0x55, 0x59,
        0x39, 0x54, 0x54, 0x54, 0x59,
        0x39, 0x55, 0x54, 0x54, 0x58,
        0x00, 0x00, 0x45, 0x7C, 0x41,
        0x00, 0x02, 0x45, 0x7D, 0x42,
        0x00, 0x01, 0x45, 0x7C, 0x40,
        0x7D, 0x12, 0x11, 0x12, 0x7D, // A-umlaut
        0xF0, 0x28, 0x25, 0x28, 0xF0,
        0x7C, 0x54, 0x55, 0x45, 0x00,
        0x20, 0x54, 0x54, 0x7C, 0x54,
        0x7C, 0x0A, 0x09, 0x7F, 0x49,
        0x32, 0x49, 0x49, 0x49, 0x32,
        0x3A, 0x44, 0x44, 0x44, 0x3A, // o-umlaut
        0x32, 0x4A, 0x48, 0x48, 0x30,
        0x3A, 0x41, 0x41, 0x21, 0x7A,
        0x3A, 0x42, 0x40, 0x20, 0x78,
        0x00, 0x9D, 0xA0, 0xA0, 0x7D,
        0x3D, 0x42, 0x42, 0x42, 0x3D, // O-umlaut
        0x3D, 0x40, 0x40, 0x40, 0x3D,
        0x3C, 0x24, 0xFF, 0x24, 0x24,
        0x48, 0x7E, 0x49, 0x43, 0x66,
        0x2B, 0x2F, 0xFC, 0x2F, 0x2B,
        0xFF, 0x09, 0x29, 0xF6, 0x20,
        0xC0, 0x88, 0x7E, 0x09, 0x03,
        0x20, 0x54, 0x54, 0x79, 0x41,
        0x00, 0x00, 0x44, 0x7D, 0x41,
        0x30, 0x48, 0x48, 0x4A, 0x32,
        0x38, 0x40, 0x40, 0x22, 0x7A,
        0x00, 0x7A, 0x0A, 0x0A, 0x72,
        0x7D, 0x0D, 0x19, 0x31, 0x7D,
        0x26, 0x29, 0x29, 0x2F, 0x28,
        0x26, 0x29, 0x29, 0x29, 0x26,
        0x30, 0x48, 0x4D, 0x40, 0x20,
        0x38, 0x08, 0x08, 0x08, 0x08,
        0x08, 0x08, 0x08, 0x08, 0x38,
        0x2F, 0x10, 0xC8, 0xAC, 0xBA,
        0x2F, 0x10, 0x28, 0x34, 0xFA,
        0x00, 0x00, 0x7B, 0x00, 0x00,
        0x08, 0x14, 0x2A, 0x14, 0x22,
        0x22, 0x14, 0x2A, 0x14, 0x08,
        0x55, 0x00, 0x55, 0x00, 0x55, // #176 (25% block) missing in old code
        0xAA, 0x55, 0xAA, 0x55, 0xAA, // 50% block
        0xFF, 0x55, 0xFF, 0x55, 0xFF, // 75% block
        0x00, 0x00, 0x00, 0xFF, 0x00,
        0x10, 0x10, 0x10, 0xFF, 0x00,
        0x14, 0x14, 0x14, 0xFF, 0x00,
        0x10, 0x10, 0xFF, 0x00, 0xFF,
        0x10, 0x10, 0xF0, 0x10, 0xF0,
        0x14, 0x14, 0x14, 0xFC, 0x00,
        0x14, 0x14, 0xF7, 0x00, 0xFF,
        0x00, 0x00, 0xFF, 0x00, 0xFF,
        0x14, 0x14, 0xF4, 0x04, 0xFC,
        0x14, 0x14, 0x17, 0x10, 0x1F,
        0x10, 0x10, 0x1F, 0x10, 0x1F,
        0x14, 0x14, 0x14, 0x1F, 0x00,
        0x10, 0x10, 0x10, 0xF0, 0x00,
        0x00, 0x00, 0x00, 0x1F, 0x10,
        0x10, 0x10, 0x10, 0x1F, 0x10,
        0x10, 0x10, 0x10, 0xF0, 0x10,
        0x00, 0x00, 0x00, 0xFF, 0x10,
        0x10, 0x10, 0x10, 0x10, 0x10,
        0x10, 0x10, 0x10, 0xFF, 0x10,
        0x00, 0x00, 0x00, 0xFF, 0x14,
        0x00, 0x00, 0xFF, 0x00, 0xFF,
        0x00, 0x00, 0x1F, 0x10, 0x17,
        0x00, 0x00, 0xFC, 0x04, 0xF4,
        0x14, 0x14, 0x17, 0x10, 0x17,
        0x14, 0x14, 0xF4, 0x04, 0xF4,
        0x00, 0x00, 0xFF, 0x00, 0xF7,
        0x14, 0x14, 0x14, 0x14, 0x14,
        0x14, 0x14, 0xF7, 0x00, 0xF7,
        0x14, 0x14, 0x14, 0x17, 0x14,
        0x10, 0x10, 0x1F, 0x10, 0x1F,
        0x14, 0x14, 0x14, 0xF4, 0x14,
        0x10, 0x10, 0xF0, 0x10, 0xF0,
        0x00, 0x00, 0x1F, 0x10, 0x1F,
        0x00, 0x00, 0x00, 0x1F, 0x14,
        0x00, 0x00, 0x00, 0xFC, 0x14,
        0x00, 0x00, 0xF0, 0x10, 0xF0,
        0x10, 0x10, 0xFF, 0x10, 0xFF,
        0x14, 0x14, 0x14, 0xFF, 0x14,
        0x10, 0x10, 0x10, 0x1F, 0x00,
        0x00, 0x00, 0x00, 0xF0, 0x10,
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
        0xF0, 0xF0, 0xF0, 0xF0, 0xF0,
        0xFF, 0xFF, 0xFF, 0x00, 0x00,
        0x00, 0x00, 0x00, 0xFF, 0xFF,
        0x0F, 0x0F, 0x0F, 0x0F, 0x0F,
        0x38, 0x44, 0x44, 0x38, 0x44,
        0xFC, 0x4A, 0x4A, 0x4A, 0x34, // sharp-s or beta
        0x7E, 0x02, 0x02, 0x06, 0x06,
        0x02, 0x7E, 0x02, 0x7E, 0x02,
        0x63, 0x55, 0x49, 0x41, 0x63,
        0x38, 0x44, 0x44, 0x3C, 0x04,
        0x40, 0x7E, 0x20, 0x1E, 0x20,
        0x06, 0x02, 0x7E, 0x02, 0x02,
        0x99, 0xA5, 0xE7, 0xA5, 0x99,
        0x1C, 0x2A, 0x49, 0x2A, 0x1C,
        0x4C, 0x72, 0x01, 0x72, 0x4C,
        0x30, 0x4A, 0x4D, 0x4D, 0x30,
        0x30, 0x48, 0x78, 0x48, 0x30,
        0xBC, 0x62, 0x5A, 0x46, 0x3D,
        0x3E, 0x49, 0x49, 0x49, 0x00,
        0x7E, 0x01, 0x01, 0x01, 0x7E,
        0x2A, 0x2A, 0x2A, 0x2A, 0x2A,
        0x44, 0x44, 0x5F, 0x44, 0x44,
        0x40, 0x51, 0x4A, 0x44, 0x40,
        0x40, 0x44, 0x4A, 0x51, 0x40,
        0x00, 0x00, 0xFF, 0x01, 0x03,
        0xE0, 0x80, 0xFF, 0x00, 0x00,
        0x08, 0x08, 0x6B, 0x6B, 0x08,
        0x36, 0x12, 0x36, 0x24, 0x36,
        0x06, 0x0F, 0x09, 0x0F, 0x06,
        0x00, 0x00, 0x18, 0x18, 0x00,
        0x00, 0x00, 0x10, 0x10, 0x00,
        0x30, 0x40, 0xFF, 0x01, 0x01,
        0x00, 0x1F, 0x01, 0x01, 0x1E,
        0x00, 0x19, 0x1D, 0x17, 0x12,
        0x00, 0x3C, 0x3C, 0x3C, 0x3C,
        0x00, 0x00, 0x00, 0x00, 0x00 // #255 NBSP
    };

}
