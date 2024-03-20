package com.zarroboogsfound.ws4pi;

public enum DeviceType {
	STEPPER,
	SWITCH,
	MOTOR_BRIDGE,
	ULTRASOUND,
	SERVO,
	LED,
	MACRO,
	SOUND, // plays a sound from the ./sound folder
	DELAY, // special time delay device type used for pauses in macro execution
	EXECPROC, // executes a specified process in background
	LOOP, // pseudo device type to allow macro loops
	WAIT, // pseudo device type to wait for given servo name to stop moving
	NULL_DEVICE
}