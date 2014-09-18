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
 * This state parses the analog message that points an analog input has
 * changed.<br/>
 * The analog message consists of three bytes: first contains four bits of a
 * command and four bits with port id; second and third contains current value
 * of port.<br/>
 * After receiving the last byte, the state fires an event to FSM and transfers
 * FSM to {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingAnalogMessageState extends AbstractState {

    private int portId, counter, value;

    public ParsingAnalogMessageState(FiniteStateMachine fsm, int portId) {
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
                Event evt = new Event(ANALOG_MESSAGE_RESPONSE, FIRMATA_MESSAGE_EVENT_TYPE);
                evt.setBodyItem(PIN_ID, portId);
                evt.setBodyItem(PIN_VALUE, value);
                publish(evt);
                transitTo(WaitingForMessageState.class);
                break;
        }
    }
}
