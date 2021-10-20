# ws4pi
Web Services for Raspberry Pi

WS4PI exposes the Raspberry Pi's GPIO circuits as a Web Service. It uses [JBoss Unterow](https://undertow.io/)
as the (embedded) web server and provides both HTTP and Web Socket interfaces.

This project is designed to work with the [pi4j API](https://pi4j.com/]pi4j).
This initial release will be based on pi4j version 1.3; this means it relies on Java 8 and still offers support
for the pi4j-device and pi4j-gpio libraries, which have been removed from the pi4j project in later versions.
