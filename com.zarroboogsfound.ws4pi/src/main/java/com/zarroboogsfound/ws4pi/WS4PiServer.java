package com.zarroboogsfound.ws4pi;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.zarroboogsfound.ws4pi.devices.DelayController;
import com.zarroboogsfound.ws4pi.devices.DeviceController;
import com.zarroboogsfound.ws4pi.devices.DeviceControllerProvider;
import com.zarroboogsfound.ws4pi.devices.DualMotorBridgeController;
import com.zarroboogsfound.ws4pi.devices.ExecProcController;
import com.zarroboogsfound.ws4pi.devices.LEDController;
import com.zarroboogsfound.ws4pi.devices.MacroController;
import com.zarroboogsfound.ws4pi.devices.ServoController;
import com.zarroboogsfound.ws4pi.devices.SoundController;
import com.zarroboogsfound.ws4pi.devices.UltrasoundController;
import com.zarroboogsfound.ws4pi.devices.StepperMotorController;
import com.zarroboogsfound.ws4pi.devices.SwitchController;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.examples.websockets.WebSocketServer;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class WS4PiServer implements DeviceControllerProvider {
	private Undertow undertowServer;
	private HttpRequestHandler httpRequestHandler;
	private ServoController servoController = new ServoController();
	private DualMotorBridgeController motoBridgeController = new DualMotorBridgeController();
	private StepperMotorController stepperController = new StepperMotorController();
	private SwitchController switchController = new SwitchController();
	private UltrasoundController ultrasoundController = new UltrasoundController();
	private MacroController macroController = new MacroController();
	private SoundController soundController = new SoundController();
	private ExecProcController execProcController = new ExecProcController();
	private DelayController delayController = new DelayController();
	private LEDController ledController = new LEDController();
	private DeviceController[] deviceControllers = new DeviceController[] {
			servoController,
			motoBridgeController,
			stepperController,
			switchController,
			ultrasoundController,
			macroController,
			soundController,
			execProcController,
			delayController,
			ledController
	};
	private Thread runner;

	public WS4PiServer(WS4PiConfig config) {
		System.setProperty("java.net.preferIPv4Stack" , "true");

		config.setDeviceProvider(this);

		try {
			config.provisionDevicePins();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// initialize all device controllers
		for (DeviceController dc : deviceControllers) {
			try {
				dc.initialize(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// start all device controllers - this allows the controllers to
		// perform additional configuration that depends on other device
		// controllers having been initialized.
		for (DeviceController dc : deviceControllers) {
			try {
				dc.start(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// build HTTP Request Handler
		httpRequestHandler = new HttpRequestHandler(config);
		
		// build Undertow server
        undertowServer = Undertow.builder()
                .addHttpListener(config.getHttpServerPort(), config.getHttpServerName())
                //.addHttpsListener(config.getHttpServerPort(), config.getHttpServerName(), config.getSslContext())
                .setHandler(new HttpHandler() {
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                    	// Change response headers to allow cross-origin resource sharing (CORS)
                    	exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
                    	exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
                    	exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "*");
                    	exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "Content-Type");
                    	exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    	httpRequestHandler.handleRequest(exchange);
                    }
                })
                /*
                .addHttpListener(config.getHttpServerPort()-1, config.getHttpServerName())
                .setHandler(path()
                        .addPrefixPath("/myapp", websocket(new WebSocketConnectionCallback() {

                            @Override
                            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                                    @Override
                                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                                        WebSockets.sendText(message.getData(), channel, null);
                                    }
                                });
                                channel.resumeReceives();
                            }
                        }))
                        .addPrefixPath("/", resource(new ClassPathResourceManager(WebSocketServer.class.getClassLoader(), WebSocketServer.class.getPackage())).addWelcomeFiles("index.html")))
                */
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .build();
	}
	
	public void start() {
        // let 'er rip!
		runner = new Thread() {

			@Override
			public void run() {
		        undertowServer.start();
			}
		};
		runner.start();
	}
	
	public boolean isRunning() {
		return runner!=null && runner.isAlive();
	}
	
	public DeviceController getDeviceController(DeviceType type) {
		for (int i=0; i<deviceControllers.length; ++i) {
			if (deviceControllers[i].getDeviceType().equals(type))
				return deviceControllers[i];
		}
		return null;
	}
	
	public DeviceController[] getAllDeviceControllers() {
		return Arrays.copyOf(deviceControllers, deviceControllers.length);
	}
}
