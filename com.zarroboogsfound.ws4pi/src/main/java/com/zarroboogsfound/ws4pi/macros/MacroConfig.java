package com.zarroboogsfound.ws4pi.macros;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

public class MacroConfig {
	public String name;
	public String revision;
	public String date;
	public String description;
	public List<Macro> macros;
	
	private static MacroConfig instance;
	
	private MacroConfig() {
		instance = this;
	}
	
	public static MacroConfig getInstance() {
		if (instance==null)
			instance = new MacroConfig();
		return instance;
	}
	
	public static MacroConfig load(String filename) throws FileNotFoundException {
		FileReader fr = new FileReader(filename);
		Gson gson = new GsonBuilder() .create();
		instance = gson.fromJson(fr, MacroConfig.class);
		return instance;
	}
	
	public Macro get(String name) {
		for (Macro m : macros) {
			if (m.name.equalsIgnoreCase(name))
				return m;
		}
		return null;
	}
}
