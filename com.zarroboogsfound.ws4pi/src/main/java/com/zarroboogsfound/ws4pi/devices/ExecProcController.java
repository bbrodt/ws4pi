package com.zarroboogsfound.ws4pi.devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;

import io.undertow.server.HttpServerExchange;

public class ExecProcController extends DeviceController {

	public ExecProcController() {
		super(DeviceType.EXECPROC);
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
	}
	
	@Override
	public void stop() {
	}
	
	public Process exec(String cmd) {
		class ExecProcThread extends Thread {
			private Process proc;
			private String cmdLine;
			
			ExecProcThread(String cmd) {
				this.cmdLine = cmd;
			}
			
			public Process exec() {
				try {
					proc = Runtime.getRuntime().exec(cmdLine);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return proc;
			}
			
			@Override
			public void run() {
				try {
		            InputStream stderr = proc.getErrorStream();
		            InputStreamReader isr = new InputStreamReader(stderr);
		            BufferedReader br = new BufferedReader(isr);
		            String line = null;
		            System.out.println("<ERROR>");
		            while ( (line = br.readLine()) != null)
		                System.out.println(line);
		            System.out.println("</ERROR>");
					int exitVal = proc.waitFor();
					System.out.println("Process exitValue: " + exitVal);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		
		ExecProcThread t = new ExecProcThread(cmd);
		Process p = t.exec();
		t.start();
		return p;
	}

	public void kill(int pid) {
		Optional<ProcessHandle> h = ProcessHandle.of(pid);
		if (h.isPresent()) {
			h.get().destroy();
		}
	}
	
	public boolean isBusy(int id) {
		Optional<ProcessHandle> h = ProcessHandle.of(id);
		if (h.isPresent()) {
			return h.get().isAlive();
		}
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
