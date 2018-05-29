package org.firmata4j.firmata.parser;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.firmata4j.fsm.Event;
import org.junit.After;
import org.junit.Test;

public class FirmataParserTest {
    @After
    public void tearDown() throws InterruptedException {
        assertIfThreadStillRunning("firmata");
        assertIfThreadStillRunning("parser");
    }

    @Test
    public void testConstruct() {
        FirmataParser parser = new FirmataParser() {
            @Override
            public void onEvent(Event event) {

            }
        };

        parser.start();

        parser.stop();
    }

    @Test
    public void testStartAndStopTwice() {
        FirmataParser parser = new FirmataParser() {
            @Override
            public void onEvent(Event event) {

            }
        };

        parser.start();
        parser.start();

        parser.stop();
        parser.stop();
    }

    @Test
    public void testParse() throws InterruptedException {
        final AtomicInteger eventCount = new AtomicInteger(0);
        FirmataParser parser = new FirmataParser() {
            @Override
            public void onEvent(Event event) {
                eventCount.incrementAndGet();
            }
        };

        parser.start();

        parser.parse(new byte[] {1, 2, 3});

        int i = 0;
        while(eventCount.get() < 3 && i < 20) {
            Thread.sleep(100);
            i++;
        }
        assertEquals("Should receive 3 events for each byte an 'unknown' error event",
                3, eventCount.get());

        parser.stop();
    }

    private static void assertIfThreadStillRunning(final String contains) throws InterruptedException {
        int count = Thread.currentThread().getThreadGroup().activeCount();

        Thread[] threads = new Thread[count];
        Thread.currentThread().getThreadGroup().enumerate(threads);

        for (Thread t : threads) {
            if (t != null && t.getName().contains(contains)) {
                t.join();
            }
        }
    }
}
