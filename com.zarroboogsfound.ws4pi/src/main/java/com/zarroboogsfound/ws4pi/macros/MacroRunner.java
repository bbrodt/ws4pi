package com.zarroboogsfound.ws4pi.macros;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.naming.NameNotFoundException;

import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.WS4PiConfig.Device;
import com.zarroboogsfound.ws4pi.devices.DeviceController;
import com.zarroboogsfound.ws4pi.devices.DeviceException;
import com.zarroboogsfound.ws4pi.devices.ServoController;
import com.zarroboogsfound.ws4pi.devices.UltrasoundController;

public class MacroRunner {
	private WS4PiConfig config = WS4PiConfig.getInstance();
	private MacroConfig macroConfig = MacroConfig.getInstance();
	private List<MacroRunnable> activeRunnables = new ArrayList<MacroRunnable>();
	private Queue<Macro> macroQueue = new ConcurrentLinkedQueue<Macro>();
	private Thread runner;
	private boolean stopExecution = false;
	
	private static MacroRunner instance;
	
	class MacroRunnable implements Runnable {
		Macro macro;
		
		public MacroRunnable(Macro m) {
			macro = m;
		}
		
		public Macro getMacro() {
			return macro;
		}
		
		@Override
		public void run() {
			try {
				// Collect all actions grouped by priority.
				// These are all executed simultaneously.
				// All actions in each priority group must complete before
				// the next priority group is considered for execution.
				List<Action> actions = new ArrayList<Action>();
				List<DeviceController> controllers = new ArrayList<DeviceController>();
				int count = 0;
				int priority=0;
				
				while (true) {
					actions.clear();
					controllers.clear();
					for (Action a : macro.actions) {
						if (a.priority == priority) {
							actions.add(a);
							count++;
						}
					}
					if (actions.size()>0) {
						// run this group of actions and wait for completion
						controllers = runActions(actions);
					}
					// if all actions have been executed, we're done with this macro
					if (count == macro.actions.size())
						break;
					
					// wait until all controllers involved in the action group report "not busy"
					while (controllers.size()>0) {
						count = 0;
						for (DeviceController c : controllers) {
							boolean busy = false;
							for (int id = 0; id<c.getComponentCount(); ++id) {
								if (c.isBusy(id)) {
									busy = true;
									break;
								}
							}
							if (!busy)
								++count;
						}
						if (count==controllers.size())
							break;
						Thread.sleep(100);
					}
					// do the next priority group
					++priority;
				}
			}
			catch (Exception e) {
			}
			deactivateRunnable(this);
		}
		
		private List<DeviceController> runActions(List<Action> actions) {
			List<DeviceController> controllers = new ArrayList<DeviceController>();
			
			try {
				for (int i=0; i<actions.size(); ++i) {
					Action a = actions.get(i);
					DeviceType type = DeviceType.valueOf(a.type.toUpperCase());
					DeviceController controller = config.getDeviceProvider().getDeviceController(type);
					controllers.add(controller);

					switch (type) {
					case MACRO:
						break;
					case MOTOR_BRIDGE:
						break;
					case SERVO:
						ServoController sc = (ServoController)controller;
						Device d = config.getDevice(DeviceType.SERVO, a.name);
						sc.setTargetPositions(d.id, a.targets.toArray(new TargetPosition[a.targets.size()]));
						break;
					case STEPPER:
						break;
					case SWITCH:
						break;
					case ULTRASOUND:
						break;
					case DELAY:
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return controllers;
		}
	}
	
	private MacroRunner() {
		instance = this;
	}
	
	public void start() {
		// create a thread to poll the macro queue
		Thread poller = new Thread() {

			@Override
			public void run() {
				while (!stopExecution) {
					try {
						Thread.sleep(1000);
						Macro m = peekMacro();
						if (m!=null && canRunMacro(m)) {
							m = dequeueMacro();
							MacroRunner.getInstance().run(m.name);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}			
				}
			}
		};
		poller.start();
	}

	public void stop() {
		macroQueue.clear();
		stopExecution = true;
		while (isActive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isActive() {
		// TODO Auto-generated method stub
		return runner!=null && runner.isAlive() || macroQueue.size()>0;
	}

	public static MacroRunner getInstance() {
		if (instance==null) {
			instance = new MacroRunner();
		}
		return instance;
	}

	public void run(String macroName) throws NameNotFoundException {
		boolean found = false;
		for (Macro m : macroConfig.macros) {
			if (m.name.equalsIgnoreCase(macroName)) {
				// check if we can run this macro: we can only run macros in
				// parallel if none of the devices are in use by any currently
				// running macros. If the requested macro can not be executed
				// then queue it.
				if (!canRunMacro(m)) {
					enqueueMacro(m);
					return;
				}
				MacroRunnable mr = new MacroRunnable(m);
				activateRunnable(mr);
				runner = null;
				runner = new Thread(mr);
				runner.start();
				found = true;
			}
		}
		if (!found)
			throw new NameNotFoundException("The macro '"+macroName+"' was not found");
	}
	
	private boolean canRunMacro(Macro m) {
		for (MacroRunnable mr : activeRunnables) {
			for (Action mra : mr.getMacro().actions) {
				for (Action ma : m.actions ) {
					if (mra.type == ma.type && mra.name.equals(ma.name)) {
						// queue this macro
						enqueueMacro(m);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private void enqueueMacro(Macro m) {
		macroQueue.add(m);
	}
	
	private Macro peekMacro() {
		return macroQueue.peek();
	}
	
	private Macro dequeueMacro() {
		return macroQueue.poll();
	}
	
	private synchronized void activateRunnable(MacroRunnable mr) {
		if (mr!=null) {
			activeRunnables.add(mr);
		}
	}
	
	private synchronized void deactivateRunnable(MacroRunnable mr) {
		if (mr!=null) {
			activeRunnables.remove(mr);
		}
	}
}
