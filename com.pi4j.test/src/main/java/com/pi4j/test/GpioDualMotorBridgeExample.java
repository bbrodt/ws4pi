package com.pi4j.test;
import java.io.IOException;
import java.util.Scanner;

import com.pi4j.component.motor.SpeedVector;
import com.pi4j.component.motor.impl.GpioDualMotorBridgeComponent;
import com.pi4j.gpio.extension.misc.L298HGpioProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


public class GpioDualMotorBridgeExample {
	private L298HGpioProvider gpioProvider;
    final GpioController gpio = GpioFactory.getInstance();
    GpioDualMotorBridgeComponent bridge;
	GpioPin motor1pins[] = new GpioPinDigitalOutput[3];
	GpioPin motor2pins[] = new GpioPinDigitalOutput[3];

	public static void main(String args[]) throws Exception {
		GpioDualMotorBridgeExample example = new GpioDualMotorBridgeExample();
		example.run();
	}
	
	public GpioDualMotorBridgeExample() throws Exception {
		gpioProvider = createProvider();

        // Define outputs in use for this example
		motor1pins[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.LOW); // BCM 24
		motor1pins[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW); // BCM 23
		motor1pins[2] = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_06); // BCM 25
		motor2pins[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, PinState.LOW); // BCM 14
		motor2pins[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, PinState.LOW); // BCM 15
		motor2pins[2] = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_00); // BCM 17
		
		// create dual motor bridge component
		bridge = new GpioDualMotorBridgeComponent(motor1pins, motor2pins);
	}
	
	public void run() {
		Scanner in = new Scanner(System.in);
		System.out.println("Forward");
		forward();
		in.nextLine();
		System.out.println("Backward");
		backward();
		in.nextLine();
		System.out.println("turn Left 30");
		turnLeft(30);
		in.nextLine();
		System.out.println("Turn Left 45");
		turnLeft(45);
		in.nextLine();
		System.out.println("Turn Left 90");
		turnLeft(90);
		in.nextLine();
		System.out.println("Turn Left 120");
		turnLeft(120);
		in.nextLine();
		System.out.println("Turn Right 30");
		turnRight(30);
		in.nextLine();
		System.out.println("Turn Right 45");
		turnRight(45);
		in.nextLine();
		System.out.println("Turn Right 90");
		turnRight(90);
		in.nextLine();
		System.out.println("Turn Right 120");
		turnRight(120);
		in.nextLine();
		System.out.println("Stop");
		stop();

		// stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
	}
	
	private void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
		}
	}
	
	private float magnitude = 1f;
	public void forward() {
		SpeedVector v = new SpeedVector(0f, magnitude);
		bridge.setSpeedVector(v);
	}
	
	public void backward() {
		SpeedVector v = new SpeedVector(180f, magnitude);
		bridge.setSpeedVector(v);
	}
	
	public void turnLeft(int degrees) {
		SpeedVector v = new SpeedVector((float)(360-degrees), magnitude);
		bridge.setSpeedVector(v);
	}
	
	public void turnRight(int degrees) {
		SpeedVector v = new SpeedVector((float)degrees, magnitude);
		bridge.setSpeedVector(v);
	}
	
	public void stop() {
		SpeedVector v = new SpeedVector(0f, 0f);
		bridge.setSpeedVector(v);
	}

	public L298HGpioProvider createProvider() throws IOException, UnsupportedBusNumberException {
    	return new L298HGpioProvider();
    }
}
