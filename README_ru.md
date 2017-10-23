[![Get help on Codementor](https://cdn.codementor.io/badges/get_help_github.svg)](https://www.codementor.io/olegkurbatov?utm_source=github&utm_medium=button&utm_term=olegkurbatov&utm_campaign=github)

## firmata4j
**firmata4j** - это клиентская библиотека протокола [Firmata](https://github.com/firmata/protocol)
написанная на Java. Она позволяет управлять Arduino (или другим устройством),
которое поддерживает взаимодействие по протоколу Firmata, из программы на языке
Java.

## Возможности
- Взаимодействие с устройством и с его портами в объектно-ориентированном стиле
- Позволяет абстрагироваться от деталей протокола
- Предоставляет графический компонент для визуализации и изменения режима и
состояния портов устройства
- Позволяет возаимодействовать с устройствами по протоколу I2C

## Установка

### Maven
Добавьте зависимость в `pom.xml` проекта:

```xml
<dependency>
    <groupId>com.github.kurbatov</groupId>
    <artifactId>firmata4j</artifactId>
    <version>2.3.5</version>
</dependency>
```

## Использование
Основной сценарий использования:
```java
IODevice device = new FirmataDevice("/dev/ttyUSB0"); // создать экземпляр устройства Firmata используя имя порта
// подписаться на события при помощи device.addEventListener(...);
// и/или device.getPin(n).addEventListener(...);
device.start(); // начать обмен сообщениями с устройствами
device.ensureInitializationIsDone(); // дождаться окончания инициализации
// здесь можно послать команды устройству
device.stop(); // остановить обмен сообщениями с устройством
```

Отправка команд устройству может привести к возникновению событий на устройстве.
Зарегестрированные обработчики событий получают оповещения о событиях асинхронно.
Обработчики могут быть добавлены и удалены в любое время.

Обработчик может быть зарегистрирован для получения событий устройства или его
портов.

```java
device.addEventListener(new IODeviceEventListener() {
    @Override
    public void onStart(IOEvent event) {
        // с этого момента можно быть уверенным, что устройство инициализировано
        // можно спрятать индикатор загрузки и начать делать интересные вещи
        System.out.println("Устройство готово");
    }

    @Override
    public void onStop(IOEvent event) {
        // с этого момента можно быть уверенным, что устройство было остановлено
        System.out.println("Устройство остановлено");
    }

    @Override
    public void onPinChange(IOEvent event) {
        // здесь мы реагируем на изменение состояния порта
        Pin pin = event.getPin();
        System.out.println(
                String.format(
                    "Порт %d перешёл в состояние %d",
                    pin.getIndex(),
                    pin.getValue())
            );
    }

    @Override
    public void onMessageReceive(IOEvent event, String message) {
        // здесь мы реагируем на получение текстовых сообщений от устройства
        System.out.println(message);
    }
});
```

Чтобы получить более детальный контроль, можно подписаться на обработку событий
одного конкретного порта.

```java
Pin pin = device.getPin(2);
pin.addEventListener(new PinEventListener() {
    @Override
    public void onModeChange(IOEvent event) {
        System.out.println("Изменился режим порта");
    }

    @Override
    public void onValueChange(IOEvent event) {
        System.out.println("Изменилось значение порта");
    }
});
```

Режим и значение порта можно изменить:

```java
pin.setMode(Pin.Mode.OUTPUT); // наши обработчики получат оповещение об этом изменении
pin.setValue(1); // ... а после и об этом изменении
```

Визуальное представление состояний портов устройства можно получить при помощи
компонента `JPinboard`.

```java
JPinboard pinboard = new JPinboard(device);
JFrame frame = new JFrame("Pinboard Example");
frame.add(pinboard);
frame.pack();
frame.setVisible(true);
```

`JPinboard` позволяет установить режим порта из контекстного меню. Состояние
порта вывода может быть изменено двойным щелчком на его изображении.

Пример использования `JPinboard` может быть найден в классе
[`org.firmata4j.Example`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/Example.java).

## I2C
**firmata4j** поддерживает взаимодействие с I2C устройствами. Получить ссылку на
I2C устройство можно следующим способом:

```java
IODevice device = new FirmataDevice(port);
...
byte i2cAddress = 0x3C;
I2CDevice i2cDevice = device.getI2CDevice(i2cAddress);
```

Во многих случаях удобнее написать обёртку для класса [`I2CDevice`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/I2CDevice.java)
под конкретное устройство, чтобы облегчить и сократить написание кода. Пример 
такого подхода продемонстрирован в классах 
[`SSD1306`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/ssd1306/SSD1306.java)
и
[`I2CExample`](https://github.com/kurbatov/firmata4j/blob/master/src/main/java/org/firmata4j/I2CExample.java).

## Версии
**firmata4j** придерживается версий протокола Firmata. Первая доступная версия
**firmata4j** - 2.3.1.

**firmata4j**-2.3.x будет работать с Fimata v. 2.3.x. На самом деле она
заработает и с Firmata v. 2.x.x, но не обязательно будет поддерживать все
функции, заявленые протоколом.
Первые номера в версиях должны обязательно совпадать, потому что они означают
значительные (несовместимые) изменения в протоколе.

## Загрузка Firmata на Arduino
Arduino IDE содержит реализацию протокола Firmata. Чтобы загрузить её на Arduino,
нужно сделать следущее:

- Подключить Arduino к компьютеру
- Запустить Arduino IDE
- Выбрать `File -> Examples -> Firmata -> StandardFirmata` в меню IDE
- Выбрать устройство в `Tools -> Board`
- Выбрать порт в `Tools -> Port` (уже выбрано, если вы ранее загружали что-либо на Arduino)
- Нажать кнопку `Upload`

## Примеры использования

- [Easy Peripherals for the Internet of Things](https://repositorio-aberto.up.pt/bitstream/10216/84433/2/138208.pdf)
- [Modelovanie a Riadenie Hybridných Systémov s Využitím Petriho Sietí Vyšších Úrovní](http://www.fei.stuba.sk/docs/2016/autoreferaty/autoref_Kucera.pdf)
- [Использование **firmata4j** в Closure](https://github.com/cowlike/firmata4j-samples-clojure)

## Развитие библиотеки
Помощь в развитии библиотеки всегда приветствуется. Если вы нашли ошибку или
хотите предложить идею новой функции, пожалуйста,
[создайте запрос](https://github.com/kurbatov/firmata4j/issues/new).

Если вы хотите улучшить библиотеку, пожалуйста, сделайте следующее:

1. Сделайте `fork` этого репозитория
2. Склонируйте свой `fork` на локальный компьютер (`git clone https://github.com/<your_username>/firmata4j.git`)
3. Создайте feature branch (`git checkout -b my-new-feature`)
4. Внесите необходимые изменения в код
5. Примените ваши изменения(`git commit -am 'Adds some feature'`)
6. Отправьте изменения на сервер (`git push origin my-new-feature`)
7. Создайте Pull Request

## Лицензия
**firmata4j** распространяется по лицензии MIT. Текст лицензии находится в файле
[LICENSE](https://github.com/kurbatov/firmata4j/blob/master/LICENSE).
