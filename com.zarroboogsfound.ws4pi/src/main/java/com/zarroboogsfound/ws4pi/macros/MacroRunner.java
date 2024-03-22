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
import com.zarroboogsfound.ws4pi.devices.DualMotorBridgeController;
import com.zarroboogsfound.ws4pi.devices.ExecController;
import com.zarroboogsfound.ws4pi.devices.LEDController;
import com.zarroboogsfound.ws4pi.devices.ServoController;
import com.zarroboogsfound.ws4pi.devices.SoundController;

public class MacroRunner {
	private WS4PiConfig config;
	private MacroConfig macroConfig;
	private List<MacroRunnable> activeRunnables;
	private Queue<Macro> macroQueue;
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
						System.out.println(System.currentTimeMillis()+" MACRO "+a.name);
						MacroRunner.getInstance().run(a.name);
						break;
					case MOTOR_BRIDGE:
						System.out.println(System.currentTimeMillis()+" MOTOR_BRIDGE "+a.name);
						DualMotorBridgeController motorCtl = (DualMotorBridgeController)controller;
						// FIX ME: currently only 1 motor bridge is supported, id=0
						motorCtl.setSpeedVector(0, a.direction, a.value);
						break;
					case SERVO:
						ServoController servoCtl = (ServoController)controller;
						Device d = config.getDevice(DeviceType.SERVO, a.name);
						if (a.targets.size()>0) {
							for (TargetPosition t : a.targets) {
								if ("startPos".equals(t.limitPos))
									t.position = d.limits.startPos;
								if ("minPos".equals(t.limitPos))
									t.position = d.limits.minPos;
								if ("maxPos".equals(t.limitPos))
									t.position = d.limits.maxPos;
							}
							if (a.targets.size()==1 && a.targets.get(0).startSteps==0 && a.targets.get(0).stopSteps==0 ) {
								System.out.println(System.currentTimeMillis()+" SERVO "+a.name+" position="+a.targets.get(0).position+" delay="+a.targets.get(0).stepDelay);
								servoCtl.setPosition(d.id, a.targets.get(0).position, a.targets.get(0).stepDelay );
							}
							else {
								System.out.println(System.currentTimeMillis()+" SERVO "+a.name+" targets");
								servoCtl.setTargetPositions(d.id, a.targets.toArray(new TargetPosition[a.targets.size()]));
							}
						}
						else {
							System.out.println(System.currentTimeMillis()+" SERVO "+a.name+" position="+a.value);
							servoCtl.setPosition(d.id, (float)a.value, 0.1f );
						}
						break;
					case STEPPER:
						break;
					case SWITCH:
						break;
					case ULTRASOUND:
						break;
					case DELAY:
						System.out.println(System.currentTimeMillis()+" DELAY "+a.value);
						Thread.sleep((long)a.value);
						break;
					case WAIT:
						System.out.println(System.currentTimeMillis()+" WAIT "+a.name);
						ServoController c = (ServoController)config.getDeviceProvider().getDeviceController(DeviceType.SERVO);
						Device sd = config.getDevice(DeviceType.SERVO, a.name);
						while (c.isBusy(sd.id))
							Thread.sleep(500);
						break;
					case LED:
						// FIX ME: currently only 1 LED is supported, id=0
						LEDController ledCtl = (LEDController)controller;
						if (a.value!=0)
							ledCtl.on(0);
						else
							ledCtl.off(0);
						break;
					case NULL_DEVICE:
						break;
					case SOUND:
						System.out.println(System.currentTimeMillis()+" SOUND "+a.name);
						SoundController soundCtl = (SoundController)controller;
						soundCtl.play("sounds/" + a.name);
						break;
					case LOOP:
						for (int loopcount=0; loopcount<a.value; ++loopcount) {
							runActions(a.actions);
						}
						break;
					case EXEC:
						ExecController execCtl = (ExecController)controller;
						execCtl.exec(a.name);
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
		config = WS4PiConfig.getInstance();
		macroConfig = MacroConfig.getInstance();
		activeRunnables = new ArrayList<MacroRunnable>();
		macroQueue = new ConcurrentLinkedQueue<Macro>();

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
		config = null;
		macroConfig = null;
		activeRunnables = null;
		macroQueue = null;
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
		if (activeRunnables!=null) {
			for (MacroRunnable mr : activeRunnables) {
				for (Action mra : mr.getMacro().actions) {
					for (Action ma : m.actions ) {
						if (mra.type == ma.type && mra.name.equals(ma.name)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private void enqueueMacro(Macro m) {
		if (macroQueue==null)
			return;
		macroQueue.add(m);
	}
	
	private Macro peekMacro() {
		if (macroQueue==null)
			return null;
		return macroQueue.peek();
	}
	
	private Macro dequeueMacro() {
		if (macroQueue==null)
			return null;
		return macroQueue.poll();
	}
	
	private synchronized void activateRunnable(MacroRunnable mr) {
		if (mr!=null && activeRunnables!=null) {
			activeRunnables.add(mr);
		}
	}
	
	private synchronized void deactivateRunnable(MacroRunnable mr) {
		if (mr!=null && activeRunnables!=null) {
			activeRunnables.remove(mr);
		}
	}
}
