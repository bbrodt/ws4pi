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
	NULL_DEVICE
}