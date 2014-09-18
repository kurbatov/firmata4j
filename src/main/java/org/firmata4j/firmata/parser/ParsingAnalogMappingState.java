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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This state parses analog mapping message and fires an event that contains
 * information about how the analog ports match to the pin indexes.<br/>
 * After receiving the last byte, the state transfers FSM to
 * {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingAnalogMappingState extends AbstractState {

    private int portId;
    private final Map<Integer, Integer> mapping = new ConcurrentHashMap<Integer, Integer>();

    public ParsingAnalogMappingState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        if (b == END_SYSEX) {
            Event evt = new Event(ANALOG_MAPPING_MESSAGE, FIRMATA_MESSAGE_EVENT_TYPE);
            evt.setBodyItem(ANALOG_MAPPING, mapping);
            publish(evt);
            transitTo(WaitingForMessageState.class);
        } else if (b != 127) { // if pin does support analog, corresponding analog id is in the byte
            mapping.put((int) b, portId);
        }
        portId++;
    }
}
