package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.pi4j.component.sensor.UltrasonicDistanceSensorComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinState;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class UltrasoundController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();
	UltrasonicDistanceSensorComponent sensors[] = new UltrasonicDistanceSensorComponent[2];
	
	public UltrasoundController() {
		super(DeviceType.ULTRASOUND);
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
        sensors[0] = new UltrasonicDistanceSensorComponent(pins[0], pins[1]);
        gpio.setShutdownOptions(true, PinState.LOW, pins);
	}

	@Override
	public void start(WS4PiConfig config) {
		
	}

	public boolean isBusy(int id) {
		return false;
	}
	
	@Override
	public int getComponentCount() {
		if (sensors!=null)
			return sensors.length;
		return 0;
	}

	@Override
	public Component getComponent(int id) {
		if (sensors!=null && sensors.length>id)
			return sensors[id];
		return null;
	}

}
