package com.zarroboogsfound.ws4pi.devices;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class DelayController extends DeviceController {

	Thread runner;
	
	public DelayController() {
		super(DeviceType.DELAY);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(WS4PiConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Component getComponent(int id) {
		return new NullComponent();
	}

	@Override
	public int getComponentCount() {
		return 1;
	}
	
	public void delay(long msec) {
		if (runner!=null && runner.isAlive())
			runner.interrupt();
		runner = null;
		runner = new Thread( new Runnable() {
				public void run() {
					try {
						Thread.sleep(msec);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		);
		runner.run();
	}
	
    public boolean isBusy(int channel) {
		return runner!=null && runner.isAlive();
    }
}
