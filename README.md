[![Get help on Codementor](https://cdn.codementor.io/badges/get_help_github.svg)](https://www.codementor.io/olegkurbatov?utm_source=github&utm_medium=button&utm_term=olegkurbatov&utm_campaign=github)

## firmata4j
**firmata4j** is a client library of [Firmata](https://github.com/firmata/protocol)
written in Java. The library allows controlling Arduino (or another board) which
runs Firmata protocol from your java program.

## Capabilities
- Interaction with a board and its pins in object-oriented style
- Communication over serial port, network or custom transport layer
- Abstraction over details of the protocol
- Provides an UI component that visualize the current state of every pin and
allows changing their mode and state
- Allows communicating with I2C devices

## Installation

### Maven
Add the following dependency to `pom.xml` of your project:

```xml
<dependency>
    <groupId>com.github.kurbatov</groupId>
    <artifactId>firmata4j</artifactId>
    <version>2.3.8</version>
</dependency>
```

## Usage
General scenario of usage is following:
```java
// construct a Firmata device instance
IODevice device = new FirmataDevice("/dev/ttyUSB0"); // using the name of a port
// IODevice device = new FirmataDevice(new NetworkTransport("192.168.1.18:4334")); // using a network address
// subscribe to events using device.addEventListener(...);
// and/or device.getPin(n).addEventListener(...);
device.start(); // initiate communication to the device
device.ensureInitializationIsDone(); // wait for initialization is done
// sending commands to the board
device.stop(); // stop communication to the device
```

Sending commands to the board may cause the device to emit events.
Registered listeners process the events asynchronously. You can add and remove
listeners along the way.

You can subscribe to events of the device or its pin.

```java
device.addEventListener(new IODeviceEventListener() {
    @Override
    public void onStart(IOEvent event) {
        // since this moment we are sure that the device is initialized
        // so we can hide initialization spinners and begin doing cool stuff
        System.out.println("Device is ready");
    }

    @Override
    public void onStop(IOEvent event) {
        // since this moment we are sure that the device is properly shut down
        System.out.println("Device has been stopped");
    }

    @Override
    public void onPinChange(IOEvent event) {
        // here we react to changes of pins' state
        Pin pin = event.getPin();
        System.out.println(
                String.format(
                    "Pin %d got a value of %d",
                    pin.getIndex(),
                    pin.getValue())
            );
    }

    @Override
    public void onMessageReceive(IOEvent event, String message) {
        // here we react to receiving a text message from the device
        System.out.println(message);
    }
});
```

To obtain more fine grained control you can subscribe to events of a particular
pin.

```java
Pin pin = device.getPin(2);
pin.addEventListener(new PinEventListener() {
    @Override
    public void onModeChange(IOEvent event) {
        System.out.println("Mode of the pin has been changed");
    }

    @Override
    public void onValueChange(IOEvent event) {
        System.out.println("Value of the pin has been changed");
    }
});
```

You can change the mode and value of a pin:

```java
pin.setMode(Pin.Mode.OUTPUT); // our listeners will get event about this change
pin.setValue(1); // and then about this change
```

## I2C
**firmata4j** supports working with I2C devices. You can obtain a reference to
an I2C device in this way:

```java
IODevice device = new FirmataDevice(port);
...
byte i2cAddress = 0x3C;
I2CDevice i2cDevice = device.getI2CDevice(i2cAddress);
```

You may find convenient writing a wrapper for [`I2CDevice` class](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/I2CDevice.java)
to facilitate communication with I2C device. Consider [`SSD1306`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/ssd1306/SSD1306.java)
and [`I2CExample`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/I2CExample.java)
classes as an example of that approach.

## Low-Level Messages and Events

**firmata4j** allows sending an arbitrary binary message to the device. For
example, setting sampling intervall using a low-level message:

```java
device.sendMessage(FirmataMessageFactory.setSamplingInterval(12));
```

Low-level event handlers are supported as well. Those may be useful for
debugging or processing custom messages from a device with modified protocol
implementation.

```java
device.addProtocolMessageHandler(FirmataEventType.SYSEX_CUSTOM_MESSAGE, new Consumer<Event>() {
    @Override
    public void accept(Event evt) {
        byte[] message = (byte[]) evt.getBodyItem(FirmataEventType.SYSEX_CUSTOM_MESSAGE);
        byte messageType = message[0];
        // and so on
    }
});
```

## Watchdog

Low-level event handlers allow regestering a watchdog:

```java
IODevice device = new FirmataDevice(port);
//...
FirmataWatchdog watchdog = new FirmataWatchdog(3000, new Runnable() {
    @Override
    public void run() {
        // do something when there were no low-level events during 3000 milliseconds
    }
});
device.addProtocolMessageHandler(FirmataEventType.ANY, watchdog);
//...
device.start();
```

This watchdog implementation gets activated by the first received message since
it subscribed. That's why it should be registered before communication starts.

## Visualization

You can get visual representation of device's pins using `JPinboard` Swing component.

```java
JPinboard pinboard = new JPinboard(device);
JFrame frame = new JFrame("Pinboard Example");
frame.add(pinboard);
frame.pack();
frame.setVisible(true);
```

`JPinboard` allows setting the pin's mode by choosing one from a context menu of
the pin. State of the output pin can be changed by double clicking on it.

An example of `JPinboard` usage can be found in
[`org.firmata4j.Example` class](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/Example.java).

## Versions
**firmata4j** sticks to Firmata protocol versions. The first available version
of **firmata4j** is 2.3.1.

**firmata4j**-2.3.x will work well with Fimata v. 2.3.x. Actually it should work
with Firmata v. 2.x.x but not necessarily support all of the protocol features.
The first digits of versions must be equal because those stand for incompatible
changes of the protocol.

## Uploading Firmata To Arduino
Arduino IDE is shipped with an implementation of Firmata protocol. You can
upload it as follows:

- Plug your Arduino to the computer
- Launch Arduino IDE
- Select `File -> Examples -> Firmata -> StandardFirmata` in IDE's menu
- Select your board in `Tools -> Board`
- Select the port in `Tools -> Port` (it is already selected if you have uploaded something to your Arduino)
- Click on `Upload` button

Note that **firmata4j** is focused to be client for the `StandardFirmata` firmware.
Although there are several other firmwares that support Firmata protocol, those
may implement only a featured subset of the protocol. A firmware has to respond
to the following requests in order for **firmata4j** to initialize properly:

- `REPORT_FIRMWARE`
- `CAPABILITY_QUERY`
- `PIN_STATE_QUERY`
- `ANALOG_MAPPING_QUERY`

## Cases

- [Easy Peripherals for the Internet of Things](https://repositorio-aberto.up.pt/bitstream/10216/84433/2/138208.pdf)
- [Modelovanie a Riadenie Hybridných Systémov s Využitím Petriho Sietí Vyšších Úrovní](http://www.fei.stuba.sk/docs/2016/autoreferaty/autoref_Kucera.pdf)
- [Programmazione di Sistemi Embedded con Linguaggi ad Agenti: un Caso di Studio basato su Jason e Arduino](https://amslaurea.unibo.it/9188/1/cozzolino_francesco_tesi.pdf)
- [Using **firmata4j** in Clojure](https://github.com/cowlike/firmata4j-samples-clojure)

## Contributing
Contributions are welcome. If you discover a bug or would like to propose a new
feature, please, [open a new issue](https://github.com/kurbatov/firmata4j/issues/new).

If you have an improvement to share, please, do the following:

1. Fork this repository
2. Clone your own fork to your machine (`git clone https://github.com/<your_username>/firmata4j.git`)
3. Create a feature branch (`git checkout -b my-new-feature`)
4. Change the code
5. Commit the changes (`git commit -am 'Adds some feature'`)
6. Push to the branch (`git push origin my-new-feature`)
7. Create new Pull Request

## License
**firmata4j** is distributed under the terms of the MIT License. See the
[LICENSE](https://github.com/kurbatov/firmata4j/blob/master/LICENSE) file.
