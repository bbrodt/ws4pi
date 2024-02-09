package com.zarroboogsfound.ws4pi.macros;

public class TargetPosition {
	
	public TargetPosition() {
		startSteps = stopSteps = stepDelay = 0;
	}
	
	public float direction; // for MOTOR_BRIDGE this is the SpeedVector direction
	// for MOTOR_BRIDGE, this is the SpeedVector magnitude
	// for SERVO, this is the servo position (-100 to 100) 
	public float position; 
	// for MOTOR_BRIDGE this is the time (in seconds) to run the motor(s)
	// for the pseudo DELAY device, this is the delay duration (in seconds)
	public float duration;
	// these are used for both SERVO and MOTOR_BRIDGE

	// number of servo position changes or motor speed changes at startup
	public float startSteps;
	// same as above but for stopping the servo/motor
	public float stopSteps;
	// delay in milliseconds between each position/speed change used as
	// a multiplier for startSteps and stopSteps
	public float stepDelay; 
}