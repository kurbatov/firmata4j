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
package org.firmata4j.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.firmata4j.Encoder;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;

/**
 * Utility class to format report data
 *
 * @author Jeffrey Kuhn &lt;drjrkuhnv@gmail.com&gt;
 */
public class DeviceReport {

    public static String formatPinList(IODevice device) {
        int numPins = device.getPinsCount();
        // line 1: Pin first digits
        StringBuilder report = new StringBuilder(String.format("%11s", ""));
        for (int p = 0; p < numPins; p++) {
            int i = device.getPin(p).getIndex();
            report.append(' ');
            report.append((i >= 10) ? (i / 10) : " ");
        }
        // line 2: Pin second digits
        report.append(String.format("\n%-11s", "Pin modes"));
        for (int p = 0; p < numPins; p++) {
            int i = device.getPin(p).getIndex();
            report.append(' ');
            report.append((i % 10));
        }
        // line 3: separators
        report.append("\n----------- ");
        for (int c = numPins; c-- > 0;) {
            report.append("--");
        }
        // line 4-n: pin mode grid
        for (Pin.Mode m : Pin.Mode.values()) {
            report.append(String.format("\n%11s ", m.toString()));
            for (int p = 0; p < numPins; p++) {
                report.append(device.getPin(p).supports(m) ? "X " : ". ");
            }
        }
        // lines n+1 to end: current pin mode
        final String NONE = "none";
        int maxModeLength = 0;
        for (int p=0; p < numPins; p++) {
            Pin.Mode m = device.getPin(p).getMode();
            int modeLength = (m==null) ? NONE.length() : m.toString().length();
            if (modeLength > maxModeLength) {
                maxModeLength = modeLength;
            }
        }
        for (int r = 0; r < maxModeLength; r++) {
            report.append(String.format("\n%11s", (r == 0) ? "current" : ""));
            for (int p = 0; p < numPins; p++) {
                Pin.Mode m = device.getPin(p).getMode();
                String mode = (m == null) ? NONE : m.toString();
                report.append(' ');
                report.append((r < mode.length()) ? mode.charAt(r) : ' ');
            }
        }

        report.append('\n');
        return report.toString();
    }

    public static String formatEncoderList(IODevice device) {
        int numEncoders = device.getEncoderCount();
        if (numEncoders == 0) {
            return "Encoders not supported by firmware\n";
        }
        // line 1: header
        StringBuilder report = new StringBuilder(String.format("%11s A  B ", ""));
        // line 2: separators
        report.append("\n----------- -- --");
        // line 3-n: encoder pin grid
        for (int e = 0; e < numEncoders; e++) {
            report.append(String.format("\n%11s ", "Encoder " + e));
            Encoder encoder = device.getEncoder(e);
            if (encoder.isAttached()) {
                report.append(String.format("%2d %2d", encoder.getPinA().getIndex(), encoder.getPinB().getIndex()));
            } else {
                report.append(" .  .");
            }
        }
        report.append('\n');
        return report.toString();
    }
}
