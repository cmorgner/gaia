package com.morgner.gaia;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Christian Morgner
 */
public interface Entity {
	
	public void drawCell(Graphics g, int x, int y, int w, int h);
	public void update(Collection<Effect> effects, long dt);
	public int getX();
	public int getY();
	public boolean contains(Point p);
	public boolean isAlive();
	
	public void setHover(boolean hover);
}
