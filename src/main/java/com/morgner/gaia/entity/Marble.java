/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Effect;
import com.morgner.gaia.Entity;
import com.morgner.gaia.Environment;
import com.morgner.gaia.Resource;
import com.morgner.gaia.util.FastMath;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;

/**
 *
 * @author Christian Morgner
 */
public class Marble implements Entity {

	private Environment env = null;
	private boolean alive = true;
	private int w = 0;
	private int h = 0;

	private double Fx = 0.0;
	private double Fy = 0.0;
	private double localX = 0.0;
	private double localX0 = 0.0;
	private double tmpX = 0.0;
	private double localY = 0.0;
	private double localY0 = 0.0;
	private double tmpY = 0.0;

	private double cellSize = 5.0;
	
	public Marble(Environment env, int x, int y) {
		this.env = env;
		this.w = env.getWidth();
		this.h = env.getHeight();
		
		localX = x;
		localX0 = x;
		localY = y;
		localY0 = y;
	}

	@Override
	public void drawCell(Graphics gr, int x, int y, int w, int h) {

		int r = FastMath.rint((double)env.getCellSize() / cellSize);
		int visualCellSize = env.getCellSize();
		int viewportX = env.getViewportX();
		int viewportY = env.getViewportY();
		
		int lx = FastMath.rint(((localX / cellSize) - viewportX) * visualCellSize);
		int ly = FastMath.rint(((localY / cellSize) - viewportY) * visualCellSize);

		gr.setColor(Color.WHITE);
		gr.fillOval(lx, ly, r, r);

		gr.setColor(Color.BLACK);
		gr.drawOval(lx, ly, r, r);
		gr.drawOval(lx + 1, ly + 1, r - 2, r - 2);
		gr.drawOval(lx + 2, ly + 2, r - 4, r - 4);
	}

	@Override
	public void update(Collection<Effect> effects, long dt) {

		Resource pos = env.getResource(getX(), getY());
		double bX = w * cellSize;
		double bY = h * cellSize;
		

		tmpX = localX;
		tmpY = localY;

		Fx -= pos.getNormalX() * 5.0;
		Fy += pos.getNormalY() * 5.0;

		double dt2 = (dt * dt);

		localX += ((localX - localX0) * 0.95) + (Fx / dt2);
		localY += ((localY - localY0) * 0.95) + (Fy / dt2);

		if(localX <   0) localX = bX;
		if(localX >= bX) localX = 0;
		if(localY <   0) localY = bY;
		if(localY >= bY) localY = 0;

		localX0 = tmpX;
		localY0 = tmpY;

		// reset forces
		Fx = 0.0;
		Fy = 0.0;
	}

	@Override
	public int getX() {
		return FastMath.rint(localX / cellSize);
	}

	@Override
	public int getY() {
		return FastMath.rint(localY / cellSize);
	}

	public double getLocalX() {
		return localX;
	}

	public double getLocalY() {
		return localY;
	}
	
	public void force(double dx, double dy) {
		Fx += dx;
		Fy += dy;
	}

	@Override
	public boolean contains(Point p) {
		return false;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}
	
	@Override
	public void setHover(boolean hover) {
	}
}
