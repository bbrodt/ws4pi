package com.zarroboogsfound.ws4pi.devices;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Deque;
import java.util.Map;

import com.pi4j.component.Component;
import com.pi4j.component.servo.Servo;
import com.pi4j.component.servo.impl.GenericServo;
import com.pi4j.component.servo.impl.PCA9685GpioServoProvider;
import com.pi4j.component.servo.impl.GenericServo.Orientation;
import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.WS4PiConfig.Device;
import com.zarroboogsfound.ws4pi.data.QueryParams;
import com.zarroboogsfound.ws4pi.macros.TargetPosition;

import io.undertow.server.HttpServerExchange;

public class ServoController extends DeviceController {
    // Create custom PCA9685 GPIO provider
    I2CBus bus;
    private PCA9685GpioProvider gpioProvider;
    private PCA9685GpioServoProvider gpioServoProvider;
    private Servo[] servos;
    private Device[] devices;
    private Positioner positioners[];
    private float currentPositions[];

    public ServoController() {
    	super(DeviceType.SERVO);
    }
    
    public void initialize(WS4PiConfig config) throws UnsupportedBusNumberException, IOException {
    	
        devices = config.getDevices(DeviceType.SERVO);
        servos = new Servo[devices.length];
        currentPositions = new float[devices.length];
        positioners = new Positioner[devices.length];
        
        gpioProvider = createProvider();
        provisionPwmOutputs(gpioProvider);
        gpioServoProvider = new PCA9685GpioServoProvider(gpioProvider);

        
        for (int channel=0; channel<servos.length; ++channel) {
            servos[channel] = new GenericServo(gpioServoProvider.getServoDriver(PCA9685Pin.ALL[channel]), devices[channel].name);
            servos[channel].setProperty(Servo.PROP_END_POINT_LEFT, Float.toString(Servo.END_POINT_MAX));
            servos[channel].setProperty(Servo.PROP_END_POINT_RIGHT, Float.toString(Servo.END_POINT_MAX));
            servos[channel].setProperty(Servo.PROP_IS_REVERSE, Boolean.toString(devices[channel].limits.reversed));
        }
    }

    public void setSpeed(int speed) {
    	
    }
    
