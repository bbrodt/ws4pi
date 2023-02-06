package com.pi4j.test;

public class HeadingToAngle {

	public static void main(String args[]) throws Exception {
		for (int t=0; t<360; t+=5) {
			int a = (360 - t + 90) % 360;
			System.out.println("Theta="+t+" alpha="+a);
		}
	}
}
