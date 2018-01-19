package org.firmata4j.firmata;

import org.firmata4j.firmata.parser.WaitingForMessageState;
import org.firmata4j.fsm.FiniteStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author akia
 * @since 2018-01
 */
public abstract class FirmataParser extends FiniteStateMachine {

    private static final long NO_DATA_DELAY = 15;
    private static final long WAIT_FOR_TERMINATION_DELAY = 3000;
    private final Logger logger = LoggerFactory.getLogger(FirmataParser.class);
    private final ArrayBlockingQueue<byte[]> byteQueue = new ArrayBlockingQueue<>(128);
    private Thread parserExecutor;
    private boolean running;

    public FirmataParser() {
        super(WaitingForMessageState.class);
        parserExecutor = new Thread(new JobRunner(), "firmata-parser-thread");
        running = true;
        parserExecutor.start();
    }

    public void stopParser() {
        running = false;
        byteQueue.clear();
        try {
            parserExecutor.join(WAIT_FOR_TERMINATION_DELAY);
        } catch (InterruptedException e) {
            logger.error("parser was not stopped successfully");
            Thread.currentThread().interrupt();
        }
    }

    public void parse(byte[] bytes) {
        if (!byteQueue.offer(bytes)) {
            logger.warn("parser byte queue limit reached. some bytes where skipped");
        }
    }

    private class JobRunner implements Runnable {
        public void run() {
            try {
                while (running) {
                    if (byteQueue.isEmpty()) {
                        Thread.sleep(NO_DATA_DELAY);
                    } else {
                        process(byteQueue.take());
                    }
                }
            } catch (InterruptedException e) {
                logger.error("firmata parser executor was stopped abnormally");
                Thread.currentThread().interrupt();
            }
        }
    }
}
