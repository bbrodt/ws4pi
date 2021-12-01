package com.zarroboogsfound.ws4pi.devices;

public interface DeviceControllerProvider {

	DeviceController getController(String name);
}
