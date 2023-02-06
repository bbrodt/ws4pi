package com.zarroboogsfound.ws4pi.devices;

import java.io.FileNotFoundException;

import javax.naming.NameNotFoundException;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.macros.MacroConfig;
import com.zarroboogsfound.ws4pi.macros.MacroRunner;

import io.undertow.server.HttpServerExchange;

public class MacroController extends DeviceController {

	MacroConfig macroConfig;
	MacroRunner macroRunner;
	
	public MacroController() {
		super(DeviceType.MACRO);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		return macroConfig.macros;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		// load and parse macro file
		try {
			macroConfig = MacroConfig.load("config/macros.json");
			macroRunner = MacroRunner.getInstance();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(WS4PiConfig config) {
		macroRunner.start();
		runMacro("surprised");
	}

	public void stop() {
		macroRunner.stop();
	}
	
	public boolean isBusy(int id) {
		return macroRunner.isActive();
	}

	@Override
	public Component getComponent(int id) {
		return new NullComponent();
	}

	@Override
	public int getComponentCount() {
		return 1;
	}

	public void runMacro(String name) {
		try {
			macroRunner.run(name);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
