package com.zarroboogsfound.ws4pi.macros;

import java.util.ArrayList;
import java.util.List;

import com.zarroboogsfound.ws4pi.DeviceType;

public class Action {
	public Action() {
		type = "NULL_DEVICE";
		name = "null_device";
		priority = 0;
	}
	public String type;
	public String name;
	public List<TargetPosition> targets = new ArrayList<TargetPosition>();
	public int priority;
}