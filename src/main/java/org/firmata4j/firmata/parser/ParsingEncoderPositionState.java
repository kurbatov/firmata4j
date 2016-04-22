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
 * This state parses extended encoder position state message and fires an event
 * that contains the encoder position. After receiving the last byte of the
 * message, the state transfers FSM to {@link WaitingForMessageState}.
 *
 * Response message format  <code>
 * 0 START_SYSEX                (0xF0)
 * 1 ENCODER_DATA               (0x61)
 * 2 first enc. #  &amp; first enc. dir.   [= (direction &lt;&lt; 6) | (#)] 
 * 3 first enc. position, bits 0-6
 * 4 first enc. position, bits 7-13
 * 5 first enc. position, bits 14-20
 * 6 first enc. position, bits 21-27
 * 7 second enc. #  &amp; second enc. dir. [= (direction &lt;&lt; 6) | (#)]
 * ...
 * N END_SYSEX                  (0xF7)
 * </code>
 * 
 * A single encoder position report message is a special case of the above 
 * ReportAll message containing the first encoder bytes 2-6 followed 
 * immediately by END_SYSEX.
 *
 * @author Jeffrey Kuhn &lt;drjrkuhn@gmail.com&gt;
 */
public class ParsingEncoderPositionState extends AbstractState {

    protected static byte DIRECTION_BIT = 6;
    protected static byte DIRECTION_MASK = 0x40;
    protected static byte CHANNEL_MASK = 0x3f;
    
    private int receivedCount;

    public ParsingEncoderPositionState(FiniteStateMachine fsm) {
        super(fsm);
        receivedCount = 0;
    }

    @Override
    public void process(byte b) {
        receivedCount++;
        if (b == END_SYSEX) {
            transitTo(WaitingForMessageState.class);
        } else if (receivedCount >= 5) {
            // 5 bytes per encoder position
            bufferize(b);
            // byte 0 contains direction flag and encoder number
            // direction flag is 0 for positive, 1 for negative
            // byte 1 contains bits 0-6
            // bytes 2 to 4 contain bits 7-13 to bits 21-27
            byte[] buffer = getBuffer();
            byte encoderId = (byte)(buffer[0] & CHANNEL_MASK);
            int directionFlag = (buffer[0] & DIRECTION_MASK) >> DIRECTION_BIT;
            long value = buffer[1];
            for (int i = 2; i < 5; i++) {
                value |= buffer[i] << 7 * (i - 1);
            }
            if (directionFlag > 0) {
                value = -value;
            }
            Event evt = new Event(ENCODER_STATE, FIRMATA_MESSAGE_EVENT_TYPE);
            evt.setBodyItem(ENCODER_ID, encoderId);
            evt.setBodyItem(ENCODER_POSITION, value);
            publish(evt);
            ParsingEncoderPositionState nextState = new ParsingEncoderPositionState(getFiniteStateMashine());
            transitTo(nextState);
        } else {
            bufferize(b);
        }
    }
}
