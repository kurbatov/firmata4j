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

package org.firmata4j.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This state stores FSM it belongs to. It facilitates transitions between
 * states and buffering of the input data.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public abstract class AbstractState implements State {

    private FiniteStateMachine fsm;
    private byte[] buffer = new byte[128];
    private int index;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractState.class);

    public AbstractState(FiniteStateMachine fsm) {
        this.fsm = fsm;
    }

    @Override
    public FiniteStateMachine getFiniteStateMashine() {
        return fsm;
    }

    /**
     * Transfers the FSM to the state of specified class. This method takes only
     * classes that provide a constructor taking a {@link FiniteStateMachine}
     * instance as a parameter. The {@link IllegalArgumentException} is thrown
     * otherwise.
     *
     * @param stateClass the state class
     */
    protected void transitTo(Class<? extends State> stateClass) {
        fsm.transitTo(stateClass);
    }

    /**
     * Transfers the FSM to the specified state.
     *
     * @param state the state
     */
    protected void transitTo(State state) {
        fsm.transitTo(state);
    }

    /**
     * Publishes the event to the FSM.
     *
     * @param event the event
     */
    protected void publish(Event event) {
        fsm.onEvent(event);
    }

    /**
     * Stores the byte to the internal buffer to have a chance of getting the
     * data by a chunk latter.
     *
     * @param b the byte
     */
    protected void bufferize(byte b) {
        if (index == buffer.length) {
            byte[] newBuffer = new byte[buffer.length * 2];
            System.arraycopy(buffer, 0, newBuffer, 0, index);
            buffer = newBuffer;
        }
        buffer[index++] = b;
    }

    /**
     * Returns the data collected so far.
     *
     * @return the buffered bytes
     */
    protected byte[] getBuffer() {
        byte[] result = new byte[index];
        System.arraycopy(buffer, 0, result, 0, index);
        return result;
    }
}
