package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.pi4j.component.motor.MotorState;
import com.pi4j.component.motor.impl.GpioStepperMotorComponent;
import com.pi4j.component.switches.impl.GpioSwitchComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class StepperMotorController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();

    // create byte array to demonstrate a single-step sequencing
    // (This is the most basic method, turning on a single electromagnet every time.
    //  This sequence requires the least amount of energy and generates the smoothest movement.)
    final byte[] single_step_sequence = {
	    0b0001,
	    0b0010,
	    0b0100,
	    0b1000
    };

    // create byte array to demonstrate a double-step sequencing
    // (In this method two coils are turned on simultaneously.  This method does not generate
    //  a smooth movement as the previous method, and it requires double the current, but as
    //  return it generates double the torque.)
    final byte[] double_step_sequence = {
	    0b0011,
	    0b0110,
	    0b1100,
	    0b1001
    };

    // create byte array to demonstrate a half-step sequencing
    // (In this method two coils are turned on simultaneously.  This method does not generate
    //  a smooth movement as the previous method, and it requires double the current, but as
    //  return it generates double the torque.)
    final byte[] half_step_sequence = {
		0b0001,
		0b0011,
		0b0010,
		0b0110,
		0b0100,
		0b1100,
		0b1000,
		0b1001
    };

	private GpioStepperMotorComponent steppers[];
	private class StepperInfo {
		public int id;
		public String state;
		public float position;
		
		public StepperInfo(int id, MotorState state, float position) {
			this.id = id;
			this.state = state.name();
			this.position = position;
		}
	};

	public StepperMotorController() {
		super(DeviceType.STEPPER);
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
        GpioPin[] pins1 = config.getGpioPins(DeviceType.STEPPER, 0);
        GpioPin[] pins2 = config.getGpioPins(DeviceType.STEPPER, 1); 

        // this will ensure that the motor is stopped when the program terminates
        gpio.setShutdownOptions(true, PinState.LOW, pins1);
        gpio.setShutdownOptions(true, PinState.LOW, pins2);

        // configure Stepper #1
        steppers = new GpioStepperMotorComponent[2];
        steppers[0] = new GpioStepperMotorComponent(pins1);
        steppers[0].setStepSequence(double_step_sequence);
        steppers[0].setDirectionReversed(true);
        steppers[0].setPulleyDiameter(40f);

        steppers[0].setStepsPerRevolution(2038);

        // There are 32 steps per revolution on my sample motor, and inside is a ~1/64 reduction gear set.
        // Gear reduction is actually: (32/9)/(22/11)x(26/9)x(31/10)=63.683950617
        // This means is that there are really 32*63.683950617 steps per revolution =  2037.88641975 ~ 2038 steps!
        steppers[0].setStepsPerRevolution(2038);
        
        // configure Stepper #2
        steppers[1] = new GpioStepperMotorComponent(pins2);
        steppers[1].setStepSequence(double_step_sequence);
        steppers[1].setDirectionReversed(false);
        steppers[1].setPulleyDiameter(40f);

        steppers[1].setStepsPerRevolution(2038);
	}

	@Override
	public void start(WS4PiConfig config) {
        // add limit switches for the steppers
		SwitchController controller = (SwitchController)config.getDeviceProvider().getDeviceController(DeviceType.SWITCH);

		for (int i=0; i<getComponentCount(); ++i) {
			GpioSwitchComponent sw = (GpioSwitchComponent)controller.getComponent(i);
	    	steppers[i].addLimitSwitch(sw, 0f);
	    	// advance past the switch stop if necessary and then step up to the limit
	        steppers[i].step(200);
	        steppers[i].waitForStop();
			steppers[i].setState(MotorState.REVERSE);
		}
	}

	public boolean isBusy(int id) {
		return steppers[id].getState()!=MotorState.STOP;
	}
	
	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		validate(id);
		boolean async = true;
		if (exchange.getQueryParameters().containsKey("async")) {
			async = QueryParams.getBool(exchange, "async").get();
		}

		GpioStepperMotorComponent s = steppers[id];
		if (!async) {
			while (s.getState()!=MotorState.STOP) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
		}
		StepperInfo info = new StepperInfo(id, s.getState(), s.stepsToDistance(s.getCurrentStepPosition()));
		return info;
	}
	
	public void validate(int id, int direction) throws DeviceException {
		super.validate(id);
		if (direction!=-1 && direction !=0 && direction !=1)
			throw new DeviceException("Direction must be -1 0 or 1");
	}
	
	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		int id = QueryParams.getInt(exchange, "id").get().intValue();
		
		if (exchange.getQueryParameters().containsKey("direction")) {
			// run continuously in given direction
			int direction = QueryParams.getFloat(exchange, "direction").get().intValue();
			validate(id, direction);
			if (direction<0)
				steppers[id].setState(MotorState.REVERSE);
			else if (direction>0)
				steppers[id].setState(MotorState.FORWARD);
			else
				steppers[id].setState(MotorState.STOP);
		}
		else if (exchange.getQueryParameters().containsKey("value")) {
			// move to the given step position value
			float value = QueryParams.getFloat(exchange, "value").get().intValue();
			validate(id);
			steppers[id].moveAbsolute(value);
		}
		else if (exchange.getQueryParameters().containsKey("offset")) {
			// move to the given step offset value
			float offset = QueryParams.getFloat(exchange, "offset").get().intValue();
			validate(id);
			steppers[id].moveRelative(offset);
		}
		return null;
	}

	@Override
	public int getComponentCount() {
		if (steppers!=null)
			return steppers.length;
		return 0;
	}

	@Override
	public Component getComponent(int id) {
		if (steppers!=null && steppers.length>id)
			return steppers[id];
		return null;
	}
}
