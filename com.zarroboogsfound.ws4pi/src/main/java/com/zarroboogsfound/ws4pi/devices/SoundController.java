package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class SoundController extends DeviceController {

	public SoundController() {
		super(DeviceType.SOUND);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(WS4PiConfig config) {
		// TODO Auto-generated method stub

	}

	public boolean isBusy(int id) {
		return false;
	}

	@Override
	public Component getComponent(int id) {
		return new NullComponent();
	}

	@Override
	public int getComponentCount() {
		// TODO Auto-generated method stub
		return 1;
	}

}
