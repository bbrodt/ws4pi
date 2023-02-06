package com.pi4j.test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.pi4j.component.sensor.UltrasonicDistanceSensorComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Example Application of using the Crow Pi Ultrasonic Distance Sensor.
 */
public class UltrasonicDistanceSensorExample {

	public static void main(String[] args) throws InterruptedException, TimeoutException {
		// Create new tilt sensor component
        final GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalOutput trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
		GpioPinDigitalInput echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03);
		final UltrasonicDistanceSensorComponent distanceSensor = new UltrasonicDistanceSensorComponent(trigger, echo);

		// Configures the Sensor to find Object passing in a predefined distance
		System.out.println("Searching for an object now...");
		distanceSensor.setDetectionRange(5, 15);
		distanceSensor.setMeasurementTemperature(23);
		distanceSensor.onObjectFound(
				new Callable() {
					public Object call() throws Exception {
						System.out.println("Sensor has found an Object in Range at "+distanceSensor.measure()+" cm");
						return null;
					}
				}
		);
		distanceSensor.onObjectLost(
				new Callable() {
					public Object call() throws Exception {
						System.out.println("Sensor has lost the Object in Range!");
						return null;
					}
				}
		);

		// Clean up event handlers
		System.out.println("Searching completed.");
		distanceSensor.onObjectFound(null);
		distanceSensor.onObjectLost(null);

		// Just printing some text to the users
		System.out.println("Let's find out the impact of temperature to ultrasonic measurements!");

		// Start a measurement with a temperature compensation like it is -10°C while
		// measuring.
		double measurementCold = distanceSensor.measure(-10);
		System.out.println("If you room has -10°C now we measure: " + measurementCold + " cm");

		// Start a measurement with a temperature compensation like it is 30°C while
		// measuring.
		double measurementHot = distanceSensor.measure(30);
		System.out.println("If you room has 30°C now we measure: " + measurementHot + " cm");
		System.out.format(
				"That's a difference of %.2f %%. Only caused by the difference of sonics. Physic is " + "crazy\n",
				(measurementHot - measurementCold) / measurementCold * 100);

		System.out.println("Lets now just measure for 10 Seconds. That gives some time to try the sensor a little.");

		// Loop 10 times through the measurement. Print the result to the user
		for (int i = 0; i < 10; i++) {
			// Measures the current distance without temperature compensation and prints it
			// to the user.
			try {
				System.out.println("Measured distance is: " + distanceSensor.measure() + " cm");
			} catch (TimeoutException e) {
				// If the measurement fails with a MeasurementException, we inform the user and
				// try again next time
				System.out.println("Oh no. Measurement failed... lets try again");
			}

			// Delay the measurements a little. This gives you some time to move in front of
			// the sensor.
			Thread.sleep(1000);
		}
	}
}