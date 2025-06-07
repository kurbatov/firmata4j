/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.firmata4j.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finite State Machine base implementation.<br>
 * The finite state machine is not thread-safe by its nature. This
 * implementation does not cope with simultaneously received bytes. The bytes
 * have to be fed to the FSM one by one in a single thread that should define
 * the order the bytes go in.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FiniteStateMachine {

    public static final String ALL_EVENTS = "*";
    public static final String FSM_IS_IN_TERMINAL_STATE = "fsm is in terminal state";
    private static final Logger LOGGER = LoggerFactory.getLogger(FiniteStateMachine.class);
    private static final Consumer<Event> DEFAULT_HANDLER = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            LOGGER.warn(
                "No specific event handler is registered for {}:{}.",
                event.getType(),
                event.getBody()
            );
        }
    };
    private final Map<String, Consumer<Event>> handlers;
    private Executor eventHandlingExecutor = DirectExecutor.INSTANCE;
    private State currentState;
    
    /**
     * Constructs the finite state machine in the terminal state, i.e. without
     * any particular current state. So that it will do noting on
     * {@link #process(byte)} method invocation until a state will be set using
     * {@link #transitTo(org.firmata4j.fsm.State)}.
     */
    public FiniteStateMachine() {
        handlers = new ConcurrentHashMap<>();
        handlers.put(ALL_EVENTS, new Consumer<Event>() {
            @Override
            public void accept(Event t) {
                // default wildcard handler does nothing
            }
        });
    }

    /**
     * Constructs the finite state machine in the state of the specified class.
     *
     * @param stateClass the class of initial state of the FSM
     * @throws IllegalArgumentException when creating of the state instance is
     * impossible (likely because of lack of the constructor taking the FSM as a
     * parameter)
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public FiniteStateMachine(Class<? extends State> stateClass) {
        this();
        try {
            // LeakingThisInConstructor is suppresed since the state instance doesn't run its own thread
            currentState = stateClass.getConstructor(FiniteStateMachine.class).newInstance(this);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Cannot instantiate the initial state", ex);
        }
    }

    /**
     * Assigns an executor responsible for performing event hanling.
     *
     * @param executor the executor that will perform event handling
     */
    public void setEventHandlingExecutor(Executor executor) {
        this.eventHandlingExecutor = executor;
    }

    /**
     * Transfers the FSM to the new state.
     *
     * @param state the new state
     */
    public void transitTo(State state) {
        currentState = state;
    }

    /**
     * Transfers the FSM to the new state of the specified class.<br>
     * This method takes only classes that provide a constructor taking a
     * {@link FiniteStateMachine} instance as a parameter. The
     * {@link IllegalArgumentException} is thrown otherwise.
     *
     * @param stateClass the state class
     * @throws IllegalArgumentException when the state class does not provide a
     * constructor taking {@link FiniteStateMachine} instance as a single
     * parameter.
     */
    public void transitTo(Class<? extends State> stateClass) {
        try {
            State nextState = stateClass.getConstructor(FiniteStateMachine.class).newInstance(this);
            transitTo(nextState);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Cannot instantiate the new state", ex);
        }
    }

    /**
     * Returns current state of the FSM.
     *
     * @return the current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Hands the byte to be processed with the current state.
     *
     * @param b input byte
     */
    public void process(byte b) {
        if (currentState == null) {
            LOGGER.debug("{} is in terminal state", this);
            Event evt = new Event(FSM_IS_IN_TERMINAL_STATE);
            evt.setBodyItem("fsm", this);
            handle(evt);
        } else {
            LOGGER.trace("processing of byte {} with {}", b, currentState);
            currentState.process(b);
        }
    }

    /**
     * Hands bytes from the buffer to be processed by the current state
     * sequentially.
     *
     * @param buffer the bytes to be processed
     */
    public void process(byte[] buffer) {
        process(buffer, 0, buffer.length);
    }

    /**
     * Hands bytes from the buffer to be processed by the current state
     * sequentially.
     *
     * @param buffer the bytes to be processed
     * @param offset the index of the first byte to process
     * @param length the number of the bytes to be processed
     */
    public void process(byte[] buffer, int offset, int length) {
        int finalIndex = offset + length;
        for (int i = offset; i < finalIndex; i++) {
            process(buffer[i]);
        }
    }
    
    /**
     * Adds a handler for specified event type.
     *
     * If there already is aanother handler for that event type, this handler
     * gets added to the end of handler chain.
     *
     * @param eventType type of event the handler is supposed to deal with
     * @param handler an object that gets an event to process
     */
    public synchronized void addHandler(String eventType, Consumer<Event> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.put(eventType, handlers.get(eventType).andThen(handler));
        } else {
            handlers.put(eventType, handler);
        }
    }

    /**
     * Handles an event that occurs during processing of the input.<br>
     * The method is invoked by the current state of FSM when an event occurs.
     *
     * @param event the event
     */
    public void handle(final Event event) {
        eventHandlingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                handlers.getOrDefault(event.getType(), DEFAULT_HANDLER).accept(event);
                handlers.get(ALL_EVENTS).accept(event);
            }
        });
    }

}
