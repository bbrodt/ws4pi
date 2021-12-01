package com.zarroboogsfound.ws4pi.devices;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Deque;
import java.util.Map;

import com.pi4j.component.servo.Servo;
import com.pi4j.component.servo.impl.GenericServo;
import com.pi4j.component.servo.impl.PCA9685GpioServoProvider;
import com.pi4j.component.servo.impl.GenericServo.Orientation;
import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class ServoController extends DeviceController {
    // Create custom PCA9685 GPIO provider
    I2CBus bus;
    private PCA9685GpioProvider gpioProvider;
    private PCA9685GpioServoProvider gpioServoProvider;
    private Servo[] servos;
    private Sweeper sweeper;

    public ServoController() {
    	super("servo");
        servos = new Servo[16];
    }
    
    public void initialize() throws UnsupportedBusNumberException, IOException {
        gpioProvider = createProvider();
        provisionPwmOutputs(gpioProvider);
        gpioServoProvider = new PCA9685GpioServoProvider(gpioProvider);

        
        for (int s=0; s<servos.length; ++s) {
            servos[s] = new GenericServo(gpioServoProvider.getServoDriver(PCA9685Pin.ALL[s]), "Servo Channel " + s);
            servos[s].setProperty(Servo.PROP_END_POINT_LEFT, Float.toString(Servo.END_POINT_MAX));
            servos[s].setProperty(Servo.PROP_END_POINT_RIGHT, Float.toString(Servo.END_POINT_MAX));
        }
    }

    public void setPosition(int channel, float pos) throws IllegalArgumentException {
    	validate(channel, pos);
        stopSweeper();
        
        servos[channel].setPosition(pos);
    }
    
    public void setPositionSmooth(int channel, float pos) throws IllegalArgumentException {
    	validate(channel, pos);
        stopSweeper();
    }
    
    private void validate(int channel) throws IllegalArgumentException {
    	validate(channel, 0);
    }
    
    private void validate(int channel, float pos) throws IllegalArgumentException {
    	if (channel < 0 || channel >= servos.length)
    		throw new IllegalArgumentException("Channel "+channel+" invalid - must be in the range 0 to "+(servos.length-1));
        if (pos < Servo.POS_MAX_LEFT || pos > Servo.POS_MAX_RIGHT) {
        	throw new IllegalArgumentException("Servo position "+pos+" exceeded - must be in the range "
        	+Servo.POS_MAX_LEFT+" to "+Servo.POS_MAX_RIGHT);
        	
        }
    }
    
    private void stopSweeper() {
    	if (sweeper!=null) {
    		sweeper.interrupt();
    		sweeper = null;
    	}
    }
    
    private class Sweeper extends Thread {

        private int speed = 5;
        private final int step = 1; // make sure this is always true: 100 % step = 0
        private final int maxSleepBetweenSteps = 100;
        private final int channel;
        
        public Sweeper(int channel, float pos) {
        	this.channel = channel;
        }
        
        @Override
        public void run() {
            int position = 0;
            Orientation orientation = Orientation.RIGHT;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (orientation == Orientation.RIGHT) {
                        if (position < Servo.POS_MAX_RIGHT) {
                            position += step;
                        } else {
                            orientation = Orientation.LEFT;
                            position -= step;
                        }
                    } else if (orientation == Orientation.LEFT) {
                        if (position > Servo.POS_MAX_LEFT) {
                            position -= step;
                        } else {
                            orientation = Orientation.RIGHT;
                            position += step;
                        }
                    } else {
                        System.err.println("Unsupported value for enum <ServoBase.Orientation>: [" + orientation + "].");
                    }

                    servos[channel].setPosition(position);
                    Thread.currentThread();
                    if (position % 10 == 0) {
                        System.out.println("Position: " + position);
                    }
                    if (position==0 || position==100 || position==-100)
                        Thread.sleep(1000);
                    Thread.sleep(maxSleepBetweenSteps / speed);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void setSpeed(int speed) {
            if (speed < 1) {
                this.speed = 1;
            } else if (speed > 100) {
                this.speed = 100;
            } else {
                this.speed = speed;
            }
        }
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
	public Object handleGetOperation(HttpServerExchange exchange) {
		try {
			int channel = QueryParams.getInt(exchange, "channel").get().intValue();
			validate(channel);
			return Float.valueOf(servos[channel].getPosition());
		}
		catch (Exception e) {
			return e;
		}
	}

	@Override
	public Object handleSetOperation(HttpServerExchange exchange) {
		try {
			int channel = QueryParams.getInt(exchange, "channel").get().intValue();
			float position = QueryParams.getFloat(exchange, "position").get().floatValue();
			validate(channel, position);
			servos[channel].setPosition(position);
			return Float.valueOf(position);
		}
		catch (Exception e) {
			return e;
		}
	}
}
