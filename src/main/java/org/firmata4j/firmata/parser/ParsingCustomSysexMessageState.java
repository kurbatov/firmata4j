/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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

import org.firmata4j.fsm.AbstractState;
import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.FiniteStateMachine;
import static org.firmata4j.firmata.parser.FirmataEventType.*;
import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This state collects raw binary data until it receives {@code SYSEX_END}
 * message and publishes it as a {@code SYSEX_CUSTOM_MESSAGE} event.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingCustomSysexMessageState extends AbstractState {

    public ParsingCustomSysexMessageState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        if (b == END_SYSEX) {
            Event event = new Event(SYSEX_CUSTOM_MESSAGE);
            event.setBodyItem(SYSEX_CUSTOM_MESSAGE, getBuffer());
            transitTo(WaitingForMessageState.class);
            publish(event);
        } else {
            bufferize(b);
        }
    }

}
