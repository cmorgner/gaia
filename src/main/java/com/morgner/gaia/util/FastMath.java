/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.util;

/**
 *
 * @author Christian Morgner
 */
public class FastMath {

	private static final int BIG_ENOUGH_INT = Integer.MAX_VALUE / 4;
	private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;

	public static int foor(double x) {
		return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	public static int rint(double x) {
		return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	public static int ceil(double x) {
		return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
	}

	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static double log(double a) {
		return Math.log(a);
	}

	public static double pow(double a, double b) {
		
		final long tmp = Double.doubleToLongBits(a);
		final long tmp2 = (long) (b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		
		return Double.longBitsToDouble(tmp2);
		// return Math.pow(a, b);
	}
}
