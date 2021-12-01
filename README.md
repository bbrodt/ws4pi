# ws4pi
Web Services for Raspberry Pi

WS4PI exposes the Raspberry Pi's GPIO circuits as a Web Service. It uses [JBoss Unterow](https://undertow.io/)
as the (embedded) web server and provides both HTTP and Web Socket interfaces.

This project is designed to work with the [pi4j API](https://pi4j.com/]pi4j).
This initial release will be based on a [fork of pi4j version 1.4](https://github.com/bbrodt/pi4j-v1)
which includes the component & device classes from version 1.3 built with Java 11.
Note that the component & device classes were removed when the pi4j project moved from v1.3 to v1.4

Currently, only one device is supported, namely the [PCA9685 PWM controller board](https://learn.adafruit.com/16-channel-pwm-servo-driver?view=all).
While this device is useful for any kind of PWM (Pulse Width Modulation) application, it is currently configured as a servo driver.

The WS4Pi service endpoint is invoked using a HTTP POST method like so:

```
  http://\<raspberrypi-hostname\>:8081/servo?channel=\<c\>&position=\<p\>
```

This will move the servo on channel "c" (in the range 0 to 15) to position "p" (in the range -100 to 100).
The service returns a JSON string, which contains the new servo position and error status, for example:

```  
  {
    "type": "java.lang.Float",
    "object": 7.0,
    "status": "OK"
  }
```

When invoked with an HTTP GET, the current servo position is returned, as above.
  
As the project progresses, I will include support for additional devices.
