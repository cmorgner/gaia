/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.util;

/**
 *
 * @author Christian Morgner, SoftService GmbH
 */
public class IntColor {

	public int r = 0;
	public int g = 0;
	public int b = 0;
	public int a = 0;

	public IntColor(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public IntColor(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;

		validate();
	}

	public IntColor scale(double f) {

		this.r = FastMath.rint(r * f);
		this.g = FastMath.rint(g * f);
		this.b = FastMath.rint(b * f);
		this.a = FastMath.rint(a * f);

		validate();

		return this;
	}

	public IntColor scale(double fr, double fg, double fb, double fa) {

		this.r = FastMath.rint(r * fr);
		this.g = FastMath.rint(g * fg);
		this.b = FastMath.rint(b * fb);
		this.a = FastMath.rint(a * fa);

		validate();

		return this;
	}
	
	public static IntColor blend(IntColor dest, IntColor source) {

		int alpha = source.a;
		int neg   = 255 - alpha;
		
		int r = ((alpha * source.r) + (neg * dest.r)) / 255;
		int g = ((alpha * source.g) + (neg * dest.g)) / 255;
		int b = ((alpha * source.b) + (neg * dest.b)) / 255;
		
		return new IntColor(r, g, b, 255);
	}

	private void validate() {

		if(this.r <   0) this.r =   0;
		if(this.r > 255) this.r = 255;

		if(this.g <   0) this.g =   0;
		if(this.g > 255) this.g = 255;

		if(this.b <   0) this.b =   0;
		if(this.b > 255) this.b = 255;

		if(this.a <   0) this.a =   0;
		if(this.a > 255) this.a = 255;

	}
}
