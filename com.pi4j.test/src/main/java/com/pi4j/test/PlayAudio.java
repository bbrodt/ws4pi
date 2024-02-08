package com.pi4j.test;

import java.io.IOException;

// NOTE: aplay only works with WAV files
public class PlayAudio implements Runnable {
    public static void main(String[] args){
        Thread t = new Thread(new PlayAudio());
        t.start();
    }   

    @Override
    public void run() {
    	try {
			Runtime.getRuntime().exec("aplay /home/pi/Sounds/startup.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}