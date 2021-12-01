package com.zarroboogsfound.ws4pi.devices;

import io.undertow.server.HttpServerExchange;

public abstract class DeviceController {
	protected String name;
	
	public DeviceController(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract Object handleGetOperation(HttpServerExchange exchange);
	public abstract Object handleSetOperation(HttpServerExchange exchange);
	public abstract void initialize() throws Exception;
}
