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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.firmata4j.fsm.FiniteStateMachine;
import org.firmata4j.fsm.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses byte-stream of Firmata messages.
 *
 * @author @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 * @author Ali Kia
 */
public abstract class FirmataParser extends FiniteStateMachine implements Parser {

    private Thread parserExecutor;
    private final ArrayBlockingQueue<byte[]> byteQueue = new ArrayBlockingQueue<>(128);
    private final AtomicBoolean running = new AtomicBoolean();

    private static final long WAIT_FOR_TERMINATION_DELAY = 3000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FirmataParser.class);

    public FirmataParser() {
        super(WaitingForMessageState.class);
    }

    @Override
    public void start() {
        if (!running.getAndSet(true)) {
            parserExecutor = new Thread(new JobRunner(), "firmata-parser-thread");
            parserExecutor.start();
        }
    }

    @Override
    public void stop() {
        if (running.getAndSet(false)) {
            byteQueue.clear();

            // interrupt the thread to ensure it falls out of the loop
            // and sees the shutdown request
            parserExecutor.interrupt();

            try {
                parserExecutor.join(WAIT_FOR_TERMINATION_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parser didn't stop successfully", e);
            }
        }
    }

    @Override
    public void parse(byte[] bytes) {
        if (!byteQueue.offer(bytes)) {
            LOGGER.warn("Parser reached byte queue limit. Some bytes where skipped.");
        }
    }

    private class JobRunner implements Runnable {

        @Override
        public void run() {
            try {
                while (running.get()) {
                    process(byteQueue.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
