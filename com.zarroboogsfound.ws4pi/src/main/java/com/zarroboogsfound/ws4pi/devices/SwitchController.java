package com.zarroboogsfound.ws4pi.devices;

import java.util.ArrayList;
import java.util.List;

import com.pi4j.component.Component;
import com.pi4j.component.switches.impl.GpioSwitchComponent;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

// TODO: implement me

public class SwitchController extends DeviceController {

	private GpioSwitchComponent[] switches;
	
	public SwitchController() {
		super(DeviceType.SWITCH);
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
		List<GpioSwitchComponent> sw = new ArrayList<GpioSwitchComponent>();
		
		for (GpioPin pin : config.getGpioPins(DeviceType.SWITCH)) {
			sw.add( new GpioSwitchComponent((GpioPinDigitalInput) pin) );
		}
		switches = sw.toArray( new GpioSwitchComponent[sw.size()] );
	}

	@Override
	public void start(WS4PiConfig config) {
		
	}

	public boolean isBusy(int id) {
		return false;
	}
	
	@Override
	public int getComponentCount() {
		return switches==null? 0 : switches.length;
	}

	@Override
	public Component getComponent(int id) {
		if (switches!=null && switches.length>id)
			return switches[id];
		return null;
	}

}
