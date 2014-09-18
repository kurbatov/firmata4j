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

import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.AbstractState;
import org.firmata4j.fsm.FiniteStateMachine;

import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This is initial default state of {@link FirmataDevice}.<br/>
 * The state is waiting for command and determines to which state transfers to
 * parse further data. It extracts additional data from a command byte, when the
 * command contains that, and hands it to the next state.<br/>
 * The state skips unknown command bytes throwing events with error messages.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class WaitingForMessageState extends AbstractState {

    public WaitingForMessageState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        // first byte may contain not only command but additional information as well
        byte command = b < (byte) 0xF0 ? (byte) (b & 0xF0) : b;
        FiniteStateMachine fsm = getFiniteStateMashine();
        switch (command) {
            case DIGITAL_MESSAGE:
                transitTo(new ParsingDigitalMessageState(fsm, b & 0x0F));
                break;
            case ANALOG_MESSAGE:
                transitTo(new ParsingAnalogMessageState(fsm, b & 0x0F));
                break;
            case REPORT_VERSION:
                transitTo(ParsingVersionMessageState.class);
                break;
            case START_SYSEX:
                transitTo(ParsingSysexMessageState.class);
                break;
            case SYSTEM_RESET:
                publish(new Event(SYSTEM_RESET_MESSAGE, FIRMATA_MESSAGE_EVENT_TYPE));
                break;
            default:
                //skip non control token
                Event evt = new Event(ERROR_MESSAGE, FIRMATA_MESSAGE_EVENT_TYPE);
                evt.setBodyItem(ERROR_DESCRIPTION, String.format("Unknown control token has been receved. Skipping. 0x%2X", b));
                publish(evt);
        }
    }
}
