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
 * This state parses capability response and fires an event that contains
 * information about pins and their supported modes.<br/>
 * After receiving the last byte, the state transfers FSM to
 * {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingCapabilityResponseState extends AbstractState {

    private byte pinId;

    public ParsingCapabilityResponseState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        if (b == END_SYSEX) {
            transitTo(WaitingForMessageState.class);
        } else if (b == 127) {
            byte[] buffer = getBuffer();
            byte[] supportedModes = new byte[buffer.length / 2];
            for (int i = 0; i < buffer.length; i += 2) {
                //every second byte contains mode's resolution of pin
                supportedModes[i / 2] = buffer[i];
            }
            Event evt = new Event(PIN_CAPABILITIES_MESSAGE, FIRMATA_MESSAGE_EVENT_TYPE);
            evt.setBodyItem(PIN_ID, pinId);
            evt.setBodyItem(PIN_SUPPORTED_MODES, supportedModes);
            publish(evt);
            ParsingCapabilityResponseState nextState = new ParsingCapabilityResponseState(getFiniteStateMashine());
            nextState.setPinId(++pinId);
            transitTo(nextState);
        } else {
            bufferize(b);
        }
    }

    public void setPinId(byte pinId) {
        this.pinId = pinId;
    }
}
