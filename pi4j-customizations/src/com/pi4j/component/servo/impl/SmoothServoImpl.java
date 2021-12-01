package com.pi4j.component.servo.impl;

import java.util.Map;

import com.pi4j.component.servo.ServoDriver;
import com.pi4j.component.servo.SmoothServo;

public class SmoothServoImpl extends GenericServo implements SmoothServo {

	protected int startTime = 100;
	protected int stopTime = 100;
	protected int speed;
	protected int travelTime = 1000; // nominal
	
	public SmoothServoImpl(ServoDriver servoDriver, String name,
			Map<String, String> properties) {
		super(servoDriver, name, properties);
	}

	public SmoothServoImpl(ServoDriver servoDriver, String name) {
		super(servoDriver, name);
	}

	@Override
	public void setPosition(float position) {
		Mover mover = new Mover(position);
		mover.start();
	}

	@Override
	public void setSmoothStart(int msec) {
		startTime = msec;
	}

	@Override
	public void setSmoothStop(int msec) {
		stopTime = msec;
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

    public void setTravelTime(int msec) {
    	travelTime = msec;
    }
    
	private class Mover extends Thread {
		private int threadSleepResolution = 20; // minimum Thread.sleep() time
		private float endPosition;
		private float startPosition;
		private float step = 0;
		private float stepIncrement = 0;
		private int speed;
		private int startTime;
		private int stopTime;
		private int travelTime;
		
		public Mover(float position) {
			endPosition = position;
			startPosition = SmoothServoImpl.this.getPosition();
			startTime = SmoothServoImpl.this.startTime;
			stopTime = SmoothServoImpl.this.stopTime;
			speed = SmoothServoImpl.this.speed;
			travelTime = SmoothServoImpl.this.travelTime;
		}
		
        @Override
        public void run() {
            float position = startPosition;
            Orientation orientation;
            
            if (endPosition<startPosition)
            	orientation = Orientation.LEFT;
            else if (endPosition>startPosition)
            	orientation = Orientation.RIGHT;
            else
            	return; // start and end position are identical
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (orientation == Orientation.RIGHT) {
                        if (position < endPosition) {
                            position += step;
                        } else {
                        	break;
                        }
                    }
                    else if (orientation == Orientation.LEFT) {
                        if (position > endPosition) {
                            position -= step;
                        } else {
                        	break;
                        }
                    }

                    SmoothServoImpl.this.setPosition(position);

                    Thread.sleep(speed);
                    step += stepIncrement;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
	}
}
