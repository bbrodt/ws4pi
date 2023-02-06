package com.zarroboogsfound.ws4pi.devices;

import java.util.Map;

import com.pi4j.component.Component;

public class NullComponent implements Component {

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "NullComponent";
	}

	@Override
	public void setTag(Object tag) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasProperty(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProperty(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearProperties() {
		// TODO Auto-generated method stub

	}
}
