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
import com.zarroboogsfound.ws4pi.WS4PiConfig.Device;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class LEDController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();
	GpioLEDComponent[] leds;
    private WS4PiConfig config;

	public LEDController() {
		super(DeviceType.LED);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		validate(id);
		return leds[id].isOn() ? "true" : "false";
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		boolean value = QueryParams.getBool(exchange, "set").get();
		validate(id);
		if (value)
			on(id);
		else
			off(id);
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		this.config = config;
        Device[] devices = getDevices();
        leds = new GpioLEDComponent[devices.length];
        for (int id=0; id<devices.length; ++id) {
        	GpioPinDigitalOutput pin = (GpioPinDigitalOutput)devices[id].pins.get(0).pin;
            leds[id] = new GpioLEDComponent(pin);
            gpio.setShutdownOptions(true, PinState.LOW, pin);
        }
	}

	@Override
	public void start(WS4PiConfig config) {
		// TODO Auto-generated method stub

	}

    private Device[] getDevices() {
    	return config.getDevices(DeviceType.LED);
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
