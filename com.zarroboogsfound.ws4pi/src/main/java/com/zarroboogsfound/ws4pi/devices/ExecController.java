package com.zarroboogsfound.ws4pi.devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.wildfly.common.annotation.NotNull;

import com.pi4j.component.Component;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class ExecController extends DeviceController {

	private List<Process> procList = new ArrayList<Process>();
	
	public ExecController() {
		super(DeviceType.EXEC);
	}

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		Optional<Integer> pidParam = QueryParams.getInt(exchange, "pid");
		if (pidParam.isPresent())
			return isBusy(pidParam.get().intValue());
		return 0;
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		Optional<String> cmdParam = QueryParams.get(exchange, "cmd");
		Optional<Integer> killParam = QueryParams.getInt(exchange, "kill");
		
		if (cmdParam.isPresent()) {
			Process proc = exec(cmdParam.get());
			if (proc!=null)
				return proc.pid();
		}
		else if (killParam.isPresent()){
			return kill(killParam.get());
		}
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(WS4PiConfig config) {
		exec("mjpeg_server.py");
	}
	
	@Override
	public void stop() {
		for (Process p : procList) {
			kill(p.pid());
		}
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
		procList.add(p);
		return p;
	}

	public long kill(long pid) {
		Optional<ProcessHandle> h = ProcessHandle.of(pid);
		if (h.isPresent()) {
			h.get().destroy();
			return pid;
		}
		return 0;
	}
	
	public boolean isBusy(int pid) {
		Optional<ProcessHandle> h = ProcessHandle.of(pid);
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
