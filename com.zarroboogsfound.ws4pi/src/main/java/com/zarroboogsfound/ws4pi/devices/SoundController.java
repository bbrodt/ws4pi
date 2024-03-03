package com.zarroboogsfound.ws4pi.devices;

import java.io.IOException;

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
		play("sounds/startup.wav");
	}
	
	@Override
	public void stop() {
		play("sounds/shutdown.wav");
	}
	
	public void play(String filepath) {
		try {
			Runtime.getRuntime().exec("mplayer "+filepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		return 1;
	}

}
