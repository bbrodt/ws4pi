package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.pi4j.component.light.impl.GpioLEDComponent;
import com.pi4j.component.sensor.UltrasonicDistanceSensorComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class LEDController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();
	GpioLEDComponent leds[] = new GpioLEDComponent[1];

	public LEDController() {
		super(DeviceType.LED);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		return null;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
        GpioPin[] pins;
        pins = config.getGpioPins(DeviceType.ULTRASOUND, 0);
        leds[0] = new GpioLEDComponent((GpioPinDigitalOutput)pins[0]);
        gpio.setShutdownOptions(true, PinState.LOW, pins);
	}

	@Override
	public void start(WS4PiConfig config) {
		// TODO Auto-generated method stub

	}
    public void on(int id) {
    	leds[id].on();
    }

    public void off(int id) {
        leds[id].off();
    }

	@Override
	public Component getComponent(int id) {
		return leds[id];
	}

	@Override
	public int getComponentCount() {
		return 1;
	}

	@Override
	public boolean isBusy(int id) {
		return false;
	}

}
