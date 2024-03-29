package com.zarroboogsfound.ws4pi;

import com.zarroboogsfound.ws4pi.WS4PiConfig.Device;
import com.zarroboogsfound.ws4pi.devices.DeviceController;
import com.zarroboogsfound.ws4pi.devices.ExecController;

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
		/**********************************************
		 * Workaround for broken native code gpio unexport: spawn the gpio program to do the unexport
		 * instead of doing it in C code native library
		 */
		Device[] devices = config.getDevices();
		for (Device d : devices) {
			for (Device.PinDefinition p : d.pins) {
				ExecController.publicExec("gpio unexport "+p.gpio, true);
			}
		}
	}
}
