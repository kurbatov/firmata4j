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
 * This state parses extended analog state message and fires an event that
 * contains information about the state of an analog input where the state has
 * changed.<br/>
 * This kind of message is received when pin id of the analog input is greater
 * than 15 and/or value on the input is greater than 16383.<br/>
 * After receiving the last byte of the message, the state transfers FSM to
 * {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingExtendedAnalogMessageState extends AbstractState {

    public ParsingExtendedAnalogMessageState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        if (b == END_SYSEX) {
            byte[] buffer = getBuffer();
            int pinId = buffer[0];
            long value = buffer[1];
            for (int i = 2; i < buffer.length; i++) {
                value |= buffer[i] << 7 * (i - 1);
            }
            Event evt = new Event(ANALOG_MESSAGE_RESPONSE, FIRMATA_MESSAGE_EVENT_TYPE);
            evt.setBodyItem(PIN_ID, pinId);
            evt.setBodyItem(PIN_VALUE, value);
            publish(evt);
            transitTo(WaitingForMessageState.class);
        } else {
            bufferize(b);
        }
    }
}
