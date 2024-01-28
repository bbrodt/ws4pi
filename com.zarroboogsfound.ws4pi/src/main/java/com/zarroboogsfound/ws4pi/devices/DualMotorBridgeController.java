package com.zarroboogsfound.ws4pi.devices;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.pi4j.component.Component;
import com.pi4j.component.motor.MotorState;
import com.pi4j.component.motor.SpeedVector;
import com.pi4j.component.motor.impl.GpioDualMotorBridgeComponent;
import com.pi4j.component.sensor.UltrasonicDistanceSensorComponent;
import com.pi4j.component.servo.Servo;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class DualMotorBridgeController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();
    Thread poller;
    
	private GpioDualMotorBridgeComponent bridges[];
	public DualMotorBridgeController() {
		super(DeviceType.MOTOR_BRIDGE);
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		GpioPin motor1pins[];
		GpioPin motor2pins[];

		bridges = new GpioDualMotorBridgeComponent[1];
		motor1pins = config.getGpioPins(DeviceType.MOTOR_BRIDGE, 0);
		motor2pins = config.getGpioPins(DeviceType.MOTOR_BRIDGE, 1);
		
		bridges[0] = new GpioDualMotorBridgeComponent(motor1pins, motor2pins);

		gpio.setShutdownOptions(true, PinState.LOW, motor1pins);
		gpio.setShutdownOptions(true, PinState.LOW, motor2pins);
	}

	@Override
	public void start(WS4PiConfig config) {
		UltrasoundController controller = (UltrasoundController)config.getDeviceProvider().getDeviceController(DeviceType.ULTRASOUND);
		final UltrasonicDistanceSensorComponent sensor = (UltrasonicDistanceSensorComponent)controller.getComponent(0);
		sensor.setDetectionRange(5, 15);
		sensor.setMeasurementTemperature(23);
		poller = new Thread() {

			@Override
			public void run() {
				SpeedVector sv = bridges[0].getSpeedVector();
				boolean forcedStop = false;
				while (true) {
					try {
						double d = sensor.measure();
						if (d>15.0) {
							for (int i=0; i<5; ++i) {
								d = sensor.measure();
								if (d<15.0)
									i = 0;
								Thread.sleep(100);
							}
							if (d>15.0) {
								SpeedVector reverse = new SpeedVector(
										(sv.direction+180) % 360, sv.magnitude);
								bridges[0].setSpeedVector(reverse);
								sleep(1000);
								bridges[0].setSpeedVector(new SpeedVector(0,0));
								forcedStop = true;
							}
						}
						else {
							if (forcedStop) {
								forcedStop = false;
								//bridges[0].setSpeedVector(sv);
							}
							sv = bridges[0].getSpeedVector();
						}
						sleep(100);
					}
					catch (InterruptedException e) {
						break;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		};
		poller.start();
		/*
		sensor.onObjectFound(new Callable() {
			@Override
			public Object call() throws Exception {
				System.out.println("Object found: "+sensor.measure());
				return null;
			}
		});
		sensor.onObjectLost(new Callable() {
			@Override
			public Object call() throws Exception {
				System.out.println("Object lost");
				bridges[0].setSpeedVector(new SpeedVector(0,0));
				return null;
			}
		});
		*/
	}

	public void stop() {
		if (poller!=null && poller.isAlive()) {
			poller.interrupt();
			poller = null;
			bridges[0].setSpeedVector(new SpeedVector(0,0));
		}
	}
	
	public boolean isBusy(int id) {
		return bridges[0].getState(0) == MotorState.STOP &&
				bridges[0].getState(1) == MotorState.STOP;
	}
	
	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		validate(id);
		return bridges[id].getSpeedVector();
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		float direction = QueryParams.getFloat(exchange, "direction").get().floatValue();
		float magnitude = QueryParams.getFloat(exchange, "magnitude").get().floatValue();
		validate(id, direction, magnitude);
		SpeedVector v = new SpeedVector(direction, magnitude);
		bridges[id].setSpeedVector(v);
		return v;
	}
    
    private void validate(int id, float direction, float magnitude) throws DeviceException {
    	super.validate(id);
        if (direction<0 || direction>360)
        	throw new DeviceException("Motor Direction "+direction+" invalid - must be in the range 0 to 360");
        if (magnitude<0f || magnitude>1f)
        	throw new DeviceException("Motor Magnitude "+magnitude+" invalid - must be in the range 0.0 to 1.0");
    }

	@Override
	public int getComponentCount() {
		if (bridges!=null)
			return bridges.length;
		return 0;
	}

	@Override
	public Component getComponent(int id) {
		if (bridges!=null && bridges.length>id)
			return bridges[id];
		return null;
	}
}