package com.zarroboogsfound.ws4pi.devices;

import com.zarroboogsfound.ws4pi.DeviceType;

public interface DeviceControllerProvider {

	DeviceController getDeviceController(DeviceType type);
	DeviceController[] getAllDeviceControllers();
}
