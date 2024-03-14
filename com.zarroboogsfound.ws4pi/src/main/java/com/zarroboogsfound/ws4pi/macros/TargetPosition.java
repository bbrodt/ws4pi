package com.zarroboogsfound.ws4pi.macros;

public class TargetPosition {
	
	public TargetPosition() {
		position = startSteps = stopSteps = stepDelay = 0;
	}
	
	// the servo position (-100 to 100) 
	public float position; 
	// number of servo position changes or motor speed changes at startup
	public float startSteps;
	// same as above but for stopping the servo/motor
	public float stopSteps;
	// delay in milliseconds between each position/speed change used as
	// a multiplier for startSteps and stopSteps
	public float stepDelay; 
}