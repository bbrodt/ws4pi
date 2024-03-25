package com.zarroboogsfound.ws4pi.devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class SoundController extends DeviceController {

	public SoundController() {
		super(DeviceType.SOUND);
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
		play("sounds/startup.wav");
	}
	
	@Override
	public void stop() {
		play("sounds/shutdown.wav");
		try {
			// allow shutdown sound to play before the entire process exits
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void play(final String filepath) {
		Thread t = new Thread ( ) {

			@Override
			public void run() {
				try {
					Process proc = Runtime.getRuntime().exec(new String[] {"cvlc", "--aout=alsa", "--no-video", "--play-and-exit", filepath} );
		            InputStream stderr = proc.getErrorStream();
		            InputStreamReader isr = new InputStreamReader(stderr);
		            BufferedReader br = new BufferedReader(isr);
		            String line = null;
		            int lineCount = 0;
		            while ( (line = br.readLine()) != null) {
		            	if (lineCount==0) {
				            System.out.println("<ERROR>");
		            	}
		                System.out.println(line);
		                ++lineCount;
		            }
		            if (lineCount>0)
		            	System.out.println("</ERROR>");
					int exitVal = proc.waitFor();
					System.out.println("Process exitValue: " + exitVal);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
		};
		t.start();
	}

	public boolean isBusy(int id) {
		return false;
	}

	@Override
	public Component getComponent(int id) {
		return new NullComponent();
	}

	@Override
	public int getComponentCount() {
		return 1;
	}

}
