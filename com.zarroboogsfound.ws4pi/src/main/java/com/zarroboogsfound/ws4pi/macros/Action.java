package com.zarroboogsfound.ws4pi.macros;

import java.util.ArrayList;
import java.util.List;

import com.zarroboogsfound.ws4pi.DeviceType;

public class Action {
	public Action() {
		type = "NULL_DEVICE";
		name = "null_device";
		value = 0;
		direction = 0;
		priority = 0;
	}
	// device type; one of the DeviceType enum string values
	public String type;
	// Action name: for SOUND this is the audio file name,
	// FIX ME: for MOTOR_BRIDGE this is the DualMotorBridge device name
	// for all other device types this is for descriptive purposes only
	public String name;
	
	// used by SERVO, MOTOR_BRIDGE and DELAY devices
	// for SERVO, this is the target position (replaces TargetPosition items)
	public float value;
	// used by MOTOR_BRIDGE device only
	public float direction;
	// servo movement descriptors; position and how fast to get there
	public List<TargetPosition> targets = new ArrayList<TargetPosition>();
	public int priority;
	public List<Action> actions = new ArrayList<Action>();
}