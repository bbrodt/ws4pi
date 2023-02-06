package com.zarroboogsfound.ws4pi.macros;

import java.util.ArrayList;
import java.util.List;

public class Macro {
	public Macro() {
		name = "null macro";
		description = "do-nothing place holder";
	}
	public String name;
	public String description;
	public List<Action> actions = new ArrayList<Action>();
}