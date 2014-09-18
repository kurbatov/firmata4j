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
 * This state parses digital message and fires an event that contains
 * information about the state of each pin of the port where state of at least
 * one pin has changed.<br/>
 * After receiving the last byte, the state transfers FSM to
 * {@link WaitingForMessageState}.<br/>
 * When digital input of particular pin has been changed, Firmata transmits
 * state of whole port the pin is contained in.<br/>
 * A port is a set of 8 pins. State of every pin is represented inside one byte
 * by bit (0 - low, 1 - high).
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingDigitalMessageState extends AbstractState {

    private int portId, counter, value;

    public ParsingDigitalMessageState(FiniteStateMachine fsm, int portId) {
        super(fsm);
        this.portId = portId;
    }

    @Override
    public void process(byte b) {
        switch (counter) {
            case 0:
                value = b;
                counter++;
                break;
            case 1:
                value |= b << 7;
                int pinId = portId * 8;
                for (int i = 0; i < 8; i++) {
                    Event evt = new Event(DIGITAL_MESSAGE_RESPONSE, FIRMATA_MESSAGE_EVENT_TYPE);
                    evt.setBodyItem(PIN_ID, pinId + i);
                    evt.setBodyItem(PIN_VALUE, (value >>> i) & 0x01);
                    publish(evt);
                }
                transitTo(WaitingForMessageState.class);
                break;
        }
    }
}
