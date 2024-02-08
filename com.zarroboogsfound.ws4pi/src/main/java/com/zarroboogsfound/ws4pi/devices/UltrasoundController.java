package com.zarroboogsfound.ws4pi.devices;

import java.lang.reflect.MalformedParametersException;
import java.util.concurrent.TimeoutException;

import com.pi4j.component.Component;
import com.pi4j.component.sensor.UltrasonicDistanceSensorComponent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinState;
import com.zarroboogsfound.ws4pi.DeviceType;
import com.zarroboogsfound.ws4pi.WS4PiConfig;
import com.zarroboogsfound.ws4pi.data.QueryParams;

import io.undertow.server.HttpServerExchange;

public class UltrasoundController extends DeviceController {
    final GpioController gpio = GpioFactory.getInstance();
	UltrasonicDistanceSensorComponent sensors[] = new UltrasonicDistanceSensorComponent[2];
	
	public UltrasoundController() {
		super(DeviceType.ULTRASOUND);
	}
	
	@Override
	public Object handleGetOperation(HttpServerExchange exchange) throws DeviceException {
		int channel = QueryParams.getInt(exchange, "channel").get().intValue();
		validate(channel);
		final UltrasonicDistanceSensorComponent sensor = (UltrasonicDistanceSensorComponent)getComponent(channel);
		try {
			return measure(sensor);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private double measure(UltrasonicDistanceSensorComponent sensor) throws TimeoutException, InterruptedException {
		double m[] = new double[10];
		for (int i=0; i<m.length; ++i) {
			m[i] = sensor.measure();
			Thread.sleep(10);
		}
		return trimmedAvg(m);
	}
	
	private static double trimmedAvg(double[] inputArray) {
		double max = inputArray[0];
		double min = inputArray[0];
		double sum = 0;
	    for(int i = 0; i < inputArray.length; i++){
	        sum = sum + inputArray[i];
	        if (inputArray[i] > max) {
	            max = inputArray[i];
	        }
	        if (inputArray[i] < min) {
	            min = inputArray[i];
	        }
	    }
	    return ((double) (sum - max - min)) / (inputArray.length - 2);
	}
	
	@Override
	public Object handleSetOperation(HttpServerExchange exchange) throws DeviceException {
		int channel = QueryParams.getInt(exchange, "channel").get().intValue();
		validate(channel);
		final UltrasonicDistanceSensorComponent sensor = (UltrasonicDistanceSensorComponent)getComponent(channel);
		if (QueryParams.getFloat(exchange, "low").isPresent()) {
			float low = QueryParams.getFloat(exchange, "low").get().floatValue();
			if (QueryParams.getFloat(exchange, "high").isPresent()) {
				float high = QueryParams.getFloat(exchange, "high").get().floatValue();
				sensor.setDetectionRange(low, high);
			}
			else
				throw new MalformedParametersException("Missing high value for range parameters");
		}
		if (QueryParams.getFloat(exchange, "high").isPresent()) {
			float high = QueryParams.getFloat(exchange, "high").get().floatValue();
			if (QueryParams.getFloat(exchange, "low").isPresent()) {
				float low = QueryParams.getFloat(exchange, "low").get().floatValue();
				sensor.setDetectionRange(low, high);
			}
			else
				throw new MalformedParametersException("Missing low value for range parameters");
		}
		if (QueryParams.getFloat(exchange, "temp").isPresent()) {
			float temp = QueryParams.getFloat(exchange, "temp").get().floatValue();
			sensor.setMeasurementTemperature(temp);
		}
		return null;
	}

	@Override
	public void initialize(WS4PiConfig config) throws Exception {
        GpioPin[] pins;
        for (int i=0; i<getComponentCount(); ++i) {
            pins = config.getGpioPins(DeviceType.ULTRASOUND, i);
            sensors[i] = new UltrasonicDistanceSensorComponent(pins[0], pins[1]);
            gpio.setShutdownOptions(true, PinState.LOW, pins);
        }
	}

	@Override
	public void start(WS4PiConfig config) {
		
	}

	public boolean isBusy(int id) {
		return false;
	}
	
	@Override
	public int getComponentCount() {
		if (sensors!=null)
			return sensors.length;
		return 0;
	}

	@Override
	public Component getComponent(int id) {
		if (sensors!=null && sensors.length>id)
			return sensors[id];
		return null;
	}

}
