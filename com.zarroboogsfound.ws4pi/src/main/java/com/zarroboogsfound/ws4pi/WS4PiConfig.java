package com.zarroboogsfound.ws4pi;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.zarroboogsfound.ws4pi.devices.DeviceController;
import com.zarroboogsfound.ws4pi.devices.DeviceControllerProvider;

public class WS4PiConfig {
	private String httpServerName;
	private int httpServerPort;
	private String keyStoreFile;
	private String password;
	private String guid;
	
	private DeviceControllerProvider deviceProvider;
	private SSLContext sslContext;
	private List<Device> devices;

    private final GpioController gpio = GpioFactory.getInstance();
    
    private static WS4PiConfig instance;

	public class Device {
		public class PinDefinition {
			// Pin name
			public String name;
			// type, direction and state information for provisioning
			public String provision;
			// the pin's WiringPi address
			public int address;
			// the provisioned pin defined by the Gpio class:
			public GpioPin pin;
		}
		// The following two are only used for servos and steppers
		public class Limits {
			public float startPos; // ServoController will move each servo to this position in DeviceController.start() 
			public float minPos;
			public float maxPos;
			public float maxTorque; // measured in Kg-cm
			public float speed; // measured in # of milliseconds to move through its entire range (-100 to 100)
			public boolean reversed;
		}
		public class Lag {
			public float position;
			public float time;
		}
		
		public String type;
		public String name;
		// if device type==SERVO, this is the servo controller board channel number
		// (connector) to which the servo is connected. Otherwise, this is intended
		// to uniquely identify a specific device in the set of each DeviceType.
		public int id;
		public List<PinDefinition> pins = new ArrayList<PinDefinition>() ;
		public Limits limits = new Limits();
		public List<Lag> lags = new ArrayList<Lag>();
		public List<Device> supportingDevices = new ArrayList<Device>();
	}
	
	private WS4PiConfig() {
		instance = this;
	}
	
	public static WS4PiConfig getInstance() {
		if (instance==null)
			instance = new WS4PiConfig();
		return instance;
	}
	
	public static WS4PiConfig load(String filename) throws FileNotFoundException {
		FileReader fr = new FileReader(filename);
		Gson gson = new GsonBuilder() .create();
		instance = gson.fromJson(fr, WS4PiConfig.class);
		return instance;
	}
	
	public GpioPin[] getGpioPins(DeviceType deviceType) {
		return getGpioPins(deviceType, -1);
	}
	
	public GpioPin[] getGpioPins(DeviceType deviceType, int id) {
		if (devices==null) {
			throw new ExceptionInInitializerError("Configuration file is not loaded yet");
		}
		List<GpioPin> allPins = new ArrayList<GpioPin>();
		for (Device d : devices) {
			List<GpioPin> pins = getGpioPins(deviceType, id, d);
			if (pins!=null)
				allPins.addAll(pins);
		}
		return allPins.toArray(new GpioPin[allPins.size()]);
	}
	
	private List<GpioPin> getGpioPins(DeviceType deviceType, int id, Device device) {
		List<GpioPin> pins = new ArrayList<GpioPin>();
		if (deviceType.name().equalsIgnoreCase(device.type)) {
			if (id<0 || device.id==id) {
				for (Device.PinDefinition p : device.pins)
					pins.add(p.pin);
				return pins;
			}
		}
		if (device.supportingDevices!=null) {
			for (Device d : device.supportingDevices) {
				pins = getGpioPins(deviceType, id, d);
				if (pins != null)
					return pins;
			}
		}
		return null; 
	}
	
	public List<Device> provisionDevicePins() throws Exception {
		for (Device d : devices) {
			// servos don't use any pins - they are connected to the I2C interface
			if (!DeviceType.SERVO.name().equalsIgnoreCase(d.type)) {
				System.out.println("Provisioning pins for '"+d.name+"':");
				provisionDevicePins(d);
			}
		}
		return devices;
	}
	
	public Device getDevice(DeviceType type, String name) {
		for (Device d : devices) {
			if (type.name().equalsIgnoreCase(d.type) && d.name.equalsIgnoreCase(name))
				return d;
		}
		return null;
	}
	
	public Device[] getDevices(DeviceType type) {
		List<Device> results = new ArrayList<Device>();
		
		for (Device d : devices) {
			if (type.name().equalsIgnoreCase(d.type))
				results.add(d);
		}
		return results.toArray(new Device[results.size()]);
	}
	
	private void provisionDevicePins(Device device) throws Exception {
		for (Device.PinDefinition p : device.pins) {
			p.pin = provisionPin(device, p);
		}
		if (device.supportingDevices!=null) {
			for (Device d : device.supportingDevices) {
				provisionDevicePins(d);
			}
		}
	}
	
	private GpioPin provisionPin(Device device, Device.PinDefinition pinDef) throws Exception {
		Pin pin = RaspiPin.getPinByAddress(pinDef.address);
		GpioPin provisionedPin = null;
		DeviceController controller = deviceProvider.getDeviceController(DeviceType.valueOf(device.type.toUpperCase()));
		GpioProvider gpioProvider = controller.getGpioProvider();
		
		String[] p = pinDef.provision.split(" ");
		if (p.length!=3) {
			throw new IllegalArgumentException("Invalid provisioning parameter '"+pinDef.provision+"' in Configuration File");
		}
		if (p[0].equalsIgnoreCase("digital")) {
			if (p[1].equalsIgnoreCase("input")) {
				if (p[2].equalsIgnoreCase("pullup"))
					provisionedPin = gpio.provisionDigitalInputPin(gpioProvider, pin, PinPullResistance.PULL_UP);
				else if (p[2].equalsIgnoreCase("pulldown"))
					provisionedPin = gpio.provisionDigitalInputPin(gpioProvider, pin, PinPullResistance.PULL_DOWN);
				else
					throw new IllegalArgumentException("Invalid provisioning parameter '"+pinDef.provision+"' in Configuration File");
			}
			else if (p[1].equalsIgnoreCase("output")) {
				if (p[2].equalsIgnoreCase("low")) {
					provisionedPin = gpio.provisionDigitalOutputPin(gpioProvider, pin, PinState.LOW);
				}
				else if (p[2].equalsIgnoreCase("high"))
					provisionedPin = gpio.provisionDigitalOutputPin(gpioProvider, pin, PinState.HIGH);
				else
					throw new IllegalArgumentException("Invalid provisioning parameter '"+pinDef.provision+"' in Configuration File");
			}
			else if (p[1].equalsIgnoreCase("softpwm")) {
				provisionedPin = gpio.provisionSoftPwmOutputPin(gpioProvider, pin);
			}
		}
		else if (p[0].equals("analog")) {
			throw new IllegalArgumentException("Provisioning '"+pinDef.provision+"' not implemented in Configuration File"); 
		}
		else
			throw new IllegalArgumentException("Invalid provisioning parameter '"+pinDef.provision+"' in Configuration File");

		if (provisionedPin!=null)
			System.out.println("    "+provisionedPin.getName()+" for "+pinDef.name+" as "+pinDef.provision);
		return provisionedPin;
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
