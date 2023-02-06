package com.zarroboogsfound.ws4pi;

import com.zarroboogsfound.ws4pi.devices.DeviceController;

public class ShutdownHook extends Thread {

	WS4PiConfig config;
	
	public ShutdownHook(WS4PiConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		DeviceController dcs[] = config.getDeviceProvider().getAllDeviceControllers();
		for (DeviceController dc : dcs) {
			dc.stop();
		}
		for (DeviceController dc : dcs) {
			dc.shutdown();
		}
	}
}
