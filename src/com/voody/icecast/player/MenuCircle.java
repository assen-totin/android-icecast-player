package com.voody.icecast.player;

import java.lang.Math;

public class MenuCircle {
	private static double a, b, c;
	
	public static int getCircle(int x, int y, int size){
		//Re-calc coordinates to center of the circle
		x = Math.abs(size/2 - x);
		y = Math.abs(size/2 - y);
		a = getAB(x);
		b = getAB(y);
		
		// Test inner circle
		c = getC(size, 6);
		if ((a + b) < c)
			return 3;

		// Test middle circle
		c = getC(size, 3);
		if ((a + b) < c)
			return 2;

		// Test outer circle
		c = getC(size, 2);
		if ((a + b) < c)
			return 1;
			
		return 0;
    }	 
	
	private static double getAB(int xy) {
		double res = Math.pow((double)xy, 2);
		return res;
	}
	private static double getC(int size, int d) {
		double res = Math.pow((double)(size/d), 2);
		return res;
	}
}