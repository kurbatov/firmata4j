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

import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.AbstractState;
import org.firmata4j.fsm.FiniteStateMachine;

import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This state parses firmware message and fires an event that contains the name
 * and version of firmware.<br/>
 * After receiving the last byte of the message, the state transfers FSM to
 * {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingFirmwareMessageState extends AbstractState {

    public ParsingFirmwareMessageState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        if (b == END_SYSEX) {
            byte[] buffer = getBuffer();
            int major = buffer[0];
            int minor = buffer[1];
            String name = decode(buffer, 2, buffer.length - 2);
            Event event = new Event(FIRMWARE_MESSAGE, FIRMATA_MESSAGE_EVENT_TYPE);
            event.setBodyItem(FIRMWARE_MAJOR, major);
            event.setBodyItem(FIRMWARE_MINOR, minor);
            event.setBodyItem(FIRMWARE_NAME, name);
            transitTo(WaitingForMessageState.class);
            publish(event);
        } else {
            bufferize(b);
        }
    }

    private String decode(byte[] buffer, int offset, int length) {
        StringBuilder result = new StringBuilder();
        length = length >>> 1; // divide by two
        for (int i = 0; i < length; i++) {
            result.append((char) (buffer[offset++] + (buffer[offset++] << 7)));
        }
        return result.toString();
    }
}
