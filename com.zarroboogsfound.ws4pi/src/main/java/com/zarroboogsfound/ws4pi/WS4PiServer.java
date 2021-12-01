package com.zarroboogsfound.ws4pi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.zarroboogsfound.ws4pi.devices.DeviceController;
import com.zarroboogsfound.ws4pi.devices.DeviceControllerProvider;
import com.zarroboogsfound.ws4pi.devices.ServoController;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class WS4PiServer implements DeviceControllerProvider {
	private Undertow undertowServer;
	private HttpRequestHandler httpRequestHandler;
	private ServoController servoController = new ServoController();
	private DeviceController[] devices = new DeviceController[] { servoController };

	public WS4PiServer(WS4PiConfig config) {
		System.setProperty("java.net.preferIPv4Stack" , "true");

		config.setDeviceProvider(this);
		
		try {
			servoController.initialize();
		} catch (UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		// build HTTP Request Handler
		httpRequestHandler = new HttpRequestHandler(config);
		
		// build Undertow server
        undertowServer = Undertow.builder()
                .addHttpListener(config.getHttpServerPort(), config.getHttpServerName())
                //.addHttpsListener(config.getHttpServerPort(), config.getHttpServerName(), config.getSslContext())
                .setHandler(new HttpHandler() {
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                    	httpRequestHandler.handleRequest(exchange);
                    }
                })
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .build();
	}
	
	public void start() {
        // let 'er rip!
        undertowServer.start();
	}
	
	public DeviceController getController(String name) {
		for (int i=0; i<devices.length; ++i) {
			if (devices[i].getName().equals(name))
				return devices[i];
		}
		return null;
	}
}
