/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Oleg Kurbatov (o.v.kurbatov@gmail.com)
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
package org.firmata4j.firmata;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.firmata4j.Consumer;
import org.firmata4j.fsm.Event;

/**
 * Implementation of low-level event hadler that supposed to be used as a
 * watchdog.
 *
 * It gets activated by the first event received since the watchdog subscribed.
 *
 * Example:
 * <pre>
 * FirmataWatchdog watchdog = new FirmataWatchdog(3000, new Runnable() {
 *     @Override
 *     public void run() {
 *         // do something when there were no low-level events during 3000 milliseconds
 *         }
 *     });
 * device.addProtocolMessageHandler(FirmataEventType.ANY, watchdog);
 * //...
 * device.start();
 * </pre>
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FirmataWatchdog extends Consumer<Event> {

    private final long timeout;

    private final Runnable action;

    private final AtomicBoolean active = new AtomicBoolean(false);

    private volatile long lastTimestamp = 0;

    private final static TimeUnit UNIT = TimeUnit.MILLISECONDS;

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("firmata-watchdog"));

    /**
     * Creates a watchdog that activates itself on the first received event.
     *
     * @param timeout timeout after the latest low-level event that triggers the
     * action
     * @param action the action that is to be triggered when there were no
     * low-level events during timeout since the latest event
     */
    public FirmataWatchdog(long timeout, Runnable action) {
        this.timeout = timeout;
        this.action = action;
    }

    @Override
    public void accept(Event evt) {
        if (lastTimestamp == 0) { // got fitst event
            enable();
        }
        lastTimestamp = evt.getTimestamp();
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        if (active) {
            enable();
        } else {
            disable();
        }
    }

    public void enable() {
        if (!active.getAndSet(true)) {
            EXECUTOR.schedule(watch, timeout, UNIT);
        }
    }

    public void disable() {
        active.set(false);
    }

    private final Runnable watch = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - lastTimestamp >= timeout) {
                action.run();
            }
            if (active.get()) {
                EXECUTOR.schedule(watch, timeout, UNIT);
            }
        }
    };

}
