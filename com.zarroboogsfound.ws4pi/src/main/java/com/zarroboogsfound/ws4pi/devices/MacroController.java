package com.zarroboogsfound.ws4pi.devices;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import javax.naming.NameNotFoundException;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.data.QueryParams;
import com.zarroboogsfound.ws4pi.file.FileEvent;
import com.zarroboogsfound.ws4pi.file.FileEventAdapter;
import com.zarroboogsfound.ws4pi.file.FileWatcher;
import com.zarroboogsfound.ws4pi.macros.MacroConfig;
import com.zarroboogsfound.ws4pi.macros.MacroRunner;

import io.undertow.server.HttpServerExchange;

public class MacroController extends DeviceController {

	private MacroConfig macroConfig;
	private MacroRunner macroRunner;
	private FileWatcher watcher;
	
	public MacroController() {
		super(DeviceType.MACRO);
		watcher = new FileWatcher( new File("config") );
	}
    
	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		if ( QueryParams.get(exchange, "list").isPresent() )
			return macroConfig.macros;
		Optional<String> name = QueryParams.get(exchange, "name");
		if (name.isPresent())
			return macroConfig.get(name.get());
		return null;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		String name = QueryParams.get(exchange, "name").get();
		Optional<String> params = QueryParams.get(exchange, "params");
		if (runMacro(name))
			return true;
		return false;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		// load and parse macro file
		try {
			macroConfig = MacroConfig.load("config/macros.json");
			macroRunner = MacroRunner.getInstance();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(WS4PiConfig config) {
		watcher.addListener(new FileEventAdapter() {
			public void onModified(FileEvent event) {
				System.out.println("Config folder change event: "+event.getFile().getName());
				if (event.getFile().getName().equalsIgnoreCase("macros.json")) {
					macroConfig = null;
					if (macroRunner!=null) {
						macroRunner.stop();
						macroRunner = null;
					}
					try {
						initialize(null);
						macroRunner.start();
					}
					catch (Exception e) {
					}
				}
			}
		});
		watcher.watch();
		macroRunner.start();
		try {
			macroRunner.run("startup");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			macroRunner.run("macro shutdown");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		macroRunner.stop();
	}
	
	public boolean isBusy(int id) {
		return macroRunner.isActive();
	}

	@Override
	public Component getComponent(int id) {
		return new NullComponent();
	}

	@Override
	public int getComponentCount() {
		return 1;
	}

	public boolean runMacro(String name) {
		try {
			macroRunner.run(name);
			return true;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
