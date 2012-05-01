package com.morgner.gaia;

import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

/**
 *
 * @author Christian Morgner
 */
public interface Entity {
	
	public void drawCell(Graphics g, int x, int y, int w, int h);
	public List<Effect> update(long dt);
	public int getX();
	public int getY();
	public boolean contains(Point p);
	public boolean isAlive();
}
