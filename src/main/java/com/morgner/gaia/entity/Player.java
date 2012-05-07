/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;

/**
 *
 * @author Christian Morgner
 */
public class Player implements Entity {

	private Environment env = null;
	private boolean alive = true;
	private int w = 0;
	private int h = 0;

	private double localX = 0.0;
	private double localY = 0.0;

	private double cellSize = 4.0;
	
	public Player(Environment env, int x, int y) {
		this.env = env;
		this.w = env.getWidth();
		this.h = env.getHeight();
		
		localX = x;
		localY = y;
	}

	@Override
	public void drawCell(Graphics gr, int x, int y, int w, int h) {

//		int r = (int)Math.rint((double)env.getCellSize() / cellSize);
//		int visualCellSize = env.getCellSize();
//		int viewportX = env.getViewportX();
//		int viewportY = env.getViewportY();
//		
//		int lx = (int)Math.rint(((localX / cellSize) - viewportX) * visualCellSize);
//		int ly = (int)Math.rint(((localY / cellSize) - viewportY) * visualCellSize);
//
//		gr.setColor(Color.WHITE);
//		gr.fillOval(lx, ly, r, r);
//
//		gr.setColor(Color.BLACK);
//		gr.drawOval(lx, ly, r, r);
//		gr.drawOval(lx + 1, ly + 1, r - 2, r - 2);
//		gr.drawOval(lx + 2, ly + 2, r - 4, r - 4);
		
	}

	@Override
	public void update(Collection<Effect> effects, long dt) {

		double bX = w * cellSize;
		double bY = h * cellSize;

		if(localX <   0) localX = bX;
		if(localX >= bX) localX = 0;
		if(localY <   0) localY = bY;
		if(localY >= bY) localY = 0;

	}

	@Override
	public int getX() {
		return (int)Math.rint(localX / cellSize);
	}

	@Override
	public int getY() {
		return (int)Math.rint(localY / cellSize);
	}

	public double getLocalX() {
		return localX;
	}

	public double getLocalY() {
		return localY;
	}
	
	public double getCellSize() {
		return cellSize;
	}
	
	public double getScaledX() {
		return localX / cellSize;
	}
	
	public double getScaledY() {
		return localY / cellSize;
	}
	
	public void translate(double dx, double dy) {
		
//		Resource pos = env.getResource(getX(), getY());
//		Fx -= pos.getNormalX() * loadFactor;
//		Fy += pos.getNormalY() * loadFactor;
//		double dt2 = 500;
//
		localX += dx; // + (Fx / dt2);
		localY += dy; // + (Fy / dt2); 
		
		if(localX < 0) localX = 0;
		if(localY < 0) localY = 0;

		if(localX > w*cellSize) localX = w*cellSize;
		if(localY > h*cellSize) localY = h*cellSize;
	}
	
	public void setPosition(double dx, double dy) {
		
		localX = dx * cellSize; // + (Fx / dt2);
		localY = dy * cellSize; // + (Fy / dt2); 
		
		if(localX < 0) localX = 0;
		if(localY < 0) localY = 0;

		if(localX > w*cellSize) localX = w*cellSize;
		if(localY > h*cellSize) localY = h*cellSize;
	}

	@Override
	public boolean contains(Point p) {
		return false;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}
}
