/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Effect;
import com.morgner.gaia.Entity;
import com.morgner.gaia.Environment;
import com.morgner.gaia.Gaia;
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
public class Animal implements Entity {

	private Environment env = null;
	private boolean alive = true;
	private int direction = 0;
	private int food = 1000;
	private int cellX = 0;
	private int cellY = 0;
	private int w = 0;
	private int h = 0;
	private int x = 0;
	private int y = 0;
	
	public Animal(Environment env, int x, int y) {
		this.env = env;
		this.x = x;
		this.y = y;
		this.w = env.getWidth();
		this.h = env.getHeight();
	}
	
	@Override
	public void drawCell(Graphics gr, int x, int y, int w, int h) {
		
		int r = FastMath.rint((double)env.getCellSize() / 5.0);
		
		int lx = x+(cellX*r);
		int ly = y+(cellY*r);
		
		gr.setColor(Color.GRAY);
		gr.fillRect(lx, ly, r, r);
	}

	@Override
	public void update(Collection<Effect> effects, long dt) {
		
		Resource pos = env.getResource(x, y);
		double prob = 0.5;

		// die in water
		if(pos.hasResource(Resource.WATER)) {
			alive = false;
			return;
		}
		
		if(food < 10000) {
			if(pos.hasResource(Resource.PLANTS)) {

				food += 1;
				pos.addResource(Resource.PLANTS, -1);
				prob = 0.99;

			} else if(pos.getResource(Resource.MOISTURE) > 10) {

				food += 1;
				pos.addResource(Resource.MOISTURE, -1);

				prob = 0.9;
			}
		}

		if(Gaia.rand.nextDouble() > prob) {

			if(Gaia.rand.nextDouble() > 0.5) {

				direction += Gaia.rand.nextBoolean() ? 1 : -1;
				if(direction < 0) direction = 7;
				if(direction > 7) direction = 0;
			}

			if(food > 0) {
				
				move(pos);

				if(cellX < 0) { x--; cellX = 5; }
				if(cellX > 5) { x++; cellX = 0; }
				if(cellY < 0) { y--; cellY = 5; }
				if(cellY > 5) { y++; cellY = 0; }
				
				if(x  < 0) x = w - 1;
				if(x >= w) x = 0;

				if(y  < 0) y = h - 1;
				if(y >= h) y = 0;

				food--;
				
				if(food == 0) {
					alive = false;
				}
			}
		}
		
		return;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean contains(Point p) {
		return false;
	}
	
	@Override
	public boolean isAlive() {
		return alive;
	}
	
	private void move(Resource pos) {
		
		int step = 16;
		
		switch(direction) {
			
			case 0:	// up
				Resource up = env.getResource(x, y-1);
				if(!up.hasResource(Resource.WATER) && !(up.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellY -= 1;
					return;
				}
				
			case 1:	// up right
				Resource upRight = env.getResource(x+1, y-1);
				if(!upRight.hasResource(Resource.WATER) && !(upRight.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX += 1;
					cellY -= 1;
					return;
				}
				
			case 2:	// right
				Resource right = env.getResource(x+1, y);
				if(!right.hasResource(Resource.WATER) && !(right.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX += 1;
					return;
				}
				
			case 3:	// down right
				Resource downRight = env.getResource(x+1, y+1);
				if(!downRight.hasResource(Resource.WATER) && !(downRight.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX += 1;
					cellY += 1;
					return;
				}
				
			case 4:	// down
				Resource down = env.getResource(x, y+1);
				if(!down.hasResource(Resource.WATER) && !(down.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellY += 1;
					return;
				}
				
			case 5:	// down left
				Resource downLeft = env.getResource(x-1, y+1);
				if(!downLeft.hasResource(Resource.WATER) && !(downLeft.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX -= 1;
					cellY += 1;
					return;
				}
				
			case 6:	// left
				Resource left = env.getResource(x-1, y);
				if(!left.hasResource(Resource.WATER) && !(left.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX -= 1;
					return;
				}
				
			case 7:	// up left
				Resource upLeft = env.getResource(x-1, y-1);
				if(!upLeft.hasResource(Resource.WATER) && !(upLeft.higher(pos, step) && Gaia.rand.nextDouble() > 0.9)) {
					cellX -= 1;
					cellY -= 1;
					return;
				}
				
			default:
				break;
		}
	}

	@Override
	public void setHover(boolean hover) {
	}
}
