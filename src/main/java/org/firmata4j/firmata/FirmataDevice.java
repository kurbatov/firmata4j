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
package org.firmata4j.firmata;

import org.firmata4j.*;
import org.firmata4j.firmata.parser.FirmataParser;
import org.firmata4j.firmata.parser.WaitingForMessageState;
import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.FiniteStateMachine;
import org.firmata4j.transport.SerialTransport;
import org.firmata4j.transport.TransportInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.firmata4j.firmata.parser.FirmataEventType.*;
import static org.firmata4j.firmata.parser.FirmataToken.*;

/**
 * Implements {@link IODevice} that is using Firmata protocol.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class FirmataDevice implements IODevice {

    private Parser parser;
    private TransportInterface transport;
    private FiniteStateMachine protocol;
    private final Set<IODeviceEventListener> listeners = Collections.synchronizedSet(new LinkedHashSet<IODeviceEventListener>());
    private final List<FirmataPin> pins = Collections.synchronizedList(new ArrayList<FirmataPin>());
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final AtomicInteger initializedPins = new AtomicInteger(0);
    private final AtomicInteger longestI2CDelay = new AtomicInteger(0);
    private final Map<Byte, FirmataI2CDevice> i2cDevices = new HashMap<>();
    private volatile Map<String, Object> firmwareInfo;
    private volatile Map<Integer, Integer> analogMapping;
    
    private static final long TIMEOUT = 15000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FirmataDevice.class);

    /**
     * Constructs FirmataDevice instance on specified serial port.
     *
     * @param portName the serial port name the device is connected to
     */
    public FirmataDevice(String portName) {
        this(new SerialTransport(portName));
    }

    /**
     * Constructs FirmataDevice instance using the specified transport.
     *
     * @param transport the communication channel
     */
    public FirmataDevice(TransportInterface transport) {
        this(transport, new FiniteStateMachine(WaitingForMessageState.class));
    }
    
    /**
     * Constructs FirmataDevice instance using the specified transport and
     * protocol.
     *
     * @param transport the communication channel
     * @param protocol finite state machine that can understand the byte stream
     * from the transport
     */
    public FirmataDevice(TransportInterface transport, FiniteStateMachine protocol) {
        protocol.setEventHandlingExecutor(Executors.newSingleThreadExecutor(new DaemonThreadFactory("firmata-event-handler")));
        protocol.addHandler(PROTOCOL_MESSAGE, onProtocolReceive);
        protocol.addHandler(FIRMWARE_MESSAGE, onFirmwareReceive);
        protocol.addHandler(PIN_CAPABILITIES_MESSAGE, onCapabilitiesReceive);
        protocol.addHandler(PIN_STATE, onPinStateReceive);
        protocol.addHandler(ANALOG_MAPPING_MESSAGE, onAnalogMappingReceive);
        protocol.addHandler(ANALOG_MESSAGE_RESPONSE, onAnalogMessageReceive);
        protocol.addHandler(DIGITAL_MESSAGE_RESPONSE, onDigitalMessageReceive);
        protocol.addHandler(I2C_MESSAGE, onI2cMessageReceive);
        protocol.addHandler(STRING_MESSAGE, onStringMessageReceive);
        protocol.addHandler(FiniteStateMachine.FSM_IS_IN_TERMINAL_STATE, new Consumer<Event>() {
            @Override
            public void accept(Event t) {
                LOGGER.error("Parser has reached the terminal state. It may be due receiving of unsupported command.");
            }
        });
        parser = new FirmataParser(protocol);
        transport.setParser(parser);
        this.protocol = protocol;
        this.transport = transport;
    }

    @Override
    public void start() throws IOException {
        if (!started.getAndSet(true)) {
            /*
             The startup strategy is to start device and immediately
             send the REPORT_FIRMWARE message.  When we receive the
             firmware name reply, then we know the board is ready to
             communicate.

             For boards like Arduino which use DTR to reset, they may
             reboot the moment the port opens.  They will not hear this
             REPORT_FIRMWARE message, but when they finish booting up
             they will send the firmware message.

             For boards that do not reboot when the port opens, they
             will hear this REPORT_FIRMWARE request and send the
             response.  If this REPORT_FIRMWARE request isn't sent,
             these boards will not automatically send this info.

             Either way, when we hear the REPORT_FIRMWARE reply, we
             know the board is alive and ready to communicate.
             */
            try {
                parser.start();
                transport.start();
                sendMessage(FirmataMessageFactory.REQUEST_FIRMWARE);
            } catch (IOException ex) {
                transport.stop();
                parser.stop();
                throw ex;
            }
        }
    }

    @Override
    public void stop() throws IOException {
        shutdown();
        IOEvent event = new IOEvent(this);
        for (IODeviceEventListener l : listeners) {
            l.onStop(event);
        }

    }

    @Override
    public void ensureInitializationIsDone() throws InterruptedException {
        if (!started.get()) {
            try {
                start();
            } catch (IOException ex) {
                throw new InterruptedException(ex.getMessage());
            }
        }
        long timePassed = 0L;
        long timeout = 100;
        while (!isReady()) {
            if (timePassed >= TIMEOUT) {
                throw new InterruptedException("Connection timeout");
            }
            timePassed += timeout;
            Thread.sleep(timeout);
        }
    }

    @Override
    public boolean isReady() {
        return ready.get();
    }

    @Override
    public void addEventListener(IODeviceEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(IODeviceEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Set<Pin> getPins() {
        return new HashSet<Pin>(pins);
    }

    @Override
    public int getPinsCount() {
        return pins.size();
    }

    @Override
    public Pin getPin(int index) {
        return pins.get(index);
    }

    @Override
    public synchronized I2CDevice getI2CDevice(byte address) throws IOException {
        if (!i2cDevices.containsKey(address)) {
            i2cDevices.put(address, new FirmataI2CDevice(this, address));
        }
        sendMessage(FirmataMessageFactory.i2cConfigRequest(longestI2CDelay.get()));
        return i2cDevices.get(address);
    }

    @Override
    public String getProtocol() {
        return MessageFormat.format(
                "{0} - {1}.{2}",
                firmwareInfo.get(FIRMWARE_NAME),
                firmwareInfo.get(FIRMWARE_MAJOR),
                firmwareInfo.get(FIRMWARE_MINOR));
    }

    @Override
    public void addProtocolMessageHandler(String messageType, Consumer<Event> handler) {
        protocol.addHandler(messageType, handler);
    }
    
    //TODO add get firmware method

    @Override
    public void sendMessage(String message) throws IOException {
        if (message.length() > 15) {
            LOGGER.warn("Firmata 2.3.6 implementation has input buffer only 32 bytes so you can safely send only 15 characters log messages");
        }
        sendMessage(FirmataMessageFactory.stringMessage(message));
    }

    /**
     * Sends raw message to connected Firmata device using open port.<br/>
     * Consider using {@link FirmataMessageFactory} methods to prepare binary
     * messages before sending.
     *
     * @param msg the Firmata message
     * @throws IOException when writing fails
     * @see FirmataMessageFactory
     */
    @Override
    public void sendMessage(byte... msg) throws IOException {
        transport.write(msg);
    }

    /**
     * Notifies the device listeners that a pin has changed.<br/>
     * This method is package-wide accessible to be used by {@link FirmataPin}.
     *
     * @param event the event to be send to the listeners
     */
    void pinChanged(IOEvent event) {
        for (IODeviceEventListener listener : listeners) {
            listener.onPinChange(event);
        }
    }

    /**
     * Sets delay between the moment an I2C device's register is written to and
     * the moment when the data can be read from that register. The delay is set
     * per firmata-device (not per I2C device). So firmata-device uses the
     * longest delay.
     *
     * @param delay longest delay between writing to I2C and reading from it
     * @throws IOException when sending of configuration to firmata-device
     *                     failed
     */
    void setI2CDelay(int delay) throws IOException {
        byte[] message = FirmataMessageFactory.i2cConfigRequest(delay);
        int longestDelaySoFar = longestI2CDelay.get();
        while (longestDelaySoFar < delay) {
            if (longestI2CDelay.compareAndSet(longestDelaySoFar, delay)) {
                sendMessage(message);
            }
            longestDelaySoFar = longestI2CDelay.get();
        }
    }

    /**
     * Tries to release all resources and properly terminate the connection to
     * the hardware.
     *
     * @throws IOException when communication could not be stopped properly
     */
    private void shutdown() throws IOException {
        ready.set(false);
        sendMessage(FirmataMessageFactory.analogReport(false));
        sendMessage(FirmataMessageFactory.digitalReport(false));
        parser.stop();
        transport.stop();
    }

    /**
     * Describes reaction to protocol receiving.
     */
    private final Consumer<Event> onProtocolReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            if (!event.getBodyItem(PROTOCOL_MAJOR).equals((int) FIRMATA_MAJOR_VERSION)) {
                LOGGER.error("Current version of firmata protocol on device ({}.{}) is not compatible with version of firmata4j ({}.{}).",
                        event.getBodyItem(PROTOCOL_MAJOR),
                        event.getBodyItem(PROTOCOL_MINOR),
                        FIRMATA_MAJOR_VERSION,
                        FIRMATA_MINOR_VERSION);
            } else if (!event.getBodyItem(PROTOCOL_MINOR).equals((int) FIRMATA_MINOR_VERSION)) {
                LOGGER.warn("Current version of firmata protocol on device ({}.{}) differs from version supported by firmata4j ({}.{})."
                                + " Though these are compatible you may experience some issues.",
                        event.getBodyItem(PROTOCOL_MAJOR),
                        event.getBodyItem(PROTOCOL_MINOR),
                        FIRMATA_MAJOR_VERSION,
                        FIRMATA_MINOR_VERSION);
            }
        }
    };

    /**
     * Describes reaction to firmware data receiving.
     */
    private final Consumer<Event> onFirmwareReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            firmwareInfo = event.getBody();
            try {
                sendMessage(FirmataMessageFactory.REQUEST_CAPABILITY);
            } catch (IOException ex) {
                LOGGER.error("Error requesting device capabilities.", ex);
            }
        }
    };

    /**
     * Describes reaction to capabilities data receiving.
     */
    private final Consumer<Event> onCapabilitiesReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            byte pinId = (Byte) event.getBodyItem(PIN_ID);
            FirmataPin pin = new FirmataPin(FirmataDevice.this, pinId);
            for (byte i : (byte[]) event.getBodyItem(PIN_SUPPORTED_MODES)) {
                pin.addSupprotedMode(Pin.Mode.resolve(i));
            }
            pins.add(pin.getIndex(), pin);
            if (pin.getSupportedModes().isEmpty()) {
                // if the pin has no supported modes, its initialization is already done
                initializedPins.incrementAndGet();
            } else {
                // if the pin supports some modes, we ask for its current mode and value
                try {
                    sendMessage(FirmataMessageFactory.pinStateRequest(pinId));
                    if (pinId > 0 && pinId % 14 == 0) { // 14 pins on Arduino UNO get initialized without delay
                        /* If the pin count is too high (i.e. Arduino Mega), then 
                        * too many firmata requests in a row can overflow the 
                        * device's serial input buffer. 
                        * One solution is to yield a little time between
                        * requests to allow the device to respond. The response 
                        * may then safely sit in the host's much larger serial 
                        * input buffer until it is dealt with by onPinStateReceive
                        */
                        Thread.sleep(10);
                    }
                } catch (IOException ex) {
                    LOGGER.error(String.format("Error requesting state of pin %d", pin.getIndex()), ex);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Delay between capability requests was interrupted", ex);
                }
            }
        }
    };

    /**
     * Describes reaction to the pin state data receiving.
     */
    private final Consumer<Event> onPinStateReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            byte pinId = (Byte) event.getBodyItem(PIN_ID);
            FirmataPin pin = pins.get(pinId);
            if (pin.getMode() == null) {
                pin.initMode(Pin.Mode.resolve((Byte) event.getBodyItem(PIN_MODE)));
                pin.initValue((Long) event.getBodyItem(PIN_VALUE));
            } else {
                pin.updateValue((Long) event.getBodyItem(PIN_VALUE));
            }
            if (initializedPins.incrementAndGet() == pins.size()) {
                try {
                    sendMessage(FirmataMessageFactory.ANALOG_MAPPING_REQUEST);
                } catch (IOException e) {
                    LOGGER.error("Error on request analog mapping", e);
                }
            }
        }
    };

    /**
     * Describes reaction to the analog mapping data receiving.
     */
    private final Consumer<Event> onAnalogMappingReceive = new Consumer<Event>() {
        @Override
        @SuppressWarnings("unchecked")
        public void accept(Event event) {
            analogMapping = (Map<Integer, Integer>) event.getBodyItem(ANALOG_MAPPING);
            try {
                sendMessage(FirmataMessageFactory.analogReport(true));
                sendMessage(FirmataMessageFactory.digitalReport(true));
            } catch (IOException ex) {
                LOGGER.error("Cannot enable reporting from device", ex);
            }
            ready.set(true);
            // all the pins are initialized so notification is sent to listeners
            IOEvent initIsDone = new IOEvent(FirmataDevice.this);
            for (IODeviceEventListener l : listeners) {
                l.onStart(initIsDone);
            }
        }
    };

    /**
     * Describes reaction to the analog message data receiving.
     */
    private final Consumer<Event> onAnalogMessageReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            int analogId = (Integer) event.getBodyItem(PIN_ID);
            if (analogMapping != null && analogMapping.get(analogId) != null) {
                int pinId = analogMapping.get(analogId);
                if (pinId < pins.size()) {
                    FirmataPin pin = pins.get(pinId);
                    if (Pin.Mode.ANALOG.equals(pin.getMode())) {
                        pin.updateValue((Integer) event.getBodyItem(PIN_VALUE));
                    }
                }
            }
        }
    };

    /**
     * Describes reaction to the digital message data receiving.
     */
    private final Consumer<Event> onDigitalMessageReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            int pinId = (Integer) event.getBodyItem(PIN_ID);
            if (pinId < pins.size()) {
                FirmataPin pin = pins.get(pinId);
                if (Pin.Mode.INPUT.equals(pin.getMode()) ||
                        Pin.Mode.PULLUP.equals(pin.getMode())) {
                    pin.updateValue((Integer) event.getBodyItem(PIN_VALUE));
                }
            }
        }
    };

    private final Consumer<Event> onI2cMessageReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            byte address = (Byte) event.getBodyItem(I2C_ADDRESS);
            int register = (Integer) event.getBodyItem(I2C_REGISTER);
            byte[] message = (byte[]) event.getBodyItem(I2C_MESSAGE);
            FirmataI2CDevice device = i2cDevices.get(address);
            if (device != null) {
                device.onReceive(register, message);
            }
        }
    };

    private final Consumer<Event> onStringMessageReceive = new Consumer<Event>() {
        @Override
        public void accept(Event event) {
            String message = (String) event.getBodyItem(STRING_MESSAGE);
            IOEvent evt = new IOEvent(FirmataDevice.this);
            for (IODeviceEventListener listener : listeners) {
                listener.onMessageReceive(evt, message);
            }
        }
    };
}