	@Override
	public void start(WS4PiConfig config) {
		// move all servos to their start positions
        for (int channel=0; channel<servos.length; ++channel) {
        	Device d = devices[channel];
        	try {
				setPosition(channel, d.limits.startPos);
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        //////// TEST CODE
        /*
        TargetPosition t[] = new TargetPosition[1];
        t[0] = new TargetPosition();
        t[0].startSteps = 12;
        t[0].stopSteps = 12;
        t[0].stepDelay = 1;
        try {
        	while(true) {
	            t[0].position = 80;
	            positioners[1] = new Positioner(1, t);
				while (isBusy(1))
					Thread.sleep(100);
	            positioners[1] = null;

				t[0].position = -80;
	            positioners[1] = new Positioner(1, t);
				while (isBusy(1))
					Thread.sleep(100);
	            positioners[1] = null;
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	@Override
	public void stop() {
		// move all servos back to their start positions.
        for (int channel=0; channel<servos.length; ++channel) {
        	Device d = devices[channel];
        	try {
                TargetPosition t[] = new TargetPosition[1];
                t[0] = new TargetPosition();
        		t[0].position = d.limits.startPos;
                t[0].startSteps = 12;
                t[0].stopSteps = 12;
                t[0].stepDelay = 1;

	            positioners[channel] = new Positioner(channel, t);
				while (isBusy(1))
					Thread.sleep(100);
	            positioners[channel] = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	@Override
	public void shutdown() {
		gpioProvider.shutdown();
	}
	
    public void setPosition(int channel, float pos) throws DeviceException {
    	setPosition(channel, pos, 0);
    }
    
    public void setPosition(int channel, float pos, int stepDelay) throws DeviceException {
    	pos = adjustPos(channel, pos);
        stopPositioner(channel);
        if (stepDelay==0)
        	servos[channel].setPosition(currentPositions[channel] = pos);
        else {
        	
        }
    }
    
    public void setTargetPositions(int channel, TargetPosition targets[]) throws DeviceException {
    	super.validate(channel);
        stopPositioner(channel);
        positioners[channel] = new Positioner(channel,targets);
        positioners[channel].start();
    }
    
    public boolean isBusy(int channel) {
    	if (positioners[channel]!=null && positioners[channel].isAlive())
    		return true;
    	return false;
    }
    
    private float adjustPos(int channel, float pos) throws DeviceException {
    	super.validate(channel);
    	Device d = devices[channel];
        if (pos < d.limits.minPos) {
        	pos = d.limits.minPos;
        }
        if (pos > d.limits.maxPos)
        	pos = d.limits.maxPos;
        return pos;
    }
    
    private void stopPositioner(int channel) {
    	if (positioners[channel]!=null && positioners[channel].isAlive()) {
    		positioners[channel].interrupt();
    		positioners[channel] = null;
    	}
    }

    private class Positioner extends Thread {
        private int channel;
        private float currentPosition;
        private float endPosition;
        private float startSteps;
        private float middleSteps;
        private float stopSteps;
        TargetPosition targets[];
        
        @SuppressWarnings("unused")
		private Positioner() {
        }
        
        public Positioner(int channel, TargetPosition targets[]) {
        	this.targets = targets;
        	this.channel = channel;
        }
        
        @Override
        public void run() {
        	try {
	        	for (TargetPosition t : targets) {
		        	endPosition = adjustPos(channel,t.position);
		        	currentPosition = currentPositions[channel];
		        	float distance  = (int)Math.abs(endPosition - currentPosition);
		    		startSteps = t.startSteps;
		    		stopSteps = t.stopSteps;
		        	if (distance <= startSteps + stopSteps) {
		        		startSteps = stopSteps = distance/2;
		        		middleSteps = 0;
		        	}
		        	else {
		        		middleSteps = distance - (startSteps + stopSteps);
		        	}
		
		            while (!Thread.currentThread().isInterrupted() && currentPosition!=endPosition) {
		            	try {
		            		// this must all happen atomically to ensure
		            		// currentPositions[] is properly updated
		                	if (endPosition < currentPosition)
		                		--currentPosition;
		                	else
		                		++currentPosition;
		                    servos[channel].setPosition(currentPosition);
				            currentPositions[channel] = currentPosition;
		            	}
		            	catch (Exception e) {
		            		throw new InterruptedException();
		            	}
	                    if (startSteps>0) {
	                    	Thread.sleep((long) (startSteps*startSteps*t.stepDelay));
	                    	--startSteps;
	                    }
	                    else if (middleSteps>0) {
	                    	Thread.sleep(3);
	                    	--middleSteps;
	                    }
	                    else if (stopSteps>0) {
	                    	Thread.sleep((long) (stopSteps*stopSteps*t.stepDelay));
	                    	--stopSteps;
	                    	
	                    }
	                    else {
	                    	break;
	                    }
		            }
	        	}
	        } catch (InterruptedException ex) {
	            Thread.currentThread().interrupt();
	        } catch (DeviceException e) {
				e.printStackTrace();
			}
	    }
    }
    
    public GpioProvider getGpioProvider() throws Exception {
    	if (gpioProvider==null)
    		gpioProvider = createProvider();
    	return gpioProvider;
    }

    private PCA9685GpioProvider createProvider() throws IOException, UnsupportedBusNumberException {
        // This would theoretically lead into a resolution of 5 microseconds per step:
        // 4096 Steps (12 Bit)
        // T = 4096 * 0.000005s = 0.02048s
        // f = 1 / T = 48.828125
        BigDecimal frequency = new BigDecimal("48.828125");
        //frequency = PCA9685GpioProvider.ANALOG_SERVO_FREQUENCY;
        // Correction factor: actualFreq / targetFreq
        // e.g. measured actual frequency is: 51.69 Hz
        // Calculate correction factor: 51.65 / 48.828 = 1.0578
        // --> To measure actual frequency set frequency without correction factor(or set to 1)
        BigDecimal frequencyCorrectionFactor = new BigDecimal("1.0578");

        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        return new PCA9685GpioProvider(bus, 0x40, frequency, frequencyCorrectionFactor);
    }

    private GpioPinPwmOutput[] provisionPwmOutputs(final PCA9685GpioProvider gpioProvider) {
        GpioController gpio = GpioFactory.getInstance();
        GpioPinPwmOutput myOutputs[] = {
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00, "Servo 00"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01, "Servo 01"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02, "Servo 02"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03, "Servo 03"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04, "Servo 04"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05, "Servo 05"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06, "Servo 06"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07, "Servo 07"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08, "Servo 08"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09, "Servo 09"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10, "Servo 10"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11, "Servo 11"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12, "Servo 12"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13, "Servo 13"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14, "Servo 14"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_15, "Servo 15")};
        return myOutputs;
    }

	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		int channel = QueryParams.getInt(exchange, "channel").get().intValue();
		validate(channel);
		return servos[channel]; //Float.valueOf(position);
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		int channel = QueryParams.getInt(exchange, "channel").get().intValue();
		float position = QueryParams.getFloat(exchange, "position").get().floatValue();
		position = adjustPos(channel, position);
		servos[channel].setPosition(position);
		return servos[channel]; //Float.valueOf(position);
	}

	@Override
	public int getComponentCount() {
		if (servos!=null)
			return servos.length;
		return 0;
	}

	@Override
	public Component getComponent(int id) {
		if (servos!=null && servos.length>id)
			return servos[id];
		return null;
	}
}
