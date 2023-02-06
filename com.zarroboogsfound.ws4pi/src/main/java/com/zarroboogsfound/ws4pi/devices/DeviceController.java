package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public abstract class DeviceController {
	protected DeviceType type;
	
	public DeviceController(DeviceType type) {
		this.type = type;
	}
	
	public DeviceType getDeviceType() {
		return type;
	}
	
	public void validate(int id) throws DeviceException {
		if (id<0 || id>=getComponentCount())
			throw new DeviceException("Device ID "+id+" invalid - must be in the range 0 to "+(getComponentCount()-1));
	}
	public GpioProvider getGpioProvider() throws Exception {
		return GpioFactory.getDefaultProvider();
	}
	
	public abstract Object handleGetOperation(HttpServerExchange exchange) throws DeviceException;
	public abstract Object handleSetOperation(HttpServerExchange exchange) throws DeviceException;
	public abstract void initialize(WS4PiConfig config) throws Exception;
	public abstract void start(WS4PiConfig config); // called after all controllers have been initialized
	public void stop() {
	}
	public void shutdown() {
	}
	public abstract Component getComponent(int id);
	public abstract int getComponentCount();
	public abstract boolean isBusy(int id);
}
