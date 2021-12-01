package com.zarroboogsfound.ws4pi;


import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.net.ssl.SSLContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zarroboogsfound.ws4pi.devices.DeviceControllerProvider;

public class WS4PiConfig {
	private String httpServerName;
	private int httpServerPort;
	private String keyStoreFile;
	private String password;
	private String guid;
	
	private DeviceControllerProvider deviceProvider;
	private SSLContext sslContext;
	
	public static WS4PiConfig load(String filename) throws FileNotFoundException {
		FileReader fr = new FileReader(filename);
		Gson gson = new GsonBuilder() .create();
		return gson.fromJson(fr, WS4PiConfig.class);
	}
	
	public String getHttpServerName() {
		return httpServerName;
	}
	
	public int getHttpServerPort() {
		return httpServerPort;
	}
	
	public String getHttpServerUrl() {
		return "http://" + getHttpServerName() + ":" + getHttpServerPort();
	}

	public String getGuid() {
		return guid;
	}
	
	public DeviceControllerProvider getDeviceProvider() {
		return deviceProvider;
	}
	
	public void setDeviceProvider(DeviceControllerProvider deviceProvider) {
		this.deviceProvider = deviceProvider;
	}

	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	public String getPassword() {
		return password;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}
}
