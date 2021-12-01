package com.pi4j.component.servo;

public interface SmoothServo extends Servo {

	// set the time (in milliseconds) for servo to move from a full stop to its current
	// maximum speed, as specified with setSpeed().    
	void setSmoothStart(int msec);
	void setSmoothStop(int msec);
	// Speed is just a magic number in the range [1,100] used to calculate smooth step increment
	// Default is 100
	void setSpeed(int speed);
	// The time it takes for the servo to move through its entire range from full left position
	// to its full right position (i.e. for setPosition() values of -100 to 100).
	// Each servo is different, depending on its motor speed, operating voltage, reduction gearing,
	// output arm load, etc. and should be determined empirically under the actual conditions which
	// the servo will be used. 
	void setTravelTime(int msec);
}
