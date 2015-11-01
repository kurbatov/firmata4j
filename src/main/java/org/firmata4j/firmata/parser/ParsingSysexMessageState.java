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

import org.firmata4j.fsm.AbstractState;
import org.firmata4j.fsm.FiniteStateMachine;
import org.firmata4j.fsm.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * This state parses type of sysex message and transfers FSM to the state which
 * is able to handle the message.<br/>
 * If the state receives unknown type of sysex message, it transfers FSM to
 * {@link WaitingForMessageState}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class ParsingSysexMessageState extends AbstractState {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingSysexMessageState.class);
    private static final Map<Byte, Class<? extends State>> STATES;

    static {
        STATES = new HashMap<>();
        STATES.put(REPORT_FIRMWARE, ParsingFirmwareMessageState.class);
        STATES.put(EXTENDED_ANALOG, ParsingExtendedAnalogMessageState.class);
        STATES.put(CAPABILITY_RESPONSE, ParsingCapabilityResponseState.class);
        STATES.put(ANALOG_MAPPING_RESPONSE, ParsingAnalogMappingState.class);
        STATES.put(PIN_STATE_RESPONSE, PinStateParsingState.class);
        STATES.put(STRING_DATA, ParsingStringMessageState.class);
        STATES.put(I2C_REPLY,ParsingI2CMessageState.class);
    }

    public ParsingSysexMessageState(FiniteStateMachine fsm) {
        super(fsm);
    }

    @Override
    public void process(byte b) {
        Class<? extends State> nextState = STATES.get(b);
        if (nextState == null) {
            LOGGER.error("Unsupported sysex command {}.", b);
            nextState = WaitingForMessageState.class;
        }
        transitTo(nextState);
    }
}
