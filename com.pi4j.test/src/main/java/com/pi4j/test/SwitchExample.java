package com.pi4j.test;

import com.pi4j.component.switches.SwitchListener;
import com.pi4j.component.switches.SwitchStateChangeEvent;
import com.pi4j.component.switches.impl.GpioSwitchComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

public class SwitchExample {

	public static void main(String[] args) throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();

        GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_UP);
		GpioSwitchComponent sw = new GpioSwitchComponent(pin);
		sw.addListener(new SwitchListener() {

			@Override
			public void onStateChange(SwitchStateChangeEvent event) {
				System.out.println("State changed from "+event.getOldState()+" to "+event.getNewState());
			}
			
		});
		for (int i=0; i<10000; ++i) {
			Thread.sleep(500);
		}
	}

}
