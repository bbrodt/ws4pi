package com.pi4j.test;


/*
* #%L
* **********************************************************************
* ORGANIZATION  :  Pi4J
* PROJECT       :  Pi4J :: Java Examples
* FILENAME      :  TriggerGpioExample.java
*
* This file is part of the Pi4J project. More information about
* this project can be found here:  https://pi4j.com/
* **********************************************************************
* %%
* Copyright (C) 2012 - 2022 Pi4J
* %%
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* #L%
*/


import java.util.concurrent.Callable;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSetStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSyncStateTrigger;

/**
* This example code demonstrates how to setup simple triggers for GPIO pins on the Raspberry Pi.
*
* @author Robert Savage
*/
public class TriggerGpioExample {

 public static void main(String[] args) throws InterruptedException {

     System.out.println("<--Pi4J--> GPIO Trigger Example ... started.");

     // create gpio controller
     final GpioController gpio = GpioFactory.getInstance();

     // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
     final GpioPinDigitalInput button1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25,
                                               PinPullResistance.PULL_UP);
     final GpioPinDigitalInput button2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_31,
             PinPullResistance.PULL_UP);

     button1.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
         public Void call() throws Exception {
             System.out.println(" --> GPIO TRIGGER CALLBACK RECEIVED button1");
             return null;
         }
     }));

     button2.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
         public Void call() throws Exception {
             System.out.println(" --> GPIO TRIGGER CALLBACK RECEIVED button2");
             return null;
         }
     }));

     // keep program running until user aborts (CTRL-C)
     while (true) {
         Thread.sleep(500);
     }

     // stop all GPIO activity/threads by shutting down the GPIO controller
     // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
     // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
 }
}
//END SNIPPET: trigger-gpio-snippet
